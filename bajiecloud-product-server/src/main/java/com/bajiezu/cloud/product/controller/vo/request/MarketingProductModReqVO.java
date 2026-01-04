package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "营销商品修改请求VO")
@Data
public class MarketingProductModReqVO extends MarketingProductAddReqVO {

    @Schema(description = "营销商品ID")
    private Long id;
}
