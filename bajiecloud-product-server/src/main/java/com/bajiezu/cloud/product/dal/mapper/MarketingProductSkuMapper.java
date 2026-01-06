package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.dto.IdAndCountDTO;
import com.bajiezu.cloud.product.dal.dto.IdAndPriceDTO;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 营销商品SKU表 Mapper 接口
 */
@Mapper
public interface MarketingProductSkuMapper extends BaseMapper<MarketingProductSku> {

    void logicDelByMarketingSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuIds,
                                   @Param("updateBy") Long updateBy,
                                   @Param("updateTime") Date updateTime);

    void insertBatch(@Param("list") Collection<MarketingProductSku> marketingProductSkus);

    List<IdAndCountDTO> querySkuCountByMarketingProductSpuIds(@Param("marketingSpuIds") Collection<Long> marketingProductSpuIds);

    List<IdAndCountDTO> queryStockByMarketingProductSpuIds(@Param("marketingSpuIds") Collection<Long> marketingProductSpuIds);

    List<IdAndPriceDTO> queryMinAndMaxBuybackPriceByMarketingProductSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuId);

    List<IdAndPriceDTO> queryMinDailyRentPriceByMarketingProductSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuId);

    List<IdAndPriceDTO> queryMinAndMaxOfficialPriceByMarketingProductSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuId);

    List<IdAndPriceDTO> queryMinAndMaxSuggestedRetailPriceByMarketingProductSpuIds(@Param("marketingSpuIds") Collection<Long> marketingSpuId);

    List<MarketingProductSku> selectListByMarketingSpuId(@Param("marketingSpuId") Long marketingSpuId);

    List<MarketingProductSku> selectListByIds(@Param("ids") Collection<Long> ids);
}