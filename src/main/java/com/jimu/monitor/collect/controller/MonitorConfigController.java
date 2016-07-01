package com.jimu.monitor.collect.controller;

import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.db.Filter;
import com.jimu.monitor.collect.monitorkeeper.MonitorConfigInFileService;
import com.jimu.monitor.collect.monitorkeeper.MonitorGroupInEtcdKeeper;
import com.jimu.monitor.collect.monitorkeeper.etcd.EtcdResultContainer;
import com.jimu.monitor.collect.monitorkeeper.etcd.WhiteListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhenbao.zhou on 16/6/8.
 */
@RequestMapping("/monitor/")
@Controller
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

    @RequestMapping("showEtcdGroupList.j")
    public @ResponseBody
    List<Group> showEtcdGroupList() throws Exception {
        return monitorGroupInEtcdKeeper.getGroupList();
    }

    @RequestMapping("showFileGroupList.j")
    public @ResponseBody
    List<Group> showFileGroupList() throws Exception {
        return monitorConfigInFileService.getGroupList();
    }

    @RequestMapping("reloadEtcdConfig.j")
    public @ResponseBody String reloadEtcdConfig() throws Exception {
        etcdResultContainer.refreshJob();
        return "reload reloadEtcdConfig success";
    }

    @RequestMapping("showWhiteList.j")
    public ModelAndView showWhiteList() {
        List<Filter> filterList = whiteListService.findAllFilterList();
        ModelAndView mv = new ModelAndView("whitelist");
        mv.addObject("filterList", filterList);
        return mv;
    }
}
