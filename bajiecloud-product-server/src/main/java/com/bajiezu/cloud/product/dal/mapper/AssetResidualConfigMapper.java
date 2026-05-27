package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.AssetResidualConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.Date;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetResidualConfigMapper extends BaseMapper<AssetResidualConfig> {

    AssetResidualConfig selectBySkuId(@Param("skuId") Long skuId, @Param("partnerId") Long partnerId);

    void logicDelBySkuId(
            @Param("skuId") Long skuId,
            @Param("partnerId") Long partnerId,
            @Param("updateBy") Long updateBy,
            @Param("updateTime") Date updateTime);
}
