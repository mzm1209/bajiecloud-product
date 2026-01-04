package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.dto.ValueAddedQuery;
import com.bajiezu.cloud.product.dal.entity.ValueAdded;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 增值服务表 Mapper 接口
 */
@Mapper
public interface ValueAddedMapper extends BaseMapper<ValueAdded> {

    List<ValueAdded> queryIdAndNameByStatus(@Param("status") Integer status);

    List<ValueAdded> selectListByQuery(ValueAddedQuery query);

    Long selectCountByQuery(ValueAddedQuery query);
}