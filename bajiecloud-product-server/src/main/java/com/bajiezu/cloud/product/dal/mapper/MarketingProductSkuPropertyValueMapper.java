package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.MarketingProductSkuPropertyValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 营销商品SKU关联的属性值表 Mapper 接口
 */
@Mapper
public interface MarketingProductSkuPropertyValueMapper extends BaseMapper<MarketingProductSkuPropertyValue> {

    void logicDelByMarketingSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds,
                                   @Param("updateBy") Long updateBy,
                                   @Param("updateTime") Date updateTime);

    void insertBatch(@Param("list") List<MarketingProductSkuPropertyValue> skuPropertyValues);

    List<MarketingProductSkuPropertyValue> selectListByMarketingSpuId(@Param("marketingSpuId") Long marketingSpuId);
}