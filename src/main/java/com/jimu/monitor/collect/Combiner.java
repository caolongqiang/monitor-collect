package com.jimu.monitor.collect;

import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.bean.GroupMetric;
import com.jimu.monitor.collect.bean.Packet;

import java.util.List;

/**
 * 合并各个机器的结果
 * Created by zhenbao.zhou on 16/5/25.
 */
public interface Combiner {

    GroupMetric combine(Group group, List<Packet> packetList);
}
