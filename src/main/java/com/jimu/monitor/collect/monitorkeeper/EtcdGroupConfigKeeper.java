package com.jimu.monitor.collect.monitorkeeper;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.monitorkeeper.etcd.EtcdResultContainer;
import com.jimu.monitor.collect.monitorkeeper.etcd.WhiteListService;
import com.jimu.monitor.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by zhenbao.zhou on 16/6/19.
 */
@Slf4j
@Service
public class EtcdGroupConfigKeeper implements MonitorGroupKeeper {

    @Resource
    EtcdResultContainer etcdResultContainer;

    @Resource
    WhiteListService whiteListService;

    /**
     * 每次都从etcd所有的全量和 db里的白名单里, 重新计算现在需要抓取的配置。
     * 这个程序理论上来说, 一分钟调用一次, 所以性能不是问题, 也不用存储过滤后的结果
     */
    public List<Group> getGroupList() {
        log.info("start refresh monitor group in etcd keeper.");

        List<Group> groups = etcdResultContainer.etcdResultList();
        Set<String> whiteList = whiteListService.getWhiteList();
        log.info("crawl group list in ectd api. group size:{}", groups.size());

        List<Group> filtered = groups.stream().filter(group -> whiteList.contains(group.getName()))
                .collect(Collectors.toList());

        log.info("stop refresh monitor group in etcd. filtered size:{}", filtered);
        log.debug("filtered group list:{}", JsonUtils.writeValueAsString(filtered));

        JMonitor.recordSize("crawl group list size", filtered.size());
        return filtered;
    }

}
