package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "标准商品修改请求参数")
@Data
public class StandardProductModReqVO extends StandardProductAddReqVO {

    @Schema(description = "标准商品ID", example = "1")
    @NotNull(message = "标准商品ID不能为空")
    private Long id;
}
