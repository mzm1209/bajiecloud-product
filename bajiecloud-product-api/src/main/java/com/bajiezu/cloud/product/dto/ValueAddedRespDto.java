package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "增值服务响应参数")
@Data
public class ValueAddedRespDto {

    @Schema(description = "增值服务ID")
    private Long id;

    @Schema(description = "增值服务编码")
    private String code;

    @Schema(description = "增值服务名称")
    private String name;

    @Schema(description = "增值服务状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "增值服务销售价格")
    private Long salePrice;

    @Schema(description = "续租售价")
    private Long renewalPrice;

    @Schema(description = "增值服务划线价格")
    private Long strikethroughPrice;

    @Schema(description = "是否删除 0:否 1:是")
    private Integer isDeleted;
}
