package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.MarketingProductSpuRentalMethodProperty;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 营销商品SPU租赁方式属性表 Mapper 接口
 */
@Mapper
public interface MarketingProductSpuRentalMethodPropertyMapper extends BaseMapper<MarketingProductSpuRentalMethodProperty> {

    void insertBatch(@Param("list") Collection<MarketingProductSpuRentalMethodProperty> list);

    List<MarketingProductSpuRentalMethodProperty> selectListByMarketingSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds);

    List<MarketingProductSpuRentalMethodProperty> selectListByMarketingSpuId(@Param("marketingSpuId") Long marketingSpuId);

    void logicDeleteByMarketingSpuId(@Param("marketingSpuId") Long marketingSpuId,
                                     @Param("updateBy") Long updateBy,
                                     @Param("updateTime") Date updateTime);

    void logicDeleteByMarketingSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds,
                                      @Param("updateBy") Long updateBy,
                                      @Param("updateTime") Date updateTime);
}
