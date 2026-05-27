package com.bajiezu.cloud.product.enums;
import com.bajiezu.cloud.common.web.exception.ErrorCode;
public interface AssetResidualErrorCodeConstants {
    ErrorCode ASSET_RESIDUAL_PARAM_INVALID = new ErrorCode(1_006_001_000, "资产残值配置参数不合法");
    ErrorCode ASSET_RESIDUAL_SPU_SKU_MISMATCH = new ErrorCode(1_006_001_001, "标准SPU与SKU不匹配");
    ErrorCode ASSET_RESIDUAL_YEAR_COEFFICIENT_REQUIRED = new ErrorCode(1_006_001_002, "资产残值年配置缺少系数字段，请传 totalPriceUpperCoefficient / totalPriceLowerCoefficient");
    ErrorCode ASSET_RESIDUAL_MONTH_RULE_VALUE_REQUIRED = new ErrorCode(1_006_001_003, "资产残值月配置缺少 depreciationRuleValue");
}
