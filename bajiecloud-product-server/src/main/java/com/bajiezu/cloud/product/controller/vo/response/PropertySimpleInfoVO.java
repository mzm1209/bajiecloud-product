package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "属性简明信息")
@Data
public class PropertySimpleInfoVO {

    @Schema(description = "属性id")
    private Long id;

    @Schema(description = "属性名称")
    private String name;

    @Schema(description = "属性值信息")
    private List<PropertyValueSimpleInfoVO> propertyValues;
}
