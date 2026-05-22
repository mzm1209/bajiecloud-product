package com.bajiezu.cloud.product.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "商品属性值")
@Data
public class MarketingProductPropertyValueVO {

    @Schema(description = "商品属性值id")
    private Long productPropertyValueId;

    @Schema(description = "商品属性值")
    private String value;

    @Schema(description = "排序")
    private Integer sort = 0;

    @Schema(description = "图片")
    private String picUrl;

    @Schema(description = "营销角标文案")
    private String marketingCornerText;

    private String unqKey;
}
