package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "增值服务金额赔付规则返回参数")
@Data
public class ValueAddedCompensationAmountRuleRespVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "赔付金额上限，单位为毫")
    private Long compensationAmount;

    @Schema(description = "平台赔付比例，0~100")
    private Integer compensationAmountRatio;

    @Schema(description = "排序值，越小越靠前")
    private Integer sortOrder;
}
