package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "营销商品审核请求参数")
@Data
public class MarketingProductApproveReqVO {

    @Schema(description = "营销商品ID")
    @NotNull(message = "营销商品ID不能为空")
    private Long id;

    @Schema(description = "审批状态 0:未审批 1:审批通过 2:审批拒绝")
    @NotNull(message = "审批状态不能为空")
    private Integer approveStatus;

    @Schema(description = "审核备注")
    private String approvalRemark;
}
