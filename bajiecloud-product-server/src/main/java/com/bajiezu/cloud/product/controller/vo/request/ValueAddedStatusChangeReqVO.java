package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "增值服务状态修改请求参数")
@Data
public class ValueAddedStatusChangeReqVO {

    @Schema(description = "增值服务id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "增值服务id不能为空")
    private Long id;

    @Schema(description = "状态 0:禁用 1:启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;
}
