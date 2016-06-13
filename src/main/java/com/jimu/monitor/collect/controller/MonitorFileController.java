package com.jimu.monitor.collect.controller;

import com.jimu.monitor.collect.MonitorFileAutoLoaderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by zhenbao.zhou on 16/6/8.
 */
@RequestMapping("/monitor/")
@Controller
public class MonitorFileController {

    @Resource
    MonitorFileAutoLoaderService monitorFileAutoLoaderService;

    @RequestMapping("reload.j")
    public @ResponseBody String reload() throws Exception {
        monitorFileAutoLoaderService.reload();
        return "reload success";
    }
}
