package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.MarketingProductSpuProperty;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 营销商品SPU属性表 Mapper 接口
 */
@Mapper
public interface MarketingProductSpuPropertyMapper extends BaseMapper<MarketingProductSpuProperty> {


    void logicDelByMarketingSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds,
                                   @Param("updateBy") Long updateBy,
                                   @Param("updateTime") Date updateTime);

    void insertBatch(@Param("list") Collection<MarketingProductSpuProperty> list);

    List<MarketingProductSpuProperty> selectListByMarketingProductSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds);

    List<MarketingProductSpuProperty> selectListByMarketingSpuId(@Param("marketingSpuId") Long marketingSpuId);

    List<MarketingProductSpuProperty> selectListBySpuIds(@Param("spuIds") Collection<Long> spuIds);

    List<Long> selectProductPropertyIdsByMarketingProductSpuId(@Param("spuId") Long spuId);

    void logicDeleteByMarketingSpuIdAndPropertyIds(@Param("marketingSpuId") Long marketingSpuId,
                                                   @Param("productPropertyIds") Collection<Long> productPropertyIds,
                                                   @Param("updateBy") Long updateBy,
                                                   @Param("updateTime") Date updateTime);

    void updateBatch(@Param("list") Collection<MarketingProductSpuProperty> spuProperties);
}
