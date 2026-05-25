package com.bajiezu.cloud.product.controller.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetResidualConfigQueryReqVO {

    @NotNull
    private Long standardSpuId;

    private Long standardProductSkuId;
}
