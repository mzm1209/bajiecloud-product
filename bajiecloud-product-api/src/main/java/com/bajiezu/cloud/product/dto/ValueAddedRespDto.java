package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "增值服务响应参数")
@Data
public class ValueAddedRespDto {

    @Schema(description = "增值服务ID")
    private Long id;

    @Schema(description = "增值服务编码")
    private String code;

    @Schema(description = "增值服务名称")
    private String name;

    @Schema(description = "增值服务状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "增值服务销售价格")
    private Long salePrice;

    @Schema(description = "续租售价")
    private Long renewalPrice;

    @Schema(description = "增值服务划线价格")
    private Long strikethroughPrice;

    @Schema(description = "是否删除 0:否 1:是")
    private Integer isDeleted;

    @Schema(description = "合作商ID")
    private Long partnerId;

    private String serviceTypes;
    private String effectiveChannels;
    private Integer compensationStandard;
    private Integer compensationLevel;
    private String compensationLevelLimits;
    private Integer slightCompensationRatio;
    private Integer mediumCompensationRatio;
    private Integer severeCompensationRatio;
    private Integer scrapCompensationRatio;
    @Schema(description = "赔付金额，历史兼容字段；取金额赔付规则第一条")
    private Long compensationAmount;

    @Schema(description = "金额赔付时平台赔付比例，历史兼容字段；取金额赔付规则第一条")
    private Integer compensationAmountRatio;

    @Schema(description = "金额赔付规则列表")
    private List<ValueAddedCompensationAmountRuleRespDto> compensationAmountRules;
    private String saleLimits;
    private Integer annualLimitPurchaseCount;
    private Integer monthlyLimitPurchaseCount;
    private Integer dailyLimitPurchaseCount;
    private Integer accessCondition;
    private String accessConditionLimits;
    private Long accessConditionBreachAmount;
    private Integer accessConditionBreachCount;
}
