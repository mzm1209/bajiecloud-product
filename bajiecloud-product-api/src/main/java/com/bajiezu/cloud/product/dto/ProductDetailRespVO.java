package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "商品详情返回参数")
@Data
public class ProductDetailRespVO {

    @Schema(description = "SPU/SKU ID")
    private Long id;

    @Schema(description = "商品编码 SPU有编码 SKU无编码")
    private String code;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品类型 具体值参见 ProductTypeEnum枚举类")
    private Integer productType;

    @Schema(description = "商品总价")
    private Long totalPrice;

    @Schema(description = "日租金")
    private Long dailyRent;

    @Schema(description = "商品属性")
    private List<PropertyVO> properties;

    @Schema(description = "租赁方式配置")
    private List<MarketingProductRentalMethodDto> rentalMethods;

    @Schema(description = "商品SPU主图")
    private List<String> mainPics;
}
