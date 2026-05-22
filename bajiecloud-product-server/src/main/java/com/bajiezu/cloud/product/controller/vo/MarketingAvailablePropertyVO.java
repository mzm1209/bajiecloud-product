package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "营销商品可选属性")
public class MarketingAvailablePropertyVO {

    @Schema(description = "属性ID")
    private Long propertyId;

    @Schema(description = "属性名称")
    private String propertyName;

    @Schema(description = "可选属性值")
    private List<MarketingAvailablePropertyValueVO> propertyValues;
}
