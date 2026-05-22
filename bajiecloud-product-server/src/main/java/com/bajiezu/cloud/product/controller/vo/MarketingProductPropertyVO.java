package com.bajiezu.cloud.product.controller.vo;

import com.bajiezu.cloud.product.controller.MarketingProductPropertyValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "商品属性")
@Data
public class MarketingProductPropertyVO {

    @Schema(description = "商品属性ID")
    private Long propertyId;

    @Schema(description = "商品属性名称")
    private String propertyName;

    @Schema(description = "排序")
    private Integer sort = 0;

    @Schema(description = "是否添加属性图 0-否 1-是")
    private Integer isAddPropertyPic;

    @Schema(description = "是否添加营销角标 0-否 1-是")
    private Integer isAddMarketingCorner;

    @Schema(description = "是否SKU销售属性 0-否 1-是")
    private Integer isSkuProperty;

    @Schema(description = "商品属性值")
    private List<MarketingProductPropertyValueVO> propertyValues;
}
