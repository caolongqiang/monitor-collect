package com.jimu.monitor.collect.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 需要被抓取的机器 Created by yue.liu on 16/5/22.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Domain implements Serializable {

    private static final long serialVersionUID = 6778251799931369141L;

    // 地址
    private String host;

    // url地址
    private String url;

    // 默认为http, 这个属性从group集成过来
    private Group.Type type;

    public Domain(String host, String url) {
        this.host = host;
        this.url = url;
    }
}
