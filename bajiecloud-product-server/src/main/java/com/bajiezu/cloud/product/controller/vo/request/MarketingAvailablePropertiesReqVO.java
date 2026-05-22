package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "营销商品可选属性范围请求VO")
public class MarketingAvailablePropertiesReqVO {

    @NotNull(message = "标准商品ID不能为空")
    @Schema(description = "标准商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long standardProductSpuId;
}
