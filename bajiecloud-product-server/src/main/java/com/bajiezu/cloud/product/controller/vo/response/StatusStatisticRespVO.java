package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "商品状态统计信息")
@Data
public class StatusStatisticRespVO {

    @Schema(description = "商品总数")
    private Integer totalCount = 0;

    @Schema(description = "上架商品数")
    private Integer onShelvesCount = 0;

    @Schema(description = "下架商品数")
    private Integer offShelvesCount = 0;

    @Schema(description = "待审核商品数")
    private Integer waitApproveCount = 0;

    @Schema(description = "审核通过商品数")
    private Integer approvePassCount = 0;

    @Schema(description = "审核未通过商品数")
    private Integer approveRejectCount = 0;

    @Schema(description = "草稿商品数")
    private Integer draftCount = 0;
}
