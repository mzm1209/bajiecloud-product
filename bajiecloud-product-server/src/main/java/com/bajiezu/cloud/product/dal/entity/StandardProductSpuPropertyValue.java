package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("standard_product_spu_property_value")
public class StandardProductSpuPropertyValue extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long standardSpuId;
    private Long spuPropertyId;
    private Long productPropertyValueId;
    private String propertyValue;
    private String picUrl;
    private String marketingCornerText;
    private Integer sort;
    @TableField(exist = false)
    private Long productPropertyId;
}
