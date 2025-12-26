package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Schema(description = "新增商品属性请求VO")
@Data
public class PropertyAddReqVO {

    @Schema(description = "属性名称", example = "颜色")
    @NotBlank(message = "属性名称不能为空")
    private String name;

    @Schema(description = "排序", example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "属性值")
    @NotEmpty(message = "属性值不能为空")
    private Set<String> propertyValues;

}
