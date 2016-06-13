package com.jimu.monitor.collect;

import com.google.common.collect.Maps;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Group;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jimu.monitor.Configs.config;

/**
 * 调度服务的入口, 读取任务 Created by zhenbao.zhou on 2016/5/25
 */

@Service
@Slf4j
public class MonitorScheduler {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    @Resource
    private MonitorFileAutoLoaderService autoLoaderService;

    // 无界队列,不丢弃采集任务
    private Executor threadPoolExecutor = Executors.newFixedThreadPool(100);

    @PostConstruct
    public void init() throws Exception {
        // 初始化多个定时任务
        // 2s 执行一次
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                List<Group> groupList = autoLoaderService.getGroupList();
                if (log.isDebugEnabled()) {
                    log.debug("current scheduler, groupList size:{}", groupList.size());
                }

                for (Group group : groupList) {
                    threadPoolExecutor.execute(TimerTaskFactory.of(group));// 马上执行收集各个group
                }
                JMonitor.recordOne("monitor_scheduler_init_success");
            } catch (Throwable e) {
                log.error("error in run scheduler", e);
                JMonitor.recordOne("monitor_scheduler_init_error");
            }
        }, 3000, 2000, TimeUnit.MILLISECONDS);

        log.info("end init scheduler");
    }

    private static class TimerTaskFactory {
        final static Map<String, MonitorTimerTask> container = Maps.newConcurrentMap();

        static MonitorTimerTask of(Group group) {
            container.putIfAbsent(group.groupKey(), new MonitorTimerTask(group));
            return container.get(group.groupKey());
        }
    }
}
