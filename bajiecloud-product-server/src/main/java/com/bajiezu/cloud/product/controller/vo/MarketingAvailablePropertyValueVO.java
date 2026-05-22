package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "营销商品可选属性值")
public class MarketingAvailablePropertyValueVO {

    @Schema(description = "属性值ID")
    private Long propertyValueId;

    @Schema(description = "属性值")
    private String propertyValue;
}
