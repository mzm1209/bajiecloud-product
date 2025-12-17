package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.product.dal.entity.ProductPropertyValue;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Schema(description = "商品属性管理请求VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPropertyReqVO {

    @Schema(description = "属性ID", example = "1")
    private Long id;

    @Schema(description = "属性名称", example = "颜色")
    @NotBlank(message = "属性名称不能为空")
    private String name;

    @Schema(description = "排序", example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "属性值")
    @NotEmpty(message = "属性值不能为空")
    private List<ProductPropertyValue> propertyValues;

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;



}
