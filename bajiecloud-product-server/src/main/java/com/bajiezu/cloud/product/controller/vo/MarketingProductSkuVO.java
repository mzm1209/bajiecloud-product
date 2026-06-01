package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "营销商品SKU信息")
@Data
public class MarketingProductSkuVO {

    @Schema(description = "SKU ID")
    private Long id;

    @Schema(description = "官网价/采购价")
    private Long officialPrice;

    @Schema(description = "商品总价系数系数")
    private BigDecimal totalPriceFactor;

    @Schema(description = "总租金系数")
    private BigDecimal totalRentFactor;

    @Schema(description = "商品总价")
    private Long totalPrice;

    @Schema(description = "总租金")
    private Long totalRent;

    @Schema(description = "买断金额")
    private Long buyoutAmount;

    @Schema(description = "日租金")
    private Long dailyRent;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "溢价金")
    private Long premium;

    @Schema(description = "建议售价")
    private Long suggestedRetailPrice;

    @Schema(description = "划线价")
    private Long strikethroughPrice;

    @Schema(description = "现金使用比例")
    private BigDecimal cashUsageRatio;

    @Schema(description = "积分使用比例")
    private BigDecimal pointsUsageRatio;

    @Schema(description = "积分数量")
    private Integer pointsCount;

    @Schema(description = "现金价格")
    private Long cashPrice;

    @Schema(description = "SKU属性")
    private List<SkuPropertyValueVO> propertyValues;

    @Schema(description = "SKU租赁方式租期价格库存配置")
    private List<MarketingProductSkuRentalMethodPropertyVO> rentalMethodProperties;

    private String skuCode;
}
