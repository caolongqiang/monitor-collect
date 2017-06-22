package com.jimu.monitor.collect.monitorkeeper.etcd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Domain;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.monitorkeeper.EtcdGroupConfigKeeper;
import com.jimu.monitor.utils.JsonUtils;
import com.jimu.monitor.utils.http.HttpClients;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.jimu.monitor.Configs.config;

/**
 * 维护etcd里的数据结果
 *
 * Created by zhenbao.zhou on 16/7/1.
 */
@Service
@Slf4j
public class EtcdResultContainer {

    private final static Logger etcdLog = LoggerFactory.getLogger("etcdLog");

    private final static String HOST_NAME_PREFIX = "host";

    // 在etcd里的所有docker实例信息
    // 这个地方其实可以用volatile也能搞定
    private AtomicReference<List<Group>> ar = new AtomicReference<>();

    @Autowired
    EtcdEventWatcher etcdEventWatcher;

    @Resource
    EtcdGroupConfigKeeper etcdGroupConfigKeeper;

    public List<Group> etcdResultList() {
        return ar.get();
    }

    @PostConstruct
    public void init() throws Exception {
        refreshJob();

        etcdEventWatcher.watch();
    }

    // 这个是全量的工作, 定时任务每五分钟执行一次.
    // 另外有一个watcher, 监控着event api的变化. 如果发生了变化, 则会调用这个refresh job
    @Scheduled(cron = "1 */5 * * * ?") // 每5分钟的第1s执行一次
    public void refreshJob() {
        try {
            List<EtcdResult> etcdResult = crawlGroupListInETCD(config.getBBAEEtcdContentApi());

            List<Group> allGroups = etcdListToGroupList(etcdResult);
            ar.set(allGroups);
            log.info("got {} jobs in etcd", allGroups.size());
            JMonitor.recordSize("job_in_etcd", allGroups.size());
        } catch (Throwable t) {
            log.info("error in refresh job.", t);
            JMonitor.recordOne("refresh etcd content error");
        }
    }

    /**
     *
     * 从api里获取内容 ETCD 的API内容是
     * 
     * <pre>
     *     <code>
     *[
     *    {
     *      app: "bbae-www",
     *      env: "production",
     *      gen: "50",
     *      name: "bbae-www.production.gen-50.seq-1@docker-cdsus-09",
     *      id: "bbb8e9acf01a07241aa4371e02ae616b9d09a1211df71c259b1d242aae749b60",
     *      ip: "172.20.17.2",
     *      ports: "{"8080/tcp":"172.20.0.17: 1098"}",
     *      host: "docker-cdsus-09",
     *      app_check: "0"
     *     },
     *     {
     *      app: "bbae-www",
     *      env: "production",
     *      gen: "50",
     *      name: "bbae-www.production.gen-50.seq-2@docker-cdsus-08",
     *      id: "3d06021794e22778011408ac0aa36253d4df523262bcc725c3d7dc04bb28dc18",
     *      ip: "172.20.16.3",
     *      ports: "{"8080/tcp":"172.20.0.16: 1112"}",
     *      host: "docker-cdsus-08",
     *      app_check: "0"
     *      }
     *  ]
     *     </code>
     * </pre>
     * 
     * @return
     */
    private List<EtcdResult> crawlGroupListInETCD(String url) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        String content = HttpClients.syncClient().get(url).getContent();
        etcdLog.info("crawl ectd api {}, content:{}", url, content);
        if (content == null) {
            log.warn("error in get etcd api content. content is null. api address is:{}", url);
            JMonitor.recordSize("etcd api size", 0);
            return Lists.newArrayList();
        }

        JMonitor.recordSize("etcd api size", content.length());
        JMonitor.recordOne("etcd api crawl", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        List<EtcdResult> etcdResultList = JsonUtils.readValue(content, new TypeReference<List<EtcdResult>>() {
        });

        if (CollectionUtils.isEmpty(etcdResultList)) {
            log.warn("error in get etcd api content. api address is:{}, 不能使用json 反序列化.", url);
            JMonitor.recordOne("etcd content error");
            return Lists.newArrayList();
        }

        return etcdResultList;
    }

    /**
     * 把etcd的多个结果, 合并成 一个group
     * 
     * @param etcdList
     * @return
     */
    private List<Group> etcdListToGroupList(List<EtcdResult> etcdList) {

        List<Group> groupList = Lists.newArrayList();

        // 合并相同的app
        SetMultimap<String, String> setMultimap = HashMultimap.create();

        etcdList.stream().filter(etcd -> generateMonitorUrl(etcd).isPresent())
                .forEach(etcd -> setMultimap.put(SetKeyGenerator.gen(etcd.env, etcd.app), generateMonitorUrl(etcd).get()));

        // 取出来
        setMultimap.asMap().forEach((name, urlSet) -> {
            List<Domain> domainList = Lists.newArrayList();
            Iterator<String> it = urlSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                domainList.add(new Domain(HOST_NAME_PREFIX + i, it.next()));
                i++;
            }
            groupList.add(new Group(name, domainList));
        });

        return groupList;
    }

    /**
     *
     * 生成这个机器对应的监控数据url. port的值为 ports: "{"8080/tcp":"172.20.0.16:1112"}" <br/>
     * 取出第一个key里的8080, 和etcd里的ip合起来, 作为monitorUrl
     *
     * @param etcdResult
     * @return
     */
    private Optional<String> generateMonitorUrl(EtcdResult etcdResult) {
        if (StringUtils.isBlank(etcdResult.getPorts())) {
            return Optional.empty();
        }

        Map<String, String> portMap = JsonUtils.readValue(etcdResult.ports, Map.class);
        if (MapUtils.isEmpty(portMap)) {
            log.debug("ports 转换异常. ports:{}, etcdResult:{}", etcdResult.ports, JsonUtils.writeValueAsString(this));
            return Optional.empty();
        }

        try {
            String key = portMap.keySet().toArray()[0].toString();
            String portValue = key.split("/")[0];
            int port = Integer.parseInt(portValue);
            return Optional.of("http://" + etcdResult.ip + ":" + port + "/_metrics/monitor.do");
        } catch (Exception e) {
            // 有时返回的数据里, 没有port, 是正常的
            JMonitor.recordOne("etcd_ports_change_error");
            log.warn("error in got port. port:{}", etcdResult.getPorts(), e);
            return Optional.empty();
        }
    }
}
