package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "资产定价配置")
@Data
public class AssetConfigPricingDto {

    @Schema(description = "租赁模式：1-购买模式，2-租赁模式")
    private Integer leaseMode;

    @Schema(description = "使用年度(1-3)")
    private Integer useYear;

    @Schema(description = "设备价值，金额*10000")
    private Long deviceValue;

    @Schema(description = "设备总价系数")
    private BigDecimal deviceTotalPriceCoefficient;

    @Schema(description = "设备总价，金额*10000")
    private Long deviceTotalPrice;

    @Schema(description = "总租金系数")
    private BigDecimal totalRentCoefficient;

    @Schema(description = "总租金，金额*10000")
    private Long totalRent;

    @Schema(description = "月租金，金额*10000")
    private Long monthlyRent;

    @Schema(description = "日租金，金额*10000")
    private Long dailyRent;

    @Schema(description = "年度折旧金额，金额*10000")
    private Long annualDepreciationAmount;

    @Schema(description = "期满买断金额，金额*10000")
    private Long expirationPurchaseAmount;
}
