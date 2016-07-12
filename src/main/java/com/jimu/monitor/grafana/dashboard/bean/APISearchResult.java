package com.jimu.monitor.grafana.dashboard.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * grafana api search的结果.
 */
@Getter
@Setter
@NoArgsConstructor
public class APISearchResult implements Serializable {

    private static final long serialVersionUID = -8244829460194948621L;

    int id;
    String title;
    String uri;
    String type;
    List tags;
    boolean isStarred;
}
