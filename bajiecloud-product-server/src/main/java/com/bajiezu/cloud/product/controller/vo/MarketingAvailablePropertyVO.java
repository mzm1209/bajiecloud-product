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

    @Schema(description = "是否添加属性图 0否1是")
    private Integer isAddPropertyPic;

    @Schema(description = "是否添加营销角标 0否1是")
    private Integer isAddMarketingCorner;

    @Schema(description = "是否SKU销售属性 0否1是")
    private Integer isSkuProperty;

    @Schema(description = "可选属性值")
    private List<MarketingAvailablePropertyValueVO> propertyValues;
}
