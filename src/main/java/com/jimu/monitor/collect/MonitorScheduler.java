package com.jimu.monitor.collect;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.monitorkeeper.MonitorGroupKeeper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 调度服务的入口, 读取任务 Created by zhenbao.zhou on 2016/5/25
 */

@Service
@Slf4j
public class MonitorScheduler {

    @Resource(name = "etcdGroupConfigKeeper")
    private MonitorGroupKeeper etcdGroupKeeper;

    @Resource(name = "fileGroupConfigService")
    private MonitorGroupKeeper fileGroupKeeper;

    // 无界队列,不丢弃采集任务
    private final Executor threadPoolExecutor = Executors.newFixedThreadPool(100);

    @Scheduled(cron = "32 * * * * ?") // 每分钟的第32s执行一次
    public void scheduleJob() {
        log.info("start scheduleJob");
        try {
            Iterable<Group> groupIterable = Iterables.unmodifiableIterable(
                    Iterables.concat(etcdGroupKeeper.getGroupList(), fileGroupKeeper.getGroupList()));

            for (Group group : groupIterable) {
                threadPoolExecutor.execute(TimerTaskFactory.of(group));// 马上执行收集各个group
            }
            JMonitor.recordOne("monitor_scheduler_init_success");
        } catch (Throwable e) {
            log.error("error in run scheduler", e);
            JMonitor.recordOne("monitor_scheduler_init_error");
        }

        log.info("stop scheduleJob");
    }

    private static class TimerTaskFactory {
        final static Map<String, MonitorTimerTask> container = Maps.newConcurrentMap();

        static MonitorTimerTask of(Group group) {
            container.putIfAbsent(group.groupKey(), new MonitorTimerTask());
            MonitorTimerTask task = container.get(group.groupKey());
            task.setGroup(group);
            return task;
        }
    }
}
