package com.jimu.monitor.collect.monitorkeeper.etcd;

import com.google.common.base.Optional;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 保存etcd返回回来的信息. 具体的内容
 * @see com.jimu.monitor.collect.monitorkeeper.etcd.EtcdResultContainer
 * Created by zhenbao.zhou on 16/7/1.
 */
@Getter
@Setter
@ToString
@Slf4j
public class EtcdResult {

    String app;
    String env;
    String gen;
    String name;
    String id;
    String ip;
    String ports;
    String host;
    String app_heck;

    public Optional<String> generateUrl() {
        Map<String, String> portMap = JsonUtils.readValue(ports, Map.class);
        if (portMap == null || portMap.size() < 0) {
            log.error("ports 转换异常. ports:{}, etcdResult:{}", ports, this);
            JMonitor.recordOne("etcd_ports_change_error");
            return Optional.absent();
        }

        try {
            String key = portMap.keySet().toArray()[0].toString();
            String portValue = key.split("/")[0];
            int port = Integer.parseInt(portValue);
            return Optional.of("http://" + ip + ":" + port + "/_metric/monitor.do");
        } catch (Exception e) {
            // 有时返回的数据里, 没有port, 是正常的
            return Optional.absent();
        }
    }

    public String uniqKey() {
        return env + "__" + app;
    }

}

