package com.jimu.monitor.collect;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.bean.GroupMetric;
import com.jimu.monitor.exception.MonitorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.jimu.monitor.Configs.config;

/**
 * 把数据存储到carbon里 Created by zhenbao.zhou on 16/5/29.
 */
@Slf4j
@Service
public class CarbonStorer implements Storer {

    /**
     * key之间的定界符
     */
    private final static String DELIMITER = ".";

    /**
     * 类似于原来的 s.hotel 里的s. 不知道有用不, 先定为s
     */
    private final static String COMMON_PREFIX = "s";

    private final static boolean NEED_DOMAIN_METRIC = true;

    private Socket socket;
    private OutputStream outputStream;

    /**
     * 抄过来的. 防止因为carbon挂了, 把tomcat也弄挂掉. 所以会丢弃一部分数据
     */
    private final Executor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(2000), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    @PostConstruct
    public void init() {
        if (Strings.isNullOrEmpty(config.getCarbonIp())) {
            throw new MonitorException("设置的watcher server地址不正确. server不能为空");
        }

        if (config.getCarbonPort() <= 0 || config.getCarbonPort() > 65535) {
            throw new MonitorException("设置的watcher server端口号不正确. port错误. port:" + config.getCarbonPort());
        }

        socket = new Socket();
    }

    @Override
    public void write(final GroupMetric groupMetric) {
        checkNotNull(groupMetric);
        log.info("Start write groupMetric:{}", groupMetric.getGroup().groupKey());

        final Group group = groupMetric.getGroup();
        executor.execute(() -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            // 先写整个group的监控数据
            String commonPrefix = COMMON_PREFIX + DELIMITER + wrapper(group.getDepartment()) + DELIMITER
                    + wrapper(group.getName());
            String groupPrefix = commonPrefix + DELIMITER + "group_all";
            writeMapData(groupPrefix, groupMetric.getGroupMeasurements());

            // 再写各台机器的监控数据
            if (NEED_DOMAIN_METRIC) {
                final Map<String, Map<String, Double>> domainData = groupMetric.getDomainMeasurements().rowMap();
                domainData.entrySet().forEach(entry -> {
                    String domainDataPrefix = commonPrefix + DELIMITER + wrapper(entry.getKey());
                    writeMapData(domainDataPrefix, entry.getValue());
                });
            }

            try {
                outputStream.flush();
            } catch (Exception e) {
                log.error("error in flush outputstream", e);
                JMonitor.recordOne("carbon_flush_error");
                close();
            }
            stopwatch.stop();
            log.debug("write group metric. cost time :{} ms. group:{}", stopwatch.elapsed(TimeUnit.MILLISECONDS),
                    groupMetric.getGroup().groupKey());

            JMonitor.recordOne("carbon_write", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        });

        log.info("exists writing groupMetric:{}", groupMetric.getGroup().groupKey());
    }

    private synchronized void connect(String host, int port) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 3000);
            socket.setSoTimeout(1000);
            outputStream = socket.getOutputStream();
            log.info("connect to carbon. host={}, port={}", host, port);
            JMonitor.recordOne("carbon_connect_times");
        } catch (Exception e) {
            log.error("连接carbon出错,host={},port={}", host, port, e);
        }
    }

    private void writeMapData(String prefix, Map<String, Double> metrics) {
        long nowSecond = System.currentTimeMillis() / 1000;

        metrics.entrySet().parallelStream().forEach(entry -> {
            String key = prefix + DELIMITER + wrapper(entry.getKey());
            push(key, entry.getValue(), nowSecond);
        });
    }

    private void push(String key, double value, long time) {
        synchronized(this) {
            if (!socket.isConnected() || socket.isClosed() || socket.isOutputShutdown()) {
                connect(config.getCarbonIp(), config.getCarbonPort());
            }
        }

        // 如果连接出异常, 丢弃这部分数据.
        if (socket.isConnected()) {
            String pushData = key + " " + value + " " + time + "\n";
            log.debug("push socket : data ={} ", pushData);
            try {
                outputStream.write(pushData.getBytes());
            } catch (Throwable t) {
                log.error("写入carbon出错,key={},data={}", key, pushData, t);
                close(); // 处理关闭后，才能重连
                JMonitor.recordOne("carbon_write_error");
            }
        } else {
            log.warn("socket is not connected. discard data. key:{}, value:{}", key, value);
            JMonitor.recordOne("carbon_discard_metric_number");
        }

/*
        String output = StringUtils.replaceEach(input, "/", "-");
        output = output.replaceAll("[^0-9a-zA-Z_\\-]", "_");
        */
    }

    /**
     * 对字符串进行处理, 使之可以满足carbon key的要求.
     *
     * @param input 要成为key的字段
     * @return 替换之后的字段
     */
    private String wrapper(String input) {
        //return StringUtils.replaceEach(input, new String[]{"[^0-9a-zA-Z_\\-]", "/"},
         //       new String[]{"_", "-"});
        String output = input.replaceAll("/", "-");
        output = output.replaceAll("[^0-9a-zA-Z_\\-]", "_");
        return output;
    }

    private synchronized void close() {
        try {
            Closeables.close(outputStream, true);
        } catch (IOException e) {
            log.error("关闭outputStream出错", e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            log.error("关闭outputStream出错", e);
        }
    }

    public static void main(String[] args) {
        String input = "哈哈哈asdfasdf.123123.b/zxcvz-xcv|zcv12呵呵呵";
        System.out.printf("output:" + new CarbonStorer().wrapper(input));
    }

}
