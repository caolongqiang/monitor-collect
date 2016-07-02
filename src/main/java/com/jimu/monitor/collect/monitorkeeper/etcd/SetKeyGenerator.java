package com.jimu.monitor.collect.monitorkeeper.etcd;

/**
 * 把app 和 env 联合起来,成为一个group的名字
 * Created by zhenbao.zhou on 16/7/1.
 */
public class SetKeyGenerator {

    private final static String JOINER = "___";

    public static String gen(String env, String app) {
        return env + JOINER + app;
    }
}
