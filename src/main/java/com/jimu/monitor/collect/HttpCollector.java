package com.jimu.monitor.collect;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Domain;
import com.jimu.monitor.collect.bean.Packet;
import com.jimu.monitor.utils.HttpClientHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.jimu.monitor.Configs.config;

/**
 * 从提供http服务的应用里, 解释内容, 变成原始的监控指标, 存放在packet里
 * notice:packet里的监控指标
 * Created by zhenbao.zhou on 16/5/26.
 */
@Slf4j
public class HttpCollector implements Collector {

    protected final Domain domain;

    static ListeningExecutorService threadPool;
    
    private final static int MAX_THREAD_NUM = 100;

    public HttpCollector(Domain domain) {
        this.domain = domain;
    }

    static {
        threadPool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(MAX_THREAD_NUM));
    }

    @Override
    public CompletableFuture<Packet> collect() {
        checkArgument(domain != null && !isNullOrEmpty(domain.getUrl()), "domain为空 或者 domain的url为空");

        log.info("come into http collect; domain:{}", domain);

        Packet packet = Packet.builder().domain(domain).build();

        CompletableFuture<Packet> packetFuture =  CompletableFuture.supplyAsync(() -> {
            String content = HttpClientHelper.get(domain.getUrl()); // 检查过url 和domain都不为空. 不会出现NPE
            packet.setContent(content);
            packet.setDuration(System.currentTimeMillis() - packet.getTimestamp());
            packet.setRawMeasurements(parse(content));
            packet.setStatus(Packet.STATUS.SUCCESS);
            return packet;
        }, threadPool);

        packetFuture.exceptionally(ex ->  {
            log.error("error in getting packet. domain:{}", domain, ex);
            JMonitor.recordOne("collect_error");
            return Packet.errorPacket();
        });

        log.info("exists http collect; domain:{}", domain);
        return packetFuture;
    }

    private Map<String, Double> parse(String content) {
        checkNotNull(content);
        Map<String, String> map = Splitter.on("\n").omitEmptyStrings().trimResults().withKeyValueSeparator("=")
                .split(content);

        return Maps.transformEntries(map, (String key, String value) -> Double.valueOf(value));

    }

    public static void main(String[] args) throws Exception {
        Domain domain = new Domain();
        domain.setHost("test");
        domain.setUrl("http://l-qtaorderstorep18.h.cn8.qunar.com:8080/monitor/qmonitor.jsp");
        HttpCollector collector = new HttpCollector(domain);
        CompletableFuture<Packet> listenableFuture = collector.collect();
        Packet packet = listenableFuture.get();
        Thread.sleep(50000);
    }
}
