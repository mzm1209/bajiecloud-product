package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "增值服务SKU信息")
@Data
public class ValueAddedSkuRespVO {

    @Schema(description = "增值服务SKU ID")
    private Long id;

    @Schema(description = "增值服务SKU 名称")
    private String name;

    @Schema(description = "增值服务SKU 主图")
    private String mainPicUrls;

    @Schema(description = "增值服务SKU 状态")
    private Integer status;
}
