package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "增值服务金额赔付规则请求参数")
@Data
public class ValueAddedCompensationAmountRuleReqVO {

    @Schema(description = "赔付金额上限，单位为毫")
    private Long compensationAmount;

    @Schema(description = "平台赔付比例，0~100")
    private Integer compensationAmountRatio;
}
