package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 营销分类管理 - 停用启用VO")
@Data
public class ProductTagStatusChangeVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "标签id不能为空")
    private Long id;


    @Schema(description = "status 状态 0: 禁用 1: 启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "status不能为空")
    private Integer status;
}

