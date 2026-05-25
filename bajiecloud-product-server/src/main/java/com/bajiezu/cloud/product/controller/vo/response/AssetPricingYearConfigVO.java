package com.bajiezu.cloud.product.controller.vo.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetPricingYearConfigVO {
    private Integer useYear;
    private BigDecimal deviceValue;
    private BigDecimal annualDepreciationAmount;
    private BigDecimal deviceTotalPriceCoefficient;
    private BigDecimal deviceTotalPrice;
    private BigDecimal totalRentCoefficient;
    private BigDecimal totalRent;
    private BigDecimal monthlyRent;
    private BigDecimal dailyRent;
    private BigDecimal expirationPurchaseAmount;
    private Boolean riskWarning;
    private String riskWarningMsg;
}
