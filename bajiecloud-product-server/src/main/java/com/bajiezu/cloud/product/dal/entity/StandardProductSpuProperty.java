package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 标准商品SPU属性表 实体类
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("standard_product_spu_property")
public class StandardProductSpuProperty extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标准商品spu表主键ID
     */
    private Long spuId;

    /**
     * 商品属性表ID
     */
    private Long productPropertyId;

    /**
     * 属性名
     */
    private String propertyName;

    /**
     * 顺序
     */
    private Integer sort;
}