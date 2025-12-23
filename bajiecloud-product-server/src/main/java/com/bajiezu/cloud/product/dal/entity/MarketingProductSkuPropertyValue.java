package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 营销商品SKU关联的属性值表 实体类
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("marketing_product_sku_property_value")
public class MarketingProductSkuPropertyValue extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 营销商品sku表主键ID
     */
    private Long marketingProductSkuId;

    /**
     * 营销商品spu属性值表主键ID
     */
    private Long marketingSpuPropertyValueId;

}