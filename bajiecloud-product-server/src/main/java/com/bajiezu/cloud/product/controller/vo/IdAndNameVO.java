package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "id和名称")
@Data
public class IdAndNameVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "名称")
    private String name;
}
