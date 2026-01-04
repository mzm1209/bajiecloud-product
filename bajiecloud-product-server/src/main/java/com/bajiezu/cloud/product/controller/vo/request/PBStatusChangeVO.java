package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.constants.CommonStatusEnum;
import com.bajiezu.cloud.common.web.validation.InEnum;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 品牌管理 - 停用启用VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PBStatusChangeVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "id不能为空")
    private Long id;


    @Schema(description = "status 状态 0: 停用 1: 启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "status不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "修改状态必须是 {value}")
    private Integer status;
}
