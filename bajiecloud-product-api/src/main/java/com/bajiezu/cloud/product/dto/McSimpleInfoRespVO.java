package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "营销类目简单信息")
@Data
public class McSimpleInfoRespVO {

    @Schema(description = "营销类目id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "营销类目分组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xiaomi")
    private String name;

    @Schema(description = "营销类目分组级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer level;

    @Schema(description = "上级类目id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long parentId;
}
