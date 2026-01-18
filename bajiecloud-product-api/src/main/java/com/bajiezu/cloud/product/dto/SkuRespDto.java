package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "SKU信息")
@Data
public class SkuRespDto {

    @Schema(description = "SKU ID")
    private Long id;

    @Schema(description = "官网价/采购价")
    private Long officialPrice;

    @Schema(description = "商品总价系数")
    private BigDecimal totalPriceFactor;

    @Schema(description = "总租金系数")
    private BigDecimal totalRentFactor;

    @Schema(description = "商品总价")
    private Long totalPrice;

    @Schema(description = "总租金")
    private Long totalRent;

    @Schema(description = "买断金")
    private Long buyoutAmount;

    @Schema(description = "日租金")
    private Long dailyRent;

    @Schema(description = "溢价金")
    private Long premium;

    @Schema(description = "建议零售价")
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

    @Schema(description = "是否允许下单 0:否 1:是")
    private Integer isAllowOrder;

    @Schema(description = "属性信息")
    private List<PropertyVO> properties;

    @Schema(description = "sku对应的spu信息")
    private SpuRespDto spuInfo;

    @Schema(description = "租赁期数")
    private Integer leaseTermCount;

    @Schema(description = "续租期数")
    private Integer renewalTermCount;

    @Schema(description = "合作商ID")
    private Long partnerId;
}
