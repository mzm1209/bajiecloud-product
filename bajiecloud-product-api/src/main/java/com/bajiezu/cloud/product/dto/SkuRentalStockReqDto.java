package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租期维度库存扣减/回补请求。
 * 按 (skuId + rentalMethod + rentalPeriodMonth) 定位
 * marketing_product_sku_rental_method_property 的具体行。
 */
@Schema(description = "租期库存变更请求")
@Data
public class SkuRentalStockReqDto {

    @Schema(description = "营销SKU ID")
    private Long skuId;

    @Schema(description = "租赁方式：1-租完归还，2-灵活租")
    private Integer rentalMethod;

    @Schema(description = "租期，单位月")
    private Integer rentalPeriodMonth;

    @Schema(description = "变更数量（正整数）")
    private Integer quantity;

    @Schema(description = "关联订单号，用于日志追踪")
    private String orderNo;
}
