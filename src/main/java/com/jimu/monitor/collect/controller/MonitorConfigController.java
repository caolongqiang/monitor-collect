package com.jimu.monitor.collect.controller;

import com.jimu.monitor.collect.monitorkeeper.MonitorConfigInFileService;
import com.jimu.monitor.collect.monitorkeeper.MonitorGroupInEtcdKeeper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

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

    @RequestMapping("reloadFileConfig.j")
    public @ResponseBody String reloadFileConfig() throws Exception {
        monitorConfigInFileService.reload();
        return "reload success";
    }

    @RequestMapping("reloadDBFilter.j")
    public @ResponseBody String reloadDBFilter() throws Exception {
        //monitorGroupInEtcdKeeper.reloadDBFilter();
        return "reload db filter success";
    }

    @RequestMapping("reloadEtcdConfig.j")
    public @ResponseBody String reloadEtcdConfig() throws Exception {
       // monitorGroupInEtcdKeeper.refresh();
        return "reload reloadEtcdConfig success";
    }
}
