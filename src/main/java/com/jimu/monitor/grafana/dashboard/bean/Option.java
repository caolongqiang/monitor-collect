package com.jimu.monitor.grafana.dashboard.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by zhenbao.zhou on 16/6/4.
 */
@Getter
@Setter
public class Option implements Serializable {

    private static final long serialVersionUID = 5831003605591653067L;
    String text;
    String value;
    boolean selected;

    public Option(String text, boolean selected) {
        this.text = text;
        this.value = text;
        this.selected = selected;
    }

    /**
     * 生成一个全选的选项
     * @return 包含All选项的option
     */
    public static Option ALL_OPTION() {
        Option o = new Option("ALL", true);
        o.setValue("$__all");
        return o;
    }
}