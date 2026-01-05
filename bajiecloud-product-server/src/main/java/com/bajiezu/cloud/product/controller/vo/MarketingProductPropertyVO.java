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

    @Schema(description = "商品属性值")
    private List<MarketingProductPropertyValueVO> propertyValues;
}
