package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SKU 在指定「租赁方式 + 租期」维度下的价格与库存。
 * 数据来源：marketing_product_sku_rental_method_property 单行。
 * 金额字段统一按 元*10000 存储。
 */
@Schema(description = "SKU租期价格信息")
@Data
public class SkuRentalPriceRespDto {

    @Schema(description = "租期属性行ID（marketing_product_sku_rental_method_property.id）")
    private Long id;

    @Schema(description = "营销SPU ID")
    private Long marketingSpuId;

    @Schema(description = "营销SKU ID")
    private Long marketingSkuId;

    @Schema(description = "租赁方式：1-租完归还，2-灵活租")
    private Integer rentalMethod;

    @Schema(description = "租期，单位月")
    private Integer rentalPeriodMonth;

    @Schema(description = "总租金，金额*10000")
    private Long totalRent;

    @Schema(description = "月租金，金额*10000")
    private Long monthlyRent;

    @Schema(description = "日租金，金额*10000")
    private Long dailyRent;

    @Schema(description = "到期购买金/买断金，金额*10000")
    private Long buyoutAmount;

    @Schema(description = "溢价金，金额*10000")
    private Long premium;

    @Schema(description = "库存")
    private Integer stock;
}
