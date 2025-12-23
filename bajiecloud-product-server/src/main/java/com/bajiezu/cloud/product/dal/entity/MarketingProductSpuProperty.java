package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 营销商品SPU属性表 实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("marketing_product_spu_property")
public class MarketingProductSpuProperty extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 营销商品spu表主键ID
     */
    private Long spuId;

    /**
     * 商品属性表ID
     */
    private Long productPropertyId;

    /**
     * 顺序
     */
    private Integer sort;
}