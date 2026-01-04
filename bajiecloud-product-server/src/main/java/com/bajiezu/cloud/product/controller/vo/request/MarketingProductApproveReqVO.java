package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "营销商品审核请求参数")
@Data
public class MarketingProductApproveReqVO {

    @Schema(description = "营销商品ID")
    private Long id;

    @Schema(description = "审批状态 0:未审批 1:审批通过 2:审批拒绝")
    private Integer approveStatus;

    @Schema(description = "审核备注")
    private String approvalRemark;
}
