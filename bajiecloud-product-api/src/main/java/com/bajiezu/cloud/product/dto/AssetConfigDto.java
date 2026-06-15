package com.bajiezu.cloud.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "资产配置信息（残值+定价）")
@Data
public class AssetConfigDto {

    @Schema(description = "标准商品SPU ID")
    private Long standardSpuId;

    @Schema(description = "标准商品SKU ID")
    private Long standardProductSkuId;

    @Schema(description = "合作商ID")
    private Long partnerId;

    @Schema(description = "官方定价，金额*10000")
    private Long officialPrice;

    @Schema(description = "折旧规则类型")
    private Integer depreciationRuleType;

    @Schema(description = "折旧规则子类型")
    private Integer depreciationRuleSubType;

    @Schema(description = "年度残值配置列表（3年）")
    private List<AssetConfigYearDto> yearConfigs;

    @Schema(description = "月度残值配置列表（36个月）")
    private List<AssetConfigMonthDto> monthConfigs;

    @Schema(description = "定价配置列表（leaseMode*useYear维度）")
    private List<AssetConfigPricingDto> pricingConfigs;
}
