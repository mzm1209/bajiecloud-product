package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

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

    @Schema(description = "SPU主图")
    private List<String> mainPicUrls;

    @Schema(description = "上架状态 0:待上架 1:已上架 2:已下架", oneOf = com.bajiezu.cloud.product.enums.ShelvesStatusEnum.class)
    private Integer shelvesStatus;

    @Schema(description = "审批状态 0:待审核 1:审核通过 2:审核失败", oneOf = com.bajiezu.cloud.product.enums.ApproveStatusEnum.class)
    private Integer approvalStatus;
}
