package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "商品属性值简明信息")
@Data
public class PropertyValueSimpleInfoVO {

    @Schema(description = "属性值ID")
    private Long id;

    @Schema(description = "属性值名称")
    private String propertyValue;
}
