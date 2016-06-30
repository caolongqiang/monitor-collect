package com.jimu.monitor.collect;

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

    private final static String HOST_NAME_PREFIX = "host";

    // groupList需要是一个cow list哦. 别用错了
    @Getter
    private List<Group> groupList = new CopyOnWriteArrayList<>();

    //
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock rLock = rwLock.readLock();
    private ReentrantReadWriteLock.WriteLock wLock = rwLock.writeLock();

    private HashSet<String> filterSet = Sets.newHashSet();

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

        if (filterSet.isEmpty()) {
            log.info("filterMap is empty. reload filter from db");
            reloadDBFilter();
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Group> groups = crawlGroupListInEtcdApi();
        log.info("crawl group list in ectd api. group size:{}", groups.size());

        List<Group> filtered = groups.stream().filter(group -> {
            rLock.lock();
            boolean flag = false;
            if (filterSet.contains(buildSetKey(group.getDepartment(), group.getName()))) {
                return true;
            }
            rLock.unlock();
            return flag;
        }).collect(Collectors.toList());

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

    // 从DB里重新所有的配置项. 加了一把写锁
    public void reloadDBFilter() {
        log.info("reload db filter begin");
        Stopwatch stopwatch = Stopwatch.createStarted();
        HashSet<String> set = Sets.newHashSet();
        List<Filter> filterList = filterMapper.queryAvailableFilterList();
        log.info("reload db filter 从数据库去除filter结束.  filterListSize:{}", filterList.size());
        JMonitor.recordOne("load filter from db.", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        filterList.forEach(filter -> {
            set.add(buildSetKey(filter.getEnv(), filter.getApp()));
        });

        if (!set.isEmpty()) {
            wLock.lock();
            filterSet = set;
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
        log.debug("crawl ectd api {}, content:{}", config.getEtcdApi(), content);
        if (content == null) {
            log.warn("error in get etcd api content. content is null. api address is:{}", config.getEtcdApi());
            JMonitor.incrRecord("etcd api size", 0, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return Lists.newArrayList();
        }
        JMonitor.incrRecord("etcd api size", content.length(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        List<EtcdResult> etcdResultList = JsonUtils.readValue(content, new TypeReference<List<EtcdResult>>() {
        });

        if (CollectionUtils.isEmpty(etcdResultList)) {
            log.warn("error in get etcd api content. api address is:{}, 不能使用json 反序列化.", config.getEtcdApi());
            JMonitor.recordOne("etcd content error");
            return Lists.newArrayList();
        }

        return etcdListToGroupList(etcdResultList);
    }

    private String buildSetKey(String env, String app) {
        return env + "__" + app;
    }

    private List<Group> etcdListToGroupList(List<EtcdResult> etcdList) {

        List<Group> groupList = Lists.newArrayList();

        SetMultimap<String, String> setMultimap = HashMultimap.create();
        etcdList.stream().filter(etcd -> etcd.generateUrl().isPresent())
                .forEach(etcd -> setMultimap.put(buildSetKey(etcd.getEnv(), etcd.getApp()), etcd.generateUrl().get()));
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

    // TODO 定义etcd的格式 以及如何转换成grouplist
    @Getter
    @Setter
    @ToString
    public static class EtcdResult {
        /**
         * app: "jinshi-ducai", env: "develop", gen: "2", name: "jinshi-ducai.develop.gen-2.seq-1@qa-115", id:
         * "8663559540c1e4082b86bad8cf5e5d36fc44f13faeb1b819702bddf214327706", ip: "172.16.185.9", ports:
         * "{"8080/tcp":"172.16.7.115:1080"}", host: "qa-115", app_check: "0"
         */

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

    }

}
