package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Schema(description = "增值服务返回参数")
@Data
public class ValueAddedRespVO {

    @Schema(description = "增值服务ID")
    private Long id;

    @Schema(description = "增值服务编码")
    private String code;

    @Schema(description = "增值服务名称")
    private String name;

    @Schema(description = "服务概要")
    private String serviceOverview;

    @Schema(description = "服务内容")
    private String serviceContent;

    @Schema(description = "增值服务销售价格")
    private Long salePrice;

    @Schema(description = "增值服务续租售价")
    private Long renewalPrice;

    @Schema(description = "增值服务划线价格")
    private Long strikethroughPrice;

    @Schema(description = "SKU数量")
    private Integer skuCount;

    @Schema(description = "状态")
    private Integer status;

    private List<String> picUrls;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建人")
    private String creatorName;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "更新人")
    private String updaterName;

    @Schema(description = "增值服务SKU信息")
    private List<ValueAddedSkuRespVO> skuRespVOList;

    private List<Integer> serviceTypes;
    private List<Integer> effectiveChannels;
    private Integer compensationStandard;
    private Integer compensationLevel;
    private List<Integer> compensationLevelLimits;
    private Integer slightCompensationRatio;
    private Integer mediumCompensationRatio;
    private Integer severeCompensationRatio;
    private Integer scrapCompensationRatio;
    @Schema(description = "赔付金额，历史兼容字段；取金额赔付规则第一条")
    private Long compensationAmount;

    @Schema(description = "金额赔付时平台赔付比例，历史兼容字段；取金额赔付规则第一条")
    private Integer compensationAmountRatio;

    @Schema(description = "金额赔付规则列表")
    private List<ValueAddedCompensationAmountRuleRespVO> compensationAmountRules;
    private List<Integer> saleLimits;
    private Integer annualLimitPurchaseCount;
    private Integer monthlyLimitPurchaseCount;
    private Integer dailyLimitPurchaseCount;
    private Integer accessCondition;
    private List<Integer> accessConditionLimits;
    private Long accessConditionBreachAmount;
    private Integer accessConditionBreachCount;
}
