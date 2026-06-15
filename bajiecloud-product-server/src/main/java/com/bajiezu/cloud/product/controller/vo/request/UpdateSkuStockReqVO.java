package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "更新SKU库存请求参数")
@Data
public class UpdateSkuStockReqVO {

    @Schema(description = "SKU ID")
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @Schema(description = "租赁方式：1-租完归还，2-灵活租")
    @NotNull(message = "租赁方式不能为空")
    private Integer rentalMethod;

    @Schema(description = "租期，单位月：如3、6、12")
    @NotNull(message = "租期不能为空")
    private Integer rentalPeriodMonth;

    @Schema(description = "库存数量")
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负数")
    private Integer stock;
}
