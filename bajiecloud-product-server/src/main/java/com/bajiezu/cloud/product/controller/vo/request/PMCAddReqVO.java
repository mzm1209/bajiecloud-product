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

@Schema(description = "管理后台 - 营销分类管理 - 新增VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PMCAddReqVO {

    @Schema(description = "营销类目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "手机数码")
    @NotBlank(message = "营销类目名称不能为空")
    private String name;

    @Schema(description = "上级营销类目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "上级营销类目ID不能为空")
    private Long parentId = 0L; // 一级分类默认为0

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "层级 1:一级 2:二级 3:三级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "层级不能为空")
    private Integer level;

    @Schema(description = "状态 0:禁用 1:启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "分组描述", example = "手机及相关数码产品分类")
    private String remark;

    public void validateParam() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "营销类目名称不能为空");
        Preconditions.checkArgument(parentId != null, "上级营销类目ID不能为空");
        Preconditions.checkArgument(sort != null, "排序不能为空");
        Preconditions.checkArgument(level != null, "层级不能为空");
        Preconditions.checkArgument(status != null, "状态不能为空");

        if (level != null && level == 1) {
            // 一级分类parentId默认设为0
            if (parentId == null) {
                parentId = 0L;
            }
        }
    }
}

