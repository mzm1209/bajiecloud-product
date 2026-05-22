package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("standard_product_spu_property")
public class StandardProductSpuProperty extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long standardSpuId;
    private Long productPropertyId;
    private Integer sort;
    private Integer isAddPropertyPic;
    private Integer isAddMarketingCorner;
    private Integer isSkuProperty;
}
