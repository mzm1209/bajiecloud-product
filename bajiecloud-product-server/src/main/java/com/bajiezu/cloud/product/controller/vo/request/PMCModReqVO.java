package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 营销分类管理 - 编辑VO")
@Data
public class PMCModReqVO extends PMCAddReqVO {

    @Schema(description = "营销类目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "营销类目ID不能为空")
    private Long id;
}
