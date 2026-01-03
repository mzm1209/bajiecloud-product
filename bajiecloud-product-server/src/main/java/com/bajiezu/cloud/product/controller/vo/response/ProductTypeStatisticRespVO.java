package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商品类型对应的商品数量统计信息")
public class ProductTypeStatisticRespVO {

    @Schema(description = "租赁商品数量")
    private Integer rentalProductCount = 0;

    @Schema(description = "售卖商品数量")
    private Integer productForSaleCount = 0;

    @Schema(description = "回收商品数量")
    private Integer recycledProductCount = 0;

    @Schema(description = "实物商品数量")
    private Integer physicalProductCount = 0;

    @Schema(description = "虚拟商品数量")
    private Integer virtualProductCount = 0;
}
