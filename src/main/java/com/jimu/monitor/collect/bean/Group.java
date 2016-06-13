package com.jimu.monitor.collect.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yue.liu on 16/5/22.
 */
@Data
public class Group implements Serializable {

    private static final long serialVersionUID = 8604760624535532915L;

    // Group的名字
    private String name;

    // Group所在的部门
    private String department;

    // Group里的所有机器
    private List<Domain> domainList;

    private Type type;

    public enum Type {
        HTTP,
        MYSQL,
        REDIS
    }

    /**
     * groupkey 是这个group的唯一性标志.不能重复
     * @return
     */
    public String groupKey() {
        return department + "." + name;
    }
}
