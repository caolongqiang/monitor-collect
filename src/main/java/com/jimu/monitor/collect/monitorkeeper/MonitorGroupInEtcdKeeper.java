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


    // groupList需要是一个cow list哦. 别用错了
    @Getter
    private List<Group> groupList = new CopyOnWriteArrayList<>();
//
//    @Resource
//    private FilterMapper filterMapper;
//
//    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
//    private ReentrantReadWriteLock.ReadLock rLock = rwLock.readLock();
//    private ReentrantReadWriteLock.WriteLock wLock = rwLock.writeLock();
//
//    // 白名单, 只有在白名单里的app+env,  才会进行抓取
//    private HashSet<String> whiteList = Sets.newHashSet();
//
//    @Scheduled(cron = "1 * * * * ?") // 每分钟的第1s执行一次
//    public void refreshJob() {
//        try {
//            refresh();
//        } catch (Throwable t) {
//            log.info("error in refresh job.", t);
//            JMonitor.recordOne("refresh etcd content error");
//        }
//    }
//
//    // 这个将来要改成由etcd出发
//    public void refresh() {
//        log.info("start refresh monitor group in etcd.");
//
//        if (whiteList.isEmpty()) {
//            log.info("filterMap is empty. reload filter from db");
//            reloadDBFilter();
//        }
//
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        List<Group> groups = crawlGroupListInEtcdApi();
//        log.info("crawl group list in ectd api. group size:{}", groups.size());
//
//        List<Group> filtered = groups.stream().filter(group -> {
//            rLock.lock();
//            boolean flag = false;
//            if (whiteList.contains(buildSetKey(group.getDepartment(), group.getName()))) {
//                return true;
//            }
//            rLock.unlock();
//            return flag;
//        }).collect(Collectors.toList());
//
//        log.info("stop refresh monitor group in etcd. filtered size:{}", filtered);
//        log.debug("filtered group list:{}", JsonUtils.writeValueAsString(filtered));
//
//        // TODO 其实这里最好做一个比较, 改变的比例大于多少时, 也放弃这次更新.
//        if (CollectionUtils.isNotEmpty(filtered)) {
//            log.info("change groupList. groupListSize:{}", groupList.size());
//            groupList = new CopyOnWriteArrayList<>(filtered);
//        } else {
//            JMonitor.recordOne("filtered group list empty");
//        }
//
//        JMonitor.incrRecord("refresh group list", groupList.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
//    }
//
//    // 从DB里重新所有的配置项. 加了一把写锁
//    public void reloadDBFilter() {
//        log.info("reload db filter begin");
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        HashSet<String> set = Sets.newHashSet();
//        List<Filter> filterList = filterMapper.queryAvailableFilterList();
//        log.info("reload db filter 从数据库去除filter结束.  filterListSize:{}", filterList.size());
//        JMonitor.recordOne("load filter from db.", stopwatch.elapsed(TimeUnit.MILLISECONDS));
//        filterList.forEach(filter -> {
//            set.add(buildSetKey(filter.getEnv(), filter.getApp()));
//        });
//
//        if (!set.isEmpty()) {
//            wLock.lock();
//            whiteList = set;
//            wLock.unlock();
//        } else {
//            log.warn("db读取出来的filter 列表为空");
//            JMonitor.recordOne("reload dbFilter error");
//        }
//
//        JMonitor.recordOne("reload dbFilter", stopwatch.elapsed(TimeUnit.MILLISECONDS));
//        log.info("reload db filter ends");
//    }
//
//



}
