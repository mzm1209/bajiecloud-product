package com.bajiezu.cloud.product.controller.vo.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssetPricingConfigSaveReqVO {
    @NotNull private Long standardSpuId;
    @NotNull private Long standardProductSkuId;
    @NotNull private Long partnerId;
    @NotEmpty @Valid private List<AssetPricingItemSaveReqVO> configs;
}
