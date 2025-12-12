package com.bajiezu.cloud.product.controller.vo;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Schema(description = "管理后台 - 品牌管理 -编辑VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PBModReqVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "id不能为空")
    private Long id;

    @Schema(description = "品牌名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xiaomi")
    @NotBlank(message = "品牌名称不能为空")
    @Size(max = 30, message = "品牌名称长度不能超过 30 个字符")
    private String brandName;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "备注", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxx")
    @NotBlank(message = "备注不能为空")
    @Size(max = 100, message = "品牌名称长度不能超过 100 个字符")
    private String remark;

    public void validateParam() {
        Preconditions.checkArgument(id != null, "id不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(brandName), "品牌名称不能为空");
        Preconditions.checkArgument(sort != null, "排序不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(remark), "备注不能为空");
    }
}
