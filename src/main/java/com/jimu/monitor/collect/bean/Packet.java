package com.jimu.monitor.collect.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Map;

/**
 * collector抓取回来的结果 Created by zhenbao.zhou on 16/5/25.
 */
@Builder
public class Packet implements Serializable {

    private static final long serialVersionUID = -3691502457004713258L;

    // packet收集的情况
    public enum STATUS {
        SUCCESS, FAIL
    }

    // 抓取的地址
    @Getter @Setter private Domain domain;

    // 抓取结果状态
    // 0表示成功
    // -1 表示失败
    @Getter @Setter private STATUS status;

    // 抓取耗时
    @Getter @Setter private long duration;

    @Getter private long timestamp = System.currentTimeMillis();

    // 抓取回来的内容
    @Getter @Setter private String content;

    // 根据内容解释之后的结果.
    // notice:这个结果和monitor.jsp的结果相同,并不能直接使用
    @Getter @Setter private Map<String, Double> rawMeasurements;

    public static Packet errorPacket() {
        return Packet.builder().status(STATUS.FAIL).build();
    }
}
