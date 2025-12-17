package com.bajiezu.cloud.product.controller.vo.request;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 营销分类管理 - 删除VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PMCDelReqVO {

    @Schema(description = "营销类目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "营销类目ID不能为空")
    private Long id;

}
