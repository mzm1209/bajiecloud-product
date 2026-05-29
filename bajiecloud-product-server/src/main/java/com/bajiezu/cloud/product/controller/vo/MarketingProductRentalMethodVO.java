package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "营销商品租赁方式配置")
@Data
public class MarketingProductRentalMethodVO {

    @Schema(description = "租赁方式：1-租完归还，2-灵活租")
    private Integer rentalMethod;

    @Schema(description = "租赁方式名称")
    private String rentalMethodName;

    @Schema(description = "租期，单位月，可选3、6、12")
    private List<Integer> rentalPeriods;
}
