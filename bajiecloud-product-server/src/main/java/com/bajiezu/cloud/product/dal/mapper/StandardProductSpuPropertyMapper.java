package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.StandardProductSpuProperty;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface StandardProductSpuPropertyMapper extends BaseMapper<StandardProductSpuProperty> {
    void insertBatch(@Param("list") Collection<StandardProductSpuProperty> list);
    List<StandardProductSpuProperty> selectListByStandardSpuId(@Param("standardSpuId") Long standardSpuId);
    Long selectCountByProductPropertyId(@Param("productPropertyId") Long productPropertyId);
    void logicDelByStandardSpuId(@Param("standardSpuId") Long standardSpuId, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);
}
