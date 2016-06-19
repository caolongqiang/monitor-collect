package com.jimu.monitor.collect;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.db.Filter;
import com.jimu.monitor.collect.db.FilterMapper;
import com.jimu.monitor.utils.HttpClientHelper;
import com.jimu.monitor.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock rLock = rwLock.readLock();
    private ReentrantReadWriteLock.WriteLock wLock = rwLock.writeLock();

    // 存值的方式是 group.departmentName group.name
    private SetMultimap<String, String> filterMap = HashMultimap.create();

    @Resource
    private FilterMapper filterMapper;

    @Scheduled(cron = "1 * * * * ?") // 每分钟的第1s执行一次
    public void refreshJob() {
        try {
            refresh();
        } catch (Throwable t) {
            log.info("error in refresh job.", t);
            JMonitor.recordOne("refresh etcd content error");
        }
    }

    // 这个将来要改成由etcd出发
    public void refresh() {
        log.info("start refresh monitor group in etcd.");

        if (filterMap.isEmpty()) {
            log.info("filterMap is empty. reload filter from db");
            reloadDBFilter();
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Group> groups = crawlGroupListInEtcdApi();
        log.info("crawl group list in ectd api. group size:{}", groups.size());

        List<Group> filtered = groups.stream().filter(group -> {
            rLock.lock();
            boolean flag = false;
            if (filterMap.get(group.getDepartment()).contains(group.getName())) {
                return true;
            }
            rLock.unlock();
            return flag;
        }).collect(Collectors.toList());

        log.info("stop refresh monitor group in etcd. filtered size:{}", filtered);
        log.debug("filtered group list:{}", JsonUtils.writeValueAsString(filtered));

        // TODO 其实这里最好做一个比较, 譬如改变的比例大于多少时, 也放弃这次更新.
        if (CollectionUtils.isNotEmpty(filtered)) {
            log.info("change groupList. groupListSize:{}", groupList.size());
            groupList = new CopyOnWriteArrayList<>(filtered);
        } else {
            JMonitor.recordOne("filtered group list empty");
        }

        JMonitor.incrRecord("refresh group list", groupList.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    // 从DB里重新所有的配置项. 加了一把写锁
    public void reloadDBFilter() {
        log.info("reload db filter begin");
        Stopwatch stopwatch = Stopwatch.createStarted();
        SetMultimap<String, String> map = HashMultimap.create();
        List<Filter> filterList = filterMapper.queryAvailableFilterList();
        log.info("reload db filter 从数据库去除filter结束.  filterListSize:{}", filterList.size());
        JMonitor.recordOne("load filter from db.", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        filterList.forEach(filter -> {
            map.put(filter.getDepartment(), filter.getGroupname());
        });

        if (!map.isEmpty()) {
            wLock.lock();
            filterMap = map;
            wLock.unlock();
        } else {
            log.warn("db读取出来的filter 列表为空");
            JMonitor.recordOne("reload dbFilter error");
        }

        JMonitor.recordOne("reload dbFilter", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        log.info("reload db filter ends");
    }

    // 从api里获取内容
    private List<Group> crawlGroupListInEtcdApi() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        String content = HttpClientHelper.get(config.getEtcdApi());
        log.debug("crawl ectd api content:{}", content);
        if (content == null) {
            log.warn("error in get etcd api content. content is null. api address is:{}", config.getEtcdApi());
            JMonitor.incrRecord("etcd api size", 0, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return Lists.newArrayList();
        }
        JMonitor.incrRecord("etcd api size", content.length(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        EtcdResult etcdResult = JsonUtils.readValue(content, EtcdResult.class);
        if (etcdResult == null) {
            log.warn("error in get etcd api content. api address is:{}, 不能使用json 反序列化.", config.getEtcdApi());
            JMonitor.recordOne("etcd content error");
            return Lists.newArrayList();
        }

        return etcdResult.toGroupList();
    }

    // TODO 定义etcd的格式 以及如何转换成grouplist
    public static class EtcdResult {

        List<Group> toGroupList() {
            return Lists.newArrayList();
        }
    }


}
