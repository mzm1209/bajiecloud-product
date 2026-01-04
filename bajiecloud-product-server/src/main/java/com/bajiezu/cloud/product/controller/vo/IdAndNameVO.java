package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "商品标签id和名称")
@Data
public class IdAndNameVO {

    @Schema(description = "标签id")
    private Long tagId;

    @Schema(description = "标签名称")
    private String tagName;
}
