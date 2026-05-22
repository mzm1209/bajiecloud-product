package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.StandardProductSpuPropertyValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface StandardProductSpuPropertyValueMapper extends BaseMapper<StandardProductSpuPropertyValue> {
    void insertBatch(@Param("list") Collection<StandardProductSpuPropertyValue> list);
    List<StandardProductSpuPropertyValue> selectListByStandardSpuId(@Param("standardSpuId") Long standardSpuId);
    void logicDelByStandardSpuId(@Param("standardSpuId") Long standardSpuId, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);
}
