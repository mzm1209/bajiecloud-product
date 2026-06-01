package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.MarketingProductSkuRentalMethodProperty;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 营销商品SKU租赁方式租期属性表 Mapper 接口
 */
@Mapper
public interface MarketingProductSkuRentalMethodPropertyMapper extends BaseMapper<MarketingProductSkuRentalMethodProperty> {

    void insertBatch(@Param("list") Collection<MarketingProductSkuRentalMethodProperty> list);

    List<MarketingProductSkuRentalMethodProperty> selectListByMarketingSpuId(@Param("marketingSpuId") Long marketingSpuId);

    List<MarketingProductSkuRentalMethodProperty> selectListByMarketingSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds);

    List<MarketingProductSkuRentalMethodProperty> selectListByMarketingSkuIds(@Param("marketingSkuIds") Collection<Long> marketingSkuIds);

    void logicDeleteByMarketingSpuId(@Param("marketingSpuId") Long marketingSpuId,
                                     @Param("updateBy") Long updateBy,
                                     @Param("updateTime") Date updateTime);

    void logicDeleteByMarketingSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds,
                                      @Param("updateBy") Long updateBy,
                                      @Param("updateTime") Date updateTime);
}
