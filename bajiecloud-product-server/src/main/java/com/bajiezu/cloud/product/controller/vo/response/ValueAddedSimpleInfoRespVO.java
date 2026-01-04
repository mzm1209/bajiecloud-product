package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "增值服务简单信息")
@Data
public class ValueAddedSimpleInfoRespVO {

    @Schema(description = "增值服务id")
    private Long id;

    @Schema(description = "增值服务名称")
    private String name;
}
