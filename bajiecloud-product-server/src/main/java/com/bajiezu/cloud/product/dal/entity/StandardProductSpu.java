package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 标准商品SPU表 实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("standard_product_spu")
public class StandardProductSpu extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 品牌ID
     */
    private Long productBrandId;

    /**
     * 名称
     */
    private String name;

    /**
     * 经营类目ID
     */
    private Long businessCategoryId;

    /**
     * 营销类目ID
     */
    private Long marketingCategoryId;

    /**
     * 是否为草稿 0:否 1:是
     */
    private Integer isDraft;

    /**
     * 状态 0:启用 1:禁用
     */
    private Integer status;
}