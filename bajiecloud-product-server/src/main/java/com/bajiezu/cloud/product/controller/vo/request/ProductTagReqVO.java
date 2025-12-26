package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "商品标签请求参数")
@Data
public class ProductTagReqVO {

    @Schema(description = "显示页面 1:SKU页 2:商品详情页")
    private Integer showPage;
}
