package com.jimu.monitor.grafana.dashboard.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhenbao.zhou on 16/6/4.
 */
@Setter
@Getter
@Builder
public class RenderDbParam implements Serializable {

    private static final long serialVersionUID = 3936119157532796650L;

    /**
     * Dashboard的名字
     */
    String title;

    /**
     * 公共query的串. 类似于 s.departmentName.projectName
     */
    String domainNameQuery;

    /**
     * 这个配置下,可选的指标名
     */
    List<Option> metricOptions;

    /**
     * 这个服务下,可选的机器名
     */
    List<Option> domainOptions;
}
