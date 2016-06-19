package com.jimu.monitor.collect.controller;

import com.jimu.monitor.collect.MonitorConfigInFileService;
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
    MonitorConfigInFileService monitorConfigInFileService;

    @RequestMapping("reload.j")
    public @ResponseBody String reload() throws Exception {
        monitorConfigInFileService.reload();
        return "reload success";
    }
}
