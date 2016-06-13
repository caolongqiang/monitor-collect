package com.jimu.monitor.collect.bean;

import com.google.common.collect.Table;
import lombok.*;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by zhenbao.zhou on 16/5/25.
 * 合并之后的结果. 这里的指标都是处理过之后, 用于直接存储
 */
@Getter @Setter @Builder
public class GroupMetric implements Serializable {

    private static final long serialVersionUID = -2480349285216676785L;

    private Group group;

    private List<Packet> packetList;

    // group合并解释之后的结果
    private Map<String, Double> groupMeasurements;

    // 单台机器的具体指标. table的格式为
    // domainName, 指标名字, 指标值
    private Table<String, String, Double> domainMeasurements;


}
