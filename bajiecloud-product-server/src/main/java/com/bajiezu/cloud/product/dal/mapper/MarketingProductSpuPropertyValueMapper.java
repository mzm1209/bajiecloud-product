package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.controller.vo.request.ProductListReqVO;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpuPropertyValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

    List<MarketingProductSpuPropertyValue> selectListByMarketingSpuId(@Param("marketingSpuId") Long marketingSpuId);

    List<MarketingProductSpuPropertyValue> selectListByIds(@Param("ids") Collection<Long> ids);

    List<MarketingProductSpuPropertyValue> selectListBySpuIds(@Param("spuIds") Collection<Long> spuIds);

    Set<Long> selectMarketingSpuIdsByPropertyValuesIds(@Param("groups") List<ProductListReqVO.PropertyValueVO> propertyValues,
                                                       @Param("groupSize") Integer groupSize);

    Set<Long> selectMarketingSkuIdsByPropertyValuesIds(@Param("groups") List<ProductListReqVO.PropertyValueVO> propertyValues,
                                                       @Param("groupSize") Integer groupSize);

    void logicDeleteByMarketingSpuIdAndSpuPropertyIds(@Param("marketingSpuId") Long marketingSpuId,
                                                      @Param("spuPropertyIds") Collection<Long> spuPropertyIds,
                                                      @Param("updateBy") Long updateBy,
                                                      @Param("updateTime") Date updateTime);

    List<MarketingProductSpuPropertyValue> selectListByMarketingSpuIdAndSpuPropertyIds(@Param("marketingSpuId") Long marketingSpuId,
                                                                                       @Param("spuPropertyIds") Collection<Long> spuPropertyIds);

    void logicDelByIds(@Param("ids") Collection<Long> ids, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    void updateBatch(@Param("list") Collection<MarketingProductSpuPropertyValue> spuPropertyValues);
}