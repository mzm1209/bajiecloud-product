package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 增值服务关联的商品 实体类
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("value_added_product")
public class ValueAddedProduct extends BaseDO {
    /**
     * 服务ID (关联表主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 增值服务ID (关联value_added表)
     */
    private Long valueAddedId;

    /**
     * 营销商品sku表ID (关联其他商品表)
     */
    private Long marketingProductSkuId;
}