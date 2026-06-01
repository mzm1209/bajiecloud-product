package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 营销商品SKU租赁方式租期属性表 实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("marketing_product_sku_rental_method_property")
public class MarketingProductSkuRentalMethodProperty extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 营销商品SPU ID
     */
    private Long marketingSpuId;

    /**
     * 营销商品SKU ID
     */
    private Long marketingSkuId;

    /**
     * 租赁方式：1-租完归还，2-灵活租
     */
    private Integer rentalMethod;

    /**
     * 租期，单位月
     */
    private Integer rentalPeriodMonth;

    /**
     * 总租金，金额按元*10000存储
     */
    private Long totalRent;

    /**
     * 月租金，金额按元*10000存储
     */
    private Long monthlyRent;

    /**
     * 日租金，金额按元*10000存储
     */
    private Long dailyRent;

    /**
     * 到期购买金/买断金，金额按元*10000存储
     */
    private Long buyoutAmount;

    /**
     * 溢价金，金额按元*10000存储
     */
    private Long premium;

    /**
     * 库存
     */
    private Integer stock;
}
