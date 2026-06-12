package com.bajiezu.cloud.product.controller.vo.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetResidualYearConfigVO {

    private Integer useYear;
    private BigDecimal totalPriceUpperCoefficient;
    private BigDecimal totalPriceLowerCoefficient;
    private BigDecimal yearBeginValue;
    private BigDecimal yearDepreciationAmount;
    private BigDecimal yearEndResidualValue;
    private BigDecimal totalPriceUpperLimit;
    private BigDecimal totalPriceLowerLimit;
}
