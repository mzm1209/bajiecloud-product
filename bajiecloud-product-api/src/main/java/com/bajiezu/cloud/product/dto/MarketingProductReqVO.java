package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "营销商品请求参数")
@Data
public class MarketingProductReqVO {

    @Schema(description = "营销商品spu")
    private List<Long> ids;

    @Schema(description = "类型 值为SKU/SPU")
    private String type;
}
