package com.jimu.monitor.collect;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.bean.GroupMetric;
import com.jimu.monitor.collect.bean.Packet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.annotation.NotThreadSafe;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static com.jimu.monitor.Configs.config;

/**
 * 合并同一个组内的属性. Created by zhenbao.zhou on 16/5/26.
 */

@Slf4j
@NotThreadSafe
public class HttpCombiner implements Combiner {

    // 合并的步骤包括
    // 1. 计算颗粒度到domain的指标值
    // 2. 根据domain的结果, 计算颗粒度到group的指标值
    @Override
    public GroupMetric combine(Group group, List<Packet> packetList) {
        log.info("come into combiner of http; group:{}, packetListSize:{}", group.groupKey(), packetList.size());
        JMonitor.recordOne("http_combiner");

        // 结构为 host, metricName, metricValue
        Table<String, String, Double> domainMeasurements = HashBasedTable.create();

        for (Packet pkt : packetList) {
            if (pkt.getStatus() == Packet.STATUS.FAIL) {
                continue;
            }
            Map<String, Double> measurements = new DomainMetricCalculator(pkt).get();
            measurements.forEach((k, v) -> domainMeasurements.put(pkt.getDomain().getHost(), k, v));
        }

        Map<String, Double> groupMeasurements = new GroupMetricCalculator(domainMeasurements).get();

        log.info("exists  combiner of http; group:{}, packetListSize:{}", group.groupKey(), packetList.size());

        return GroupMetric.builder().domainMeasurements(domainMeasurements).group(group)
                .groupMeasurements(groupMeasurements).packetList(packetList).build();
    }

    /**
     * 计算单机指标值. 以_count结尾的, 要除以60, 变成qps
     */
    private static class DomainMetricCalculator implements Supplier<Map> {
        private Packet packet;

        DomainMetricCalculator(Packet packet) {
            this.packet = packet;
        }

        // 暂时没有用到, 先保留在这里. 保留原有的功能
        Set<String> qpsMetrics = ImmutableSet.of();

        /**
         * 遍历transformer, 第一个满足的(任意一个满足的),则直接返回. 因此, 注意条件的互斥性. 如果需要顺序, 用linked hash map来解决
         * <p>
         */
        Map<Predicate<String>, Function<Double, Double>> transformer = ImmutableMap.of(
               // (key) -> StringUtils.endsWithIgnoreCase(key, "_TIME"), (value) -> value / 1E6,

                (key) -> StringUtils.endsWithIgnoreCase(key, "_COUNT")
                        || StringUtils.endsWithIgnoreCase(key, "_CNT")
                        || qpsMetrics.contains(key),
                (value) -> value / 60);

        /**
         * 真正处理的方法. 其实这个calculator就只有这一个方法
         *
         * @return 这个domain里的所有指标
         */
        @Override
        public Map<String, Double> get() {
            return packet.getRawMeasurements().entrySet().stream().filter(entry -> entry.getValue() > 0)
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                        for (Predicate<String> predicate : transformer.keySet()) {
                            if (predicate.test(entry.getKey())) {
                                return transformer.get(predicate).apply(entry.getValue());
                            }
                        }
                        return entry.getValue();
                    }));
        }

    }

    /**
     * 计算整个group的指标值.
     * <p>
     * 只有某些指标需要计算平均值, 其他的算总值
     */
    private static class GroupMetricCalculator implements Supplier<Map> {
        private final Table<String, String, Double> domainMeasurements;

        GroupMetricCalculator(Table<String, String, Double> domainMeasurements) {
            this.domainMeasurements = domainMeasurements;
        }

        static Set<String> needAvgSuffixes = ImmutableSet.of("_Time", "_CACHE_Value", "_RADIO_L_Value", "_RADIO_Value");

        class SuffixPredicate implements Predicate<String> {
            @Override
            public boolean test(String input) {
                for (String suffix : needAvgSuffixes) {
                    if (StringUtils.endsWithIgnoreCase(input, suffix)) {
                        return true;
                    }
                }
                return false;
            }
        }

        Predicate<String> needAvgPredicate = new SuffixPredicate();

        @Override
        public Map<String, Double> get() {
            Preconditions.checkArgument(domainMeasurements != null && !domainMeasurements.isEmpty(), "host指标为空");

            Map<String, Double> retMap = Maps.newHashMap();
            domainMeasurements.columnMap().entrySet().forEach(entry -> {
                DoubleStream doubleStream = entry.getValue().entrySet().parallelStream()
                        .mapToDouble(Map.Entry::getValue).filter(e -> e > 0.0);

                if (needAvgPredicate.test(entry.getKey())) {
                    retMap.put(entry.getKey(), doubleStream.average().getAsDouble());
                } else {
                    retMap.put(entry.getKey(), doubleStream.sum());
                }
            });
            return retMap;
        }
    }

}
