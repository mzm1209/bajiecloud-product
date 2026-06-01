package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "营销商品SKU租赁方式租期价格库存配置")
@Data
public class MarketingProductSkuRentalMethodPropertyVO {

    @Schema(description = "租赁方式：1-租完归还，2-灵活租")
    private Integer rentalMethod;

    @Schema(description = "租赁方式名称")
    private String rentalMethodName;

    @Schema(description = "租期，单位月")
    private Integer rentalPeriodMonth;

    @Schema(description = "总租金，金额按元*10000存储")
    private Long totalRent;

    @Schema(description = "月租金，金额按元*10000存储")
    private Long monthlyRent;

    @Schema(description = "日租金，金额按元*10000存储")
    private Long dailyRent;

    @Schema(description = "到期购买金/买断金，金额按元*10000存储")
    private Long buyoutAmount;

    @Schema(description = "溢价金，金额按元*10000存储")
    private Long premium;

    @Schema(description = "库存")
    private Integer stock;
}
