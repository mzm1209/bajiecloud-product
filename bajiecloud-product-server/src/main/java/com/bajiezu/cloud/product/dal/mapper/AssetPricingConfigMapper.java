package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.AssetPricingConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface AssetPricingConfigMapper extends BaseMapper<AssetPricingConfig> {
    List<AssetPricingConfig> selectBySkuId(@Param("skuId") Long skuId, @Param("partnerId") Long partnerId);
    void logicDelBySkuAndKeys(@Param("skuId") Long skuId, @Param("partnerId") Long partnerId, @Param("leaseMode") Integer leaseMode, @Param("useYear") Integer useYear, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);
}
