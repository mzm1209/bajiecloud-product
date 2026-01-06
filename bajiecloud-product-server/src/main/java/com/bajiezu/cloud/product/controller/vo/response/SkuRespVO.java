package com.bajiezu.cloud.product.controller.vo.response;

import com.bajiezu.cloud.product.dto.PropertyVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "商品 SKU 响应")
@Data
public class SkuRespVO {

    @Schema(description = "商品 SKU ID")
    private Long id;

    @Schema(description = "商品 SPU 名称")
    private String name;

    @Schema(description = "日租金")
    private Long dailyRent;

    @Schema(description = "商品 SPU 属性")
    private List<PropertyVO> properties;
}
