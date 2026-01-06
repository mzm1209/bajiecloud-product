package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "属性")
@Data
public class PropertyVO {

    @Schema(description = "属性ID")
    private Long propertyId;

    @Schema(description = "属性名称")
    private String propertyName;

    @Schema(description = "属性值")
    private List<PropertyValueVO> propertyValues;
}
