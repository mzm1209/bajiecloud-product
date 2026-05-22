package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标准商品属性值")
public class StandardProductPropertyValueVO {
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
