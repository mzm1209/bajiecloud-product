package com.bajiezu.cloud.product.controller.vo.request;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Schema(description = "管理后台 - 营销分类管理 - 编辑VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PMCModReqVO {

    @Schema(description = "营销类目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "营销类目ID不能为空")
    private Long id;

    @Schema(description = "营销类目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "手机数码")
    @NotBlank(message = "营销类目名称不能为空")
    private String name;

    @Schema(description = "上级营销类目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "上级营销类目ID不能为空")
    private Long parentId = 0L;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "分组描述", example = "手机及相关数码产品分类")
    private String remark;
}
