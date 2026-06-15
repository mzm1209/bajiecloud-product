package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "资产残值月度配置")
@Data
public class AssetConfigMonthDto {

    @Schema(description = "使用年度(1-3)")
    private Integer useYear;

    @Schema(description = "使用月份(1-12)")
    private Integer useMonth;

    @Schema(description = "全局月数(1-36)")
    private Integer globalMonth;

    @Schema(description = "折旧规则值")
    private BigDecimal depreciationRuleValue;

    @Schema(description = "期初值，金额*10000")
    private Long beginValue;

    @Schema(description = "折旧金额，金额*10000")
    private Long depreciationAmount;

    @Schema(description = "残值，金额*10000")
    private Long residualValue;

    @Schema(description = "累积折旧金额，金额*10000")
    private Long accumulatedDepreciationAmount;

    @Schema(description = "当期买断金额，金额*10000")
    private Long currentPurchaseAmount;
}
