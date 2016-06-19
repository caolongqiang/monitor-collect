package com.jimu.monitor.collect;

import com.jimu.monitor.collect.bean.Group;

import java.util.List;

/**
 * Created by zhenbao.zhou on 16/6/19.
 */
public interface MonitorGroupKeeper {

    /**
     * 获取需要抓取的group列表
     * @return
     */
    List<Group> getGroupList();

}
