package com.jimu.monitor.collect.controller;

import com.google.common.collect.Maps;
import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.JsonData;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.db.Filter;
import com.jimu.monitor.collect.monitorkeeper.FileGroupConfigService;
import com.jimu.monitor.collect.monitorkeeper.EtcdGroupConfigKeeper;
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
    FileGroupConfigService fileGroupConfigService;

    @Resource
    EtcdGroupConfigKeeper etcdGroupConfigKeeper;

    @Resource
    EtcdResultContainer etcdResultContainer;

    @Resource
    WhiteListService whiteListService;

    @RequestMapping("reloadFileConfig.j")
    public @ResponseBody String reloadFileConfig() throws Exception {
        fileGroupConfigService.reload();
        return "reload success";
    }

    @RequestMapping("showFileGroupList.j")
    public @ResponseBody List<Group> showFileGroupList() throws Exception {
        return fileGroupConfigService.getGroupList();
    }

    @RequestMapping("showEtcdGroupList.j")
    public @ResponseBody List<Group> showEtcdGroupList() throws Exception {
        return etcdResultContainer.etcdResultList();
    }

    @RequestMapping("reloadEtcdConfig.j")
    public @ResponseBody String reloadEtcdConfig() throws Exception {
        etcdResultContainer.refreshJob();

        return "reload reloadEtcdConfig success";
    }

    @RequestMapping("showEtcdWorkingConfig.j")
    public @ResponseBody List<Group> showEtcdWorkingConfig() throws Exception {
        return etcdGroupConfigKeeper.getGroupList();
    }

    @RequestMapping("reloadWhiteList.j")
    public @ResponseBody JsonData reloadWhiteList() throws Exception {
        log.info("reload whitelist in db");
        whiteListService.reloadDBFilter();

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
        JMonitor.recordOne("db update filter");
        log.info("update filter. id:{}, status:{}", id, status);
        try {
            whiteListService.updateFilterStatus(id, status);
            return JsonData.success("更新成功");
        } catch (Exception e) {
            log.warn("error in update id:{}, status:{}", id, status, e);
            return JsonData.success("更新失败");
        }
    }

    @RequestMapping("index.j")
    public ModelAndView showWhiteList() {
        return new ModelAndView("whitelist");
    }
}
