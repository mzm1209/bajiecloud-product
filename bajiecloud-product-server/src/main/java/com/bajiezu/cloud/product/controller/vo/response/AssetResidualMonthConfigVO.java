package com.bajiezu.cloud.product.controller.vo.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetResidualMonthConfigVO {

    private Integer useYear;
    private Integer useMonth;
    private Integer globalMonth;
    private BigDecimal depreciationRuleValue;
    private Long beginValue;
    private Long depreciationAmount;
    private Long residualValue;
    private Long accumulatedDepreciationAmount;
    private Long currentPurchaseAmount;
}
