package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ValueAddedProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 增值服务关联的商品 Mapper 接口
 */
@Mapper
public interface ValueAddedProductMapper extends BaseMapper<ValueAddedProduct> {
    
    void batchInsert(@Param("valueAddedProducts") List<ValueAddedProduct> valueAddedProducts);

    List<Long> queryMarketingProductSkuIdsByValueAddedId(@Param("valueAddedId") Long valueAddedId);

    void logicDelByValueAddedIdAndMarketingProductSkuIds(@Param("valueAddedId") Long valueAddedId,
                                                         @Param("marketingProductSkuIds") List<Long> marketingProductSkuIds,
                                                         @Param("updateBy") Long updateBy,
                                                         @Param("updateTime") Date updateTime);

    void logicDelByValueAddedId(@Param("valueAddedId") Long valueAddedId, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);
}