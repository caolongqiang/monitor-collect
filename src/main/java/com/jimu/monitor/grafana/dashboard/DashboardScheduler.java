package com.jimu.monitor.grafana.dashboard;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import static com.jimu.monitor.Configs.config;

/**
 * 调度服务的入口, 读取任务
 * Created by zhenbao.zhou on 2016/5/25
 */

@Service
@Slf4j
public class DashboardScheduler {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() throws Exception {
        log.info("start init DashboardScheduler");

        // 定期钟执行一次生成dashboard的定时任务. 线上是30分钟
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
               DashboardMaintainer maintainer = new DashboardMaintainer();
                maintainer.run();
            } catch (Throwable e) {
                log.error("error in run DashboardMaintainer", e);
            }
        }, 2, config.getDbRefreshIntervalInMin(), TimeUnit.MINUTES);

        log.info("end init DashboardScheduler");
    }
}
