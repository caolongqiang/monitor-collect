package com.jimu.monitor;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.annotation.ThreadSafe;

import java.io.Reader;
import java.util.Properties;

import static com.google.common.io.Resources.asCharSource;
import static com.google.common.io.Resources.getResource;

/**
 * Created by yue.liu on 16/5/22.
 */
@ThreadSafe
@Slf4j
public enum Configs {

    config;

    private static final Properties properties = new Properties();

    static {
        try (Reader reader = asCharSource(getResource("config.properties"), Charsets.UTF_8).openStream()) {
            properties.load(reader);
        } catch (Exception ex) {
            log.error("配置文件初始化出现问题", ex);
            throw new Error("配置文件初始化异常"); // 中止程序启动
        }
    }

    public String getMonitorPath() {
        return properties.getProperty("monitor.collect.path", "").trim();
    }

    /**
     * carbon是存储数据使用的机器
     */
    public String getCarbonIp() {
        return properties.getProperty("carbon.ip", "").trim();
    }

    public int getCarbonPort() {
        return Integer.parseInt(properties.getProperty("carbon.port", ""));
    }

    public String getVmPath() {
        return properties.getProperty("vm.loader.path", "").trim();
    }

    public String getNewDbTemplateName() {
        return properties.getProperty("vm.newdb.template", "").trim();
    }

    public String getGraphiteApi() {
        return properties.getProperty("graphite.api", "").trim();
    }

    public String getGrafanaApiUri() {
        return properties.getProperty("grafana.api.uri", "").trim();
    }

    public String getGrafanaAuthorization() {
        return properties.getProperty("grafana.api.authorization", "").trim();
    }

    public int getDbRefreshIntervalInMin() {
        return Integer.parseInt(properties.getProperty("dashboard.refresh.interval", "").trim());
    }

    public boolean isMonitorAutoRefresh() {
        return "true".equalsIgnoreCase(properties.getProperty("monitor.autorefresh", "").trim());
    }

    public String getEtcdContentApi() {
        return properties.getProperty("etcd.content.api", "").trim();
    }

    public String getEtcdEventApi() {
        return properties.getProperty("etcd.event.api", "").trim();
    }

}
