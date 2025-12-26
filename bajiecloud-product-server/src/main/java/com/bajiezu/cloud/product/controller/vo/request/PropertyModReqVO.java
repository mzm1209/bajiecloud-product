package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品属性管理请求VO")
@Data
public class PropertyModReqVO extends PropertyAddReqVO {

    @Schema(description = "属性ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "属性ID不能为空")
    private Long id;

}
