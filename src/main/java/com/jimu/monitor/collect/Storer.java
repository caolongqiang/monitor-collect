package com.jimu.monitor.collect;

import com.jimu.monitor.collect.bean.GroupMetric;

/**
 * Created by yue.liu on 16/5/24.
 */
public interface Storer {

    // 存储
    void write(final GroupMetric groupMetric);
}
