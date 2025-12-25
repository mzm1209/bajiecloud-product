package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 营销分类管理 - 新增VO")
@Data
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

    @Schema(description = "分组描述", example = "手机及相关数码产品分类")
    private String remark;

    @Schema(description = "路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2")
    private String path;
}

