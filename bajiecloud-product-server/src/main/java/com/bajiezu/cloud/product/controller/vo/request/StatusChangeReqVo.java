package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "状态变更请求参数")
@Data
public class StatusChangeReqVo {

    @Schema(description = "id不能为空", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "状态不能为空 0:禁用 1:启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;
}
