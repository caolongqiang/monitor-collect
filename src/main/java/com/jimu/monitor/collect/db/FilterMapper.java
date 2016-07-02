package com.jimu.monitor.collect.db;

import org.apache.ibatis.annotations.Param;

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

    /**
     * 查出所有filter
     * @return
     */
    List<Filter> findFilterList(@Param("limit") int limit, @Param("offset")int offset);

    int countFilterList();

    int updateFilter(Filter filter);

    int insertFilter(@Param("app") String app, @Param("env") String env);

}
