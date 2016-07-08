package com.jimu.monitor.collect.monitorkeeper.etcd;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 保存etcd返回回来的信息. 具体的内容
 * @see com.jimu.monitor.collect.monitorkeeper.etcd.EtcdResultContainer
 *
 * refer to http://git.jimubox.com/snippets/23
 * Created by zhenbao.zhou on 16/7/1.
 */
@Getter
@Setter
@ToString
@Slf4j
public class EtcdResult {

    String app;
    String env;
    String gen;
    String name;
    String id;
    String ip;
    String ports;
    String host;
    String app_heck;

}

