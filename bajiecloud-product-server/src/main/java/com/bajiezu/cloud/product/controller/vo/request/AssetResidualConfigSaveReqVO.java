package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.product.controller.vo.response.AssetResidualMonthConfigVO;
import com.bajiezu.cloud.product.controller.vo.response.AssetResidualYearConfigVO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AssetResidualConfigSaveReqVO {

    @NotNull
    private Long standardSpuId;

    @NotNull
    private Long standardProductSkuId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal officialPrice;

    @NotNull
    private Integer depreciationRuleType;

    @NotNull
    private Integer depreciationRuleSubType;

    private String remark;

    @NotEmpty
    private List<AssetResidualYearConfigVO> yearConfigs;

    @NotEmpty
    private List<AssetResidualMonthConfigVO> monthConfigs;
}
