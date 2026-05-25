package com.bajiezu.cloud.product.controller.vo.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetPricingItemSaveReqVO {
    @NotNull private Integer leaseMode;
    @NotNull private Integer useYear;
    @NotNull @DecimalMin("0") private BigDecimal deviceTotalPriceCoefficient;
    @NotNull @DecimalMin("0") private BigDecimal totalRentCoefficient;
    private String remark;
}
