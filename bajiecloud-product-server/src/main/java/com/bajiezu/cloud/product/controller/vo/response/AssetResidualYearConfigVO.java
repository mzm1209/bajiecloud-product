package com.bajiezu.cloud.product.controller.vo.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetResidualYearConfigVO {

    private Integer useYear;
    private BigDecimal totalPriceUpperCoefficient;
    private BigDecimal totalPriceLowerCoefficient;
    private Long yearBeginValue;
    private Long yearDepreciationAmount;
    private Long yearEndResidualValue;
    private Long totalPriceUpperLimit;
    private Long totalPriceLowerLimit;
}
