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

    List<Long> selectSpuPropertyValueIdsBySkuIds(@Param("skuIds") Collection<Long> skuIds);

    List<MarketingProductSkuPropertyValue> selectListBySkuIds(@Param("skuIds") Collection<Long> skuIds);

    void logicDelBySkuIds(@Param("skuIds") Collection<Long> skuIds, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    void logicDelBySkuIdAndSpuPropertyValueIds(@Param("skuId") Long skuId, @Param("spuPropertyValueIds") Collection<Long> spuPropertyValueIds,
                                               @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);
}