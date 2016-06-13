package com.jimu.monitor.collect;

import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.bean.GroupMetric;
import com.jimu.monitor.collect.bean.Packet;
import com.jimu.monitor.utils.ApplicationContextHelper;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jimu.monitor.Configs.config;

/**
 * 具体运行的定时任务 Created by zhenbao.zhou on 16/5/25.
 */
@Slf4j
public final class MonitorTimerTask implements Runnable {

    private final static int MIN_INTERVAL = 50;

    private Group group;

    // 保证任务一开始就能运行
    private DateTime lastTime = DateTime.now().minusMinutes(1);

    // private Storer storer = new LogStorer();
    private Storer storer = ApplicationContextHelper.popBean(CarbonStorer.class);

    MonitorTimerTask(Group group) {
        this.group = group;
    }

    @Override
    public synchronized void run() {
        if (!meetPrecondition()) {
            log.debug("任务暂时不能执行. group:{}", group);
            return;
        }

        log.info("采集任务开始执行. group:{}", group);
        lastTime = DateTime.now();

        @SuppressWarnings({"unchecked"})
        CompletableFuture<Packet>[] futureArray = group.getDomainList().parallelStream().map(domain -> {
            domain.setType(group.getType());
            Collector collector = CollectorFactory.of(domain);
            return collector.collect();
        }).toArray(CompletableFuture[]::new);
        // toArray(value -> (CompletableFuture<Packet>[]) Array.newInstance(CompletableFuture.class, value));
        // toArray(CompletableFuture[]::new) 上面的这行代码, 也可以直接写成下面这样. 收到的报警是一样的


        // 合并结果
        combineAndStore(group, futureArray);

        log.info("采集任务结束. groupName:{}", group.groupKey());
        JMonitor.recordOne("monitor_timer_task_success");
    }

    private boolean meetPrecondition() {
        DateTime nowTime = DateTime.now();
        if (Seconds.secondsBetween(lastTime, nowTime).getSeconds() < MIN_INTERVAL) {
            log.debug("时间间隔太短, 这次的任务不能执行. group:{}, nowTime:{}, lastTime:{}", group, nowTime, lastTime);
            return false;
        }

        if (nowTime.getSecondOfMinute() < 30) {
            // 因为monitor client在每分钟前10s时, 进行数据运算.计算上一分钟的数据. 所以, 收集server需要在后半分钟进行采集
            log.debug("秒数小于30s, 这次的任务不能执行. group:{}, nowTime:{}, lastTime:{}", group, nowTime, lastTime);
            return false;
        }

        if (CollectionUtils.isEmpty(group.getDomainList())) {
            log.warn("group 里的domainList为空. domain:{}", group);
            return false;
        }

        return true;
    }

    /**
     * 合并结果
     *
     * @param group group
     * @param futureArray 采集任务的返回future
     */
    private void combineAndStore(final Group group, CompletableFuture<Packet>[] futureArray) {
        final CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futureArray);
        allDoneFuture.join();
        log.debug("come into combine and store. groupName:{}", group.groupKey());

        allDoneFuture.thenApply(v -> Stream.of(futureArray).map(CompletableFuture::join).collect(Collectors.toList()))
                .whenComplete((list, throwable) -> {
                    try {
                        Combiner combiner = CombinerFactory.of(group);
                        GroupMetric groupMetric = combiner.combine(group, list);
                        JMonitor.recordOne("combine_success");
                        storer.write(groupMetric);
                    } catch (Throwable e) {
                        log.error("combine packet error. packetListSize:{}, groupName:{}", list.size(),
                                group.groupKey(), e);
                        JMonitor.recordOne("combine_error");
                    }
                });
        log.debug("exists combine and store. groupName:{}", group.groupKey());
    }

}
