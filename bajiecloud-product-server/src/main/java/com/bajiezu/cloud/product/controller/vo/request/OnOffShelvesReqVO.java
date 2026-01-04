package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "上架下架请求参数")
@Data
public class OnOffShelvesReqVO {

    @Schema(description = "商品ids")
    @NotEmpty(message = "商品ids不能为空")
    private List<Long> ids;

    @Schema(description = "上下架状态 1:上架 2:下架")
    @NotNull(message = "上下架状态不能为空")
    private Integer shelvesStatus;
}
