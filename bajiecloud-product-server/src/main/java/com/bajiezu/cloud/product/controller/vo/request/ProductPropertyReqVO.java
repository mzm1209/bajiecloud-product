package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "标准商品属性管理请求VO")
@Data
public class ProductPropertyReqVO {

    @Schema(description = "属性项ID", example = "1")
    private Long propertyId;

    @Schema(description = "属性项名称", example = "颜色")
    private String propertyName;

    @Schema(description = "属性值列表")
    private List<ProductPropertyValueReqVO> propertyValues;
}
