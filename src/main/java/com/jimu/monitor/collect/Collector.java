package com.jimu.monitor.collect;

import com.google.common.util.concurrent.ListenableFuture;
import com.jimu.monitor.collect.bean.Packet;

import java.util.concurrent.CompletableFuture;

/**
 * 采集单机的指标, 并解释
 * Created by zhenbao.zhou on 16/5/25.
 */
public interface Collector {

    // 抓取某个domain的数据, 并处理
    CompletableFuture<Packet> collect();
}
