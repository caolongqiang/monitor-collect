package com.jimu.monitor.collect.controller;

import com.google.common.collect.Maps;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.JsonData;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.db.Filter;
import com.jimu.monitor.collect.monitorkeeper.MonitorConfigInFileService;
import com.jimu.monitor.collect.monitorkeeper.MonitorGroupInEtcdKeeper;
import com.jimu.monitor.collect.monitorkeeper.etcd.EtcdResultContainer;
import com.jimu.monitor.collect.monitorkeeper.etcd.WhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by zhenbao.zhou on 16/6/8.
 */
@RequestMapping("/monitor/")
@Controller
@Slf4j
public class MonitorConfigController {

    @Resource
    MonitorConfigInFileService monitorConfigInFileService;

    @Resource
    MonitorGroupInEtcdKeeper monitorGroupInEtcdKeeper;

    @Resource
    EtcdResultContainer etcdResultContainer;

    @Resource
    WhiteListService whiteListService;

    @RequestMapping("reloadFileConfig.j")
    public @ResponseBody String reloadFileConfig() throws Exception {
        monitorConfigInFileService.reload();
        return "reload success";
    }

    @RequestMapping("showFileGroupList.j")
    public @ResponseBody List<Group> showFileGroupList() throws Exception {
        return monitorConfigInFileService.getGroupList();
    }

    @RequestMapping("showEtcdGroupList.j")
    public @ResponseBody List<Group> showEtcdGroupList() throws Exception {
        return etcdResultContainer.ETCDResultList();
    }

    @RequestMapping("reloadEtcdConfig.j")
    public @ResponseBody String reloadEtcdConfig() throws Exception {
        etcdResultContainer.refreshJob();

        // 更新整个etcd的结果
        monitorGroupInEtcdKeeper.refresh();
        return "reload reloadEtcdConfig success";
    }

    @RequestMapping("showEtcdWorkingConfig.j")
    public @ResponseBody List<Group> showEtcdWorkingConfig() throws Exception {
        return monitorGroupInEtcdKeeper.getGroupList();
    }

    @RequestMapping("reloadWhiteList.j")
    public @ResponseBody JsonData reloadWhiteList() throws Exception {
        log.info("reload whitelist in db");
        whiteListService.reloadDBFilter();

        // 更新整个etcd的结果
        monitorGroupInEtcdKeeper.refresh();

        return JsonData.success("reload whitelist success");
    }

    @RequestMapping("queryWhiteList.j")
    public @ResponseBody Map queryWhiteList(@RequestParam(defaultValue = "1000") int limit,
            @RequestParam(defaultValue = "0") int offset) throws Exception {
        Map<String, Object> resultMap = Maps.newHashMap();
        List<Filter> filterList = whiteListService.findFilterList(limit, offset);
        resultMap.put("total", whiteListService.countFilterList());
        resultMap.put("rows", filterList);
        return resultMap;
    }

    @RequestMapping("insertFilter.j")
    public @ResponseBody JsonData insertFilter(@RequestParam String app, @RequestParam String env) {
        log.info("insert app:{}, env:{}", app, env);
        JMonitor.recordOne("db insert filter");
        try {
            whiteListService.insertFilter(app, env);
            return JsonData.success("添加成功");
        } catch (Exception e) {
            log.warn("error in insert app:{}, env:{}", app, env, e);
            return JsonData.success("插入失败");
        }
    }

    @RequestMapping("updateFilter.j")
    public @ResponseBody JsonData updateFilter(@RequestParam int id, @RequestParam int status) {
        Filter filter = Filter.builder().id(id).status(status).build();
        JMonitor.recordOne("db update filter");
        log.info("update filter. id:{}, status:{}", id, status);
        try {
            whiteListService.updateFilter(filter);
            return JsonData.success("更新成功");
        } catch (Exception e) {
            log.warn("error in update id:{}, status:{}", id, status, e);
            return JsonData.success("更新失败");
        }
    }

    @RequestMapping("index.j")
    public ModelAndView showWhiteList() {
        ModelAndView mv = new ModelAndView("whitelist");
        return mv;
    }
}
