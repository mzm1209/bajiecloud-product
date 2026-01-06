package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "属性值")
@Data
public class PropertyValueVO {

    @Schema(description = "属性值id")
    private Long propertyValueId;

    @Schema(description = "属性值")
    private String propertyValue;
}
