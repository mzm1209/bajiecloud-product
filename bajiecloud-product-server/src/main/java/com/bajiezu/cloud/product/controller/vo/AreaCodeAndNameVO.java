package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "地区编码和名称")
@Data
public class AreaCodeAndNameVO {

    @Schema(description = "地区编码")
    private String areaCode;

    @Schema(description = "地区名称")
    private String areaName;

    @Schema(description = "地区对应的邮费")
    private Long shippingCount;
}
