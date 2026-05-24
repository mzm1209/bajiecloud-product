package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("standard_product_sku")
public class StandardProductSku extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long standardSpuId;
    private Long partnerId;
    private Integer stock;
    private String skuCode;
}
