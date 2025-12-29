package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "标准产品添加请求参数")
@Data
public class StandardProductAddReqVO {

    @Schema(description = "标准商品名称")
    private String name;

    @Schema(description = "品牌id")
    private Long brandId;

    @Schema(description = "经营类目id")
    private Long businessCategoryId;

    @Schema(description = "营销类目id")
    private Long marketingCategoryId;

    @Schema(description = "标准商品属性列表")
    private List<ProductPropertyReqVO> properties;
}
