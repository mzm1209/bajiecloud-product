package com.bajiezu.cloud.product.controller.vo.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetResidualMonthConfigVO {

    private Integer useYear;
    private Integer useMonth;
    private Integer globalMonth;
    private BigDecimal depreciationRuleValue;
    private BigDecimal beginValue;
    private BigDecimal depreciationAmount;
    private BigDecimal residualValue;
    private BigDecimal accumulatedDepreciationAmount;
    private BigDecimal currentPurchaseAmount;
}
