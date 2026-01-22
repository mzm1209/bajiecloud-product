package com.bajiezu.cloud.product.controller.vo.response;

import com.bajiezu.cloud.product.dto.PropertyVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "商品 SPU 响应")
@Data
public class SpuRespVO {

    @Schema(description = "商品 SPU ID")
    private Long id;

    @Schema(description = "商品 SPU 编码")
    private String code;

    @Schema(description = "商品 SPU 名称")
    private String name;

    @Schema(description = "商品 SPU 主图")
    private List<String> mainPics;

    @Schema(description = "商品 SPU 属性")
    private List<PropertyVO> properties;

    @Schema(description = "SPU下的SKU的最低日租金")
    private Long dailyRent;
}
