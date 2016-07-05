package com.jimu.monitor.collect.monitorkeeper.etcd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.collect.db.Filter;
import com.jimu.monitor.collect.db.FilterMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 获取db里的app白名单 Created by zhenbao.zhou on 16/7/1.
 */
@Service
@Slf4j
public class WhiteListService {

    @Getter
    private volatile Set whiteList = Sets.newHashSet();

    @Resource
    private FilterMapper filterMapper;

    @PostConstruct
    public void init() throws Exception {
        reloadDBFilter();
        // DB的配置更新时, 会人为的触发一个动作
    }

    /**
     * 更新某一条记录的状态
     * 
     * @param id
     * @param status
     * @return
     */
    public int updateFilterStatus(int id, int status) {
        Filter filter = Filter.builder().id(id).status(status).build();
        return filterMapper.updateFilter(filter);
    }

    /**
     * 插入一个白名单, 默认status 为0(有效)
     * 
     * @param app
     * @param env
     * @return
     */
    public int insertFilter(String app, String env) {
        return filterMapper.insertFilter(app, env);
    }

    /**
     * 根据id查找filter
     * 
     * @param limit
     * @param offset
     * @return
     */
    public List<Filter> findFilterList(int limit, int offset) {
        return filterMapper.findFilterList(limit, offset);
    }

    /**
     * count所有的白名单
     * 
     * @return
     */
    public int countFilterList() {
        return filterMapper.countFilterList();
    }

    /**
     * 重新load整个db里的白名单
     */
    public void reloadDBFilter() {
        log.info("reload db filter begin");
        Stopwatch stopwatch = Stopwatch.createStarted();
        HashSet<String> set = Sets.newHashSet();
        // TODO 如果filter太多了之后, 这里代码要分页获取.
        List<Filter> filterList = filterMapper.queryAvailableFilterList();
        log.info("reload db filter 从数据库去取filter结束.  filterListSize:{}", filterList.size());
        JMonitor.recordOne("load filter from db.", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        filterList.forEach(filter -> {
            set.add(SetKeyGenerator.gen(filter.getEnv(), filter.getApp()));
        });

        if (!set.isEmpty()) {
            whiteList = set;
        } else {
            log.warn("db读取出来的filter 列表为空");
            JMonitor.recordOne("reload dbFilter error");
        }

        JMonitor.recordSize("db filter", whiteList.size());
    }

}
