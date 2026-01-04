package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
public class SkuPropertyValueVO {

    @Schema(description = "属性id")
    private Long propertyId;

    @Schema(description = "属性值id")
    private Long propertyValueId;

    @Schema(description = "属性值")
    private String propertyValue;
}
