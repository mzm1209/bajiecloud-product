package com.bajiezu.cloud.product.controller.vo.request;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 品牌管理 - 删除VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PBDelReqVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "id不能为空")
    private Long id;

    public void validateParam() {
        Preconditions.checkArgument(id != null, "id不能为空");
    }
}
