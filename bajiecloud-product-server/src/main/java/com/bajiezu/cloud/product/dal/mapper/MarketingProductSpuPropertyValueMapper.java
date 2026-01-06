package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.MarketingProductSpuPropertyValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 营销商品SPU属性值表 Mapper 接口
 */
@Mapper
public interface MarketingProductSpuPropertyValueMapper extends BaseMapper<MarketingProductSpuPropertyValue> {

    void logicDelByMarketingSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds,
                                   @Param("updateBy") Long updateBy,
                                   @Param("updateTime") Date updateTime);

    void insertBatch(@Param("list") Collection<MarketingProductSpuPropertyValue> spuPropertyValues);

    List<MarketingProductSpuPropertyValue> selectListByMarketingProductSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds);
}