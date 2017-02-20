package com.jimu.monitor.collect;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Domain;
import com.jimu.monitor.collect.bean.Packet;
import com.jimu.monitor.utils.HttpClientHelper;
import com.jimu.monitor.utils.http.HttpClients;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.DoubleAccumulator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * 从提供http服务的应用里, 解释内容, 变成原始的监控指标, 存放在packet里
 * notice:packet里的监控指标
 * Created by zhenbao.zhou on 16/5/26.
 */
@Slf4j
public class HttpCollector implements Collector {

    private final Domain domain;

    private final static ListeningExecutorService threadPool;
    
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
            String content = HttpClients.syncClient().get(domain.getUrl()).getContent(); // 检查过url 和domain都不为空. 不会出现NPE
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
        log.debug("parse content:{}" , content);
        Map<String, Double> resultMap = Maps.newHashMap();
        Iterable<String> line = Splitter.on("\n").omitEmptyStrings().trimResults().split(content);
        int FIXED_LENGTH = 2;
        line.forEach(l -> {
            String[] strArray = StringUtils.split(l, "=");
            if (strArray.length == FIXED_LENGTH) {
                try {
                    String key = strArray[0].trim();
                    Double value = Double.valueOf(strArray[1]);
                    resultMap.put(key, value);
                } catch (NumberFormatException e) {
                    log.error("error in parse value:{}, key:{}, domain:{}", strArray[1], strArray[0], domain);
                }
            }
        });


        return resultMap;

    }

    public static void main(String[] args) {
        String content = "http://www.prnasia.com/m/mediafeed/rss?id=1223_failure_Count=0\na=89\nccc=zx";
        Map map = new HttpCollector(null).parse(content);
        System.err.println("map size:{}" + map.size());
    }

}
