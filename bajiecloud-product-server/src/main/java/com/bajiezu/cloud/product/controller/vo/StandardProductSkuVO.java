package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "标准商品SKU")
public class StandardProductSkuVO {
    private Long id;
    private Integer stock;
    private List<SkuPropertyValueVO> propertyValues;
    private String skuCode;
}
