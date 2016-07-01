package com.jimu.monitor.collect.monitorkeeper.etcd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.db.Filter;
import com.jimu.monitor.collect.db.FilterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * 获取db里的app白名单 Created by zhenbao.zhou on 16/7/1.
 */
@Service
@Slf4j
public class WhiteListService {

    Set whiteList = new CopyOnWriteArraySet<>();

    @Resource
    private FilterMapper filterMapper;

    // @PostConstruct
    public void init() throws Exception {
        reloadDBFilter();
        // 注册一个接口, 监听etcd接口变化信息
    }

    public int updateFilter(Filter filter) {
        return filterMapper.updateFilter(filter);
    }

    public int insertFilter(String app, String env) {
        return filterMapper.insertFilter(app, env);
    }

    public List<Filter> findAllFilterList() {
        return filterMapper.finaAllFilter();
    }

    /**
     * 重新load整个db数据
     */
    public void reloadDBFilter() {
        log.info("reload db filter begin");
        Stopwatch stopwatch = Stopwatch.createStarted();
        HashSet<String> set = Sets.newHashSet();
        List<Filter> filterList = filterMapper.queryAvailableFilterList();
        log.info("reload db filter 从数据库去除filter结束.  filterListSize:{}", filterList.size());
        JMonitor.recordOne("load filter from db.", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        filterList.forEach(filter -> {
            set.add(SetKeyGenerator.gen(filter.getEnv(), filter.getApp()));
        });

        if (!set.isEmpty()) {
            whiteList = new CopyOnWriteArraySet<>(set);
        } else {
            log.warn("db读取出来的filter 列表为空");
            JMonitor.recordOne("reload dbFilter error");
        }
    }

}
