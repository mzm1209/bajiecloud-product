package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 营销商品SPU属性值表 实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("marketing_product_spu_property_value")
public class MarketingProductSpuPropertyValue extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 营销商品spu属性表主键ID
     */
    private Long spuPropertyId;

    /**
     * 属性值表主键ID
     */
    private Long productPropertyValueId;

    /**
     * 属性值
     */
    private String propertyValue;

    /**
     * 属性图片地址
     */
    private String picUrl;

    /**
     * 顺序
     */
    private Integer sort;

    private Long marketingSpuId;

    @TableField(exist = false)
    private Long productPropertyId;

    @TableField(exist = false)
    private String unqKey;
}