package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品标签管理请求VO")
@Data
public class ProductTagModReqVO extends ProductTagAddReqVO {

    @Schema(description = "标签ID", example = "1")
    private Long id;
}
