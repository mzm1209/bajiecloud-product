package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "经营类目响应实体")
@Data
public class BusinessCategoryRespVO {

    @Schema(description = "经营类目id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "经营类目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "手机")
    private String name;

    @Schema(description = "经营类目层级 1:一级 依次类推", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer level;

    @Schema(description = "经营类目父级id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long parentId;

    @Schema(description = "子级经营类目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<BusinessCategoryRespVO> children;
}
