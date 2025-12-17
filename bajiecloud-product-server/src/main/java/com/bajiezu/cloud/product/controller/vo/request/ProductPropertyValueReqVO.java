package com.bajiezu.cloud.product.controller.vo.request;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Schema(description = "商品属性值管理请求VO")
@Data
public class ProductPropertyValueReqVO{

    @Schema(description = "属性值ID", example = "1")
    private Long id;

    @Schema(description = "属性项ID", example = "1")
    @NotNull(message = "属性项ID不能为空")
    private Long propertyId;

    @Schema(description = "属性值", example = "红色")
    @NotBlank(message = "属性值不能为空")
    private String propertyValue;

}
