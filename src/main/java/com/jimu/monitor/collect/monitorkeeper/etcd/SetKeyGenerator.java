package com.jimu.monitor.collect.monitorkeeper.etcd;

/**
 * Created by zhenbao.zhou on 16/7/1.
 */
public class SetKeyGenerator {

    private final static String JOINER = "___";

    public static String gen(String env, String app) {
        return env + JOINER + app;
    }
}
