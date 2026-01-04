package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "状态变更请求参数")
@Data
public class StatusChangeReqVo {

    @Schema(description = "记录id集合", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "ids不能为空")
    private List<Long> ids;

    @Schema(description = "状态不能为空 0:禁用 1:启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "记录id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;
}
