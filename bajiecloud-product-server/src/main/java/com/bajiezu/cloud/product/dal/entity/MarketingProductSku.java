package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 营销商品SKU表 实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("marketing_product_sku")
public class MarketingProductSku extends BaseDO {
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
     * 官网价
     */
    private Long officialPrice;

    /**
     * 商品总价系数
     */
    private BigDecimal totalPriceFactor;

    /**
     * 总租金系数
     */
    private BigDecimal totalRentFactor;

    /**
     * 商品总价
     */
    private Long totalPrice;

    /**
     * 总租金
     */
    private Long totalRent;

    /**
     * 买断金
     */
    private Long buyoutAmount;

    /**
     * 日租金
     */
    private Long dailyRent;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 溢价金
     */
    private Long premium;

    /**
     * 建议零售价
     */
    private Long suggestedRetailPrice;

    /**
     * 划线价
     */
    private Long strikethroughPrice;

    /**
     * 现金使用比率
     */
    private BigDecimal cashUsageRatio;

    /**
     * 积分使用比率
     */
    private BigDecimal pointsUsageRatio;

    /**
     * 积分数量
     */
    private Integer pointsCount;

    /**
     * 现金价格
     */
    private Long cashPrice;

    /**
     * 是否允许下单 0:不允许 1:允许
     */
    private Integer isAllowOrder;

    @TableField(exist = false)
    private String skuCode;

    @TableField(exist = false)
    private String name;
}