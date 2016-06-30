package com.jimu.monitor.collect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Group;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 调度服务的入口, 读取任务 Created by zhenbao.zhou on 2016/5/25
 */

@Service
@Slf4j
public class MonitorScheduler {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    @Resource(name = "monitorGroupInEtcdKeeper")
    private MonitorGroupKeeper groupKeeper;

    @Resource(name = "monitorConfigInFileService")
    private MonitorGroupKeeper fileGroupKeeper;

    // 无界队列,不丢弃采集任务
    private Executor threadPoolExecutor = Executors.newFixedThreadPool(100);

    @Scheduled(cron = "32 * * * * ?") // 每分钟的第32s执行一次
    public void scheduleJob() {
        log.info("start scheduleJob");
        try {
            Iterable<Group> groupIterable = Iterables.unmodifiableIterable(
                    Iterables.concat(groupKeeper.getGroupList(), fileGroupKeeper.getGroupList()));

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
            container.putIfAbsent(group.groupKey(), new MonitorTimerTask(group));
            return container.get(group.groupKey());
        }
    }
}
