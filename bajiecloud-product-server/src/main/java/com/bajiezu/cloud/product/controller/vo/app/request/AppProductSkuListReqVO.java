package com.bajiezu.cloud.product.controller.vo.app.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "App端SKU列表请求")
public class AppProductSkuListReqVO {
    @NotNull
    private Long spuId;
}
