package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 营销商品SPU租赁方式属性表 实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("marketing_product_spu_rental_method_property")
public class MarketingProductSpuRentalMethodProperty extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 营销商品spu表主键ID
     */
    private Long marketingSpuId;

    /**
     * 租赁方式：1-租完归还，2-灵活租
     */
    private Integer rentalMethod;

    /**
     * 租期，单位月
     */
    private Integer rentalPeriodMonth;
}
