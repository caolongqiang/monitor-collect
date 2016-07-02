package com.jimu.monitor.collect.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

/**
 * Created by zhenbao.zhou on 16/6/19.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Filter {
    int id;

    /**
     * 部门名
     */
    String app;

    /**
     * 环境名
     */
    String env;

    /**
     * 状态. 0:表示有效, 其他表示无效
     */
    int status;

}
