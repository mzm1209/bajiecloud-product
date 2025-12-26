package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "商品标签简单信息")
@Data
public class ProductTagSimpleInfoRespVO {

    @Schema(description = "标签id")
    private Long id;

    @Schema(description = "标签名称")
    private String name;
}
