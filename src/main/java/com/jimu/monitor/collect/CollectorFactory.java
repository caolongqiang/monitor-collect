package com.jimu.monitor.collect;

import com.jimu.monitor.collect.bean.Domain;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.exception.MonitorException;

/**
 * Created by zhenbao.zhou on 16/5/26.
 */
class CollectorFactory {
    public static Collector of(Domain domain) {
        if (domain.getType() == Group.Type.HTTP) {
            return new HttpCollector(domain);
        }

        throw new MonitorException("Collector暂时不支持别的domainType:" + domain.getType());
    }

}
