package com.jimu.monitor.collect;

import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.exception.MonitorException;

/**
 * Created by zhenbao.zhou on 16/5/27.
 */
public class CombinerFactory {
    public static Combiner of(Group group) {
        if (group.getType() == Group.Type.HTTP) {
            return new HttpCombiner();
        }

        throw new MonitorException("Combiner暂时不支持别的domainType:" + group.getType());
    }
}
