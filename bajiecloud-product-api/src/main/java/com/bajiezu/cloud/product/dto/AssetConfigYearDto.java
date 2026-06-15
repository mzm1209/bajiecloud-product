package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "资产残值年度配置")
@Data
public class AssetConfigYearDto {

    @Schema(description = "使用年度(1-3)")
    private Integer useYear;

    @Schema(description = "总价上限系数")
    private BigDecimal totalPriceUpperCoefficient;

    @Schema(description = "总价下限系数")
    private BigDecimal totalPriceLowerCoefficient;

    @Schema(description = "年初值，金额*10000")
    private Long yearBeginValue;

    @Schema(description = "年度折旧金额，金额*10000")
    private Long yearDepreciationAmount;

    @Schema(description = "年末残值，金额*10000")
    private Long yearEndResidualValue;

    @Schema(description = "总价上限，金额*10000")
    private Long totalPriceUpperLimit;

    @Schema(description = "总价下限，金额*10000")
    private Long totalPriceLowerLimit;
}
