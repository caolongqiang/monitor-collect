package com.jimu.monitor.grafana.dashboard;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.google.common.base.Stopwatch;
import com.jimu.common.jmonitor.JMonitor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import static com.jimu.monitor.Configs.config;

/**
 * 调度服务的入口, 读取任务 Created by zhenbao.zhou on 2016/5/25
 */

@Service
@Slf4j
public class DashboardScheduler {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    // TODO 每2分钟全量刷新一次dashboard, 这个将来可能会有性能问题, 造成grafana压力过大.
    @Scheduled(cron = "5 */2 * * * ?") // 每2分钟的第1s执行一次
    public void job() throws Exception {
        log.info("start DashboardScheduler");

        // 每两分钟钟执行一次生成dashboard的定时任务.
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            DashboardMaintainer maintainer = new DashboardMaintainer();
            maintainer.run();
        } catch (Throwable e) {
            log.error("error in run DashboardMaintainer", e);
            JMonitor.recordOne("dashboard scheduler error");
        }

        JMonitor.recordOne("dashboard scheduler", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        log.info("stop DashboardScheduler");
    }
}
