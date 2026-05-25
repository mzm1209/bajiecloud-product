package com.bajiezu.cloud.product.controller.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetPricingConfigQueryReqVO {
    @NotNull
    private Long standardSpuId;
    @NotNull
    private Long standardProductSkuId;
    @NotNull
    private Long partnerId;
}
