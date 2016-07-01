package com.jimu.monitor.collect.monitorkeeper.etcd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Domain;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.utils.HttpClientHelper;
import com.jimu.monitor.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static com.jimu.monitor.Configs.config;

/**
 * 维护etcd里的数据结果 Created by zhenbao.zhou on 16/7/1.
 */
@Service
@Slf4j
public class EtcdResultContainer {

    private final static String HOST_NAME_PREFIX = "host";

    // 在etcd里的所有docker实例信息
    List<Group> allGroupsInEtcd = new CopyOnWriteArrayList<>();

    private List<Group> ETCDResultList() {
        return allGroupsInEtcd;
    }


    // @PostConstruct
    public void init() throws Exception {
        allGroupsInEtcd = crawlGroupListInETCD();

        // 注册一个接口, 监听etcd接口变化信息
    }

    /**
     *
     * 从api里获取内容 ETCD 的API内容是
     *
     * @return
     */
    private List<Group> crawlGroupListInETCD() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        String content = HttpClientHelper.get(config.getEtcdContentApi());
        log.debug("crawl ectd api {}, content:{}", config.getEtcdContentApi(), content);
        if (content == null) {
            log.warn("error in get etcd api content. content is null. api address is:{}", config.getEtcdContentApi());
            JMonitor.incrRecord("etcd api size", 0, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return Lists.newArrayList();
        }
        JMonitor.incrRecord("etcd api size", content.length(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        List<EtcdResult> etcdResultList = JsonUtils.readValue(content, new TypeReference<List<EtcdResult>>() {
        });

        if (CollectionUtils.isEmpty(etcdResultList)) {
            log.warn("error in get etcd api content. api address is:{}, 不能使用json 反序列化.", config.getEtcdContentApi());
            JMonitor.recordOne("etcd content error");
            return Lists.newArrayList();
        }

        return etcdListToGroupList(etcdResultList);
    }

    /**
     * 把etcd的多个结果, 合并成 一个group
     * 
     * @param etcdList
     * @return
     */
    private List<Group> etcdListToGroupList(List<EtcdResult> etcdList) {

        List<Group> groupList = Lists.newArrayList();

        SetMultimap<String, String> setMultimap = HashMultimap.create();
        etcdList.stream().filter(etcd -> etcd.generateUrl().isPresent())
                .forEach(etcd -> setMultimap.put(etcd.uniqKey(), etcd.generateUrl().get()));

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
}
