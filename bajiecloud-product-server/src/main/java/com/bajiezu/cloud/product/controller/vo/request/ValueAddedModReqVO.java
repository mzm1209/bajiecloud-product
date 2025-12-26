package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "增值服务修改参数")
@Data
public class ValueAddedModReqVO extends ValueAddedAddReqVO{

    @Schema(description = "增值服务id")
    private Long id;
}
