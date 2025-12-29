package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "快递模板编辑参数")
@Data
public class ExpressTemplateModReqVO extends ExpressTemplateAddReqVO {

    @Schema(description = "模板id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "快递模版id不能为空")
    private Long id;
}
