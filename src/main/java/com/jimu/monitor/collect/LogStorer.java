package com.jimu.monitor.collect;

import com.jimu.monitor.collect.bean.GroupMetric;
import com.jimu.monitor.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 这是一个测试的程序. 调试使用的
 * Created by zhenbao.zhou on 16/5/27.
 */
@Slf4j
public class LogStorer implements Storer {
    @Override public void write(GroupMetric groupMetric) {
      log.info("group metric:  {}", JsonUtils.writeValueAsString(groupMetric));
    }
}
