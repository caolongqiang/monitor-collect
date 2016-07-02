package com.jimu.monitor.collect.monitorkeeper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Domain;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.db.Filter;
import com.jimu.monitor.collect.db.FilterMapper;
import com.jimu.monitor.collect.monitorkeeper.etcd.EtcdResultContainer;
import com.jimu.monitor.collect.monitorkeeper.etcd.SetKeyGenerator;
import com.jimu.monitor.collect.monitorkeeper.etcd.WhiteListService;
import com.jimu.monitor.utils.HttpClientHelper;
import com.jimu.monitor.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.jimu.monitor.Configs.config;

/**
 * Created by zhenbao.zhou on 16/6/19.
 */
@Slf4j
@Service
public class MonitorGroupInEtcdKeeper implements MonitorGroupKeeper {

    @Resource
    EtcdResultContainer etcdResultContainer;

    @Resource
    WhiteListService whiteListService;

    // groupList需要是一个cow list哦. 别用错了.
    // 只有这个group 才进行抓取任务
    private List<Group> groupList = new CopyOnWriteArrayList<>();

    public List<Group> getGroupList() {
        if (groupList.size() == 0) {
            refresh();
        }
        return groupList;
    }

    /**
     * 更新白名单
     */
    public void refresh() {
        log.info("start refresh monitor group in etcd keeper.");

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Group> groups = etcdResultContainer.ETCDResultList();
        Set<Filter> whiteList = whiteListService.getWhiteList();
        log.info("crawl group list in ectd api. group size:{}", groups.size());

        List<Group> filtered = groups.stream().filter(group -> whiteList.contains(group.getName()))
                .collect(Collectors.toList());

        log.info("stop refresh monitor group in etcd. filtered size:{}", filtered);
        log.debug("filtered group list:{}", JsonUtils.writeValueAsString(filtered));

        // TODO 其实这里最好做一个比较, 改变的比例大于多少时, 也放弃这次更新.
        if (CollectionUtils.isNotEmpty(filtered)) {
            log.info("change groupList. groupListSize:{}", groupList.size());
            groupList = new CopyOnWriteArrayList<>(filtered);
        } else {
            JMonitor.recordOne("filtered group list empty");
        }

        JMonitor.incrRecord("refresh group list", groupList.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

}
