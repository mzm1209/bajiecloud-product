package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PBRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "品牌名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xiaomi")
    private String brandName;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "备注", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxx")
    private String remark;

    @Schema(description = "启动禁用状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;
}
