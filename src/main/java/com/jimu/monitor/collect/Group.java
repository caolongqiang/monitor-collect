package com.jimu.monitor.collect;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yue.liu on 16/5/22.
 */
public class Group implements Serializable {

    private static final long serialVersionUID = 8604760624535532915L;
    private String name;
    private List<Domain> domainList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Domain> getDomainList() {
        return domainList;
    }

    public void setDomainList(List<Domain> domainList) {
        this.domainList = domainList;
    }

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", domainList=" + domainList +
                '}';
    }
}
