package com.bajiezu.cloud.product.controller.vo.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetPricingYearConfigVO {
    private Integer useYear;
    private Long deviceValue;
    private Long annualDepreciationAmount;
    private BigDecimal deviceTotalPriceCoefficient;
    private Long deviceTotalPrice;
    private BigDecimal totalRentCoefficient;
    private Long totalRent;
    private Long monthlyRent;
    private Long dailyRent;
    private Long expirationPurchaseAmount;
    private Boolean riskWarning;
    private String riskWarningMsg;
}
