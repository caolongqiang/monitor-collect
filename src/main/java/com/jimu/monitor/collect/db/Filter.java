package com.jimu.monitor.collect.db;

import lombok.Data;

/**
 * Created by zhenbao.zhou on 16/6/19.
 */

@Data
public class Filter {
    int id;

    /**
     * 部门名字
     */
    String department;

    /**
     * 组名
     */
    String groupname;

    /**
     * 状态.  0:表示有效, 其他表示无效
     */
    int status;
}
