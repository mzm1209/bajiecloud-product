package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "更新SKU库存请求参数")
@Data
public class UpdateSkuStockReqVO {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "库存数量")
    private Integer stock;
}
