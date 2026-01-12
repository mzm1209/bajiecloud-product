package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "SPU信息")
@Data
public class SpuRespDto {

    @Schema(description = "SPU ID")
    private Long id;

    @Schema(description = "SPU 名称")
    private String name;

    @Schema(description = "SPU 编码")
    private String code;

    @Schema(description = "商品成色")
    private Integer productCondition;
    private String productConditionDesc;

    @Schema(description = "监管属性")
    private Integer monitorAttribute;
    private String monitorAttributeDesc;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "经营类目名称")
    private String businessCategoryName;

    @Schema(description = "营销类目名称")
    private String marketingCategoryName;
}
