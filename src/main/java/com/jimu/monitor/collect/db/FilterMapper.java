package com.jimu.monitor.collect.db;

import java.util.List;

/**
 * Created by zhenbao.zhou on 16/6/19.
 */
@MyBatisRepository
public interface FilterMapper {

    /**
     * 查出所有有效的filter
     * @return
     */
    List<Filter> queryAvailableFilterList();

}
