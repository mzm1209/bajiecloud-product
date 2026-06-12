package com.bajiezu.cloud.product.enums;

import com.bajiezu.cloud.common.web.exception.ErrorCode;

public interface AssetPricingErrorCodeConstants {
    ErrorCode ASSET_PRICING_PARAM_INVALID = new ErrorCode(1_006_002_000, "资产定价配置参数不合法");
    ErrorCode ASSET_PRICING_SPU_SKU_MISMATCH = new ErrorCode(1_006_002_001, "标准SPU与SKU不匹配");
    ErrorCode ASSET_PRICING_TOTAL_PRICE_COEFFICIENT_INVALID = new ErrorCode(1_006_002_002, "设备总价系数必须大于 0，请重新输入");
    ErrorCode ASSET_PRICING_TOTAL_PRICE_INVALID = new ErrorCode(1_006_002_003, "设备总价计算结果为 0 或负数，请检查系数");
    ErrorCode ASSET_PRICING_TOTAL_PRICE_TOO_LOW = new ErrorCode(1_006_002_004, "设备总价低于允许范围");
    ErrorCode ASSET_PRICING_TOTAL_PRICE_TOO_HIGH = new ErrorCode(1_006_002_005, "设备总价超出允许范围");
    ErrorCode ASSET_PRICING_TOTAL_RENT_COEFFICIENT_INVALID = new ErrorCode(1_006_002_006, "总租金系数不能为负数，请重新输入");
    ErrorCode ASSET_PRICING_TOTAL_RENT_INVALID = new ErrorCode(1_006_002_007, "总租金计算结果为负数，请检查系数");
    ErrorCode ASSET_PRICING_MONTHLY_OR_DAILY_RENT_INVALID = new ErrorCode(1_006_002_008, "月租金 / 日租金不能为负数，请检查总租金系数");
    ErrorCode ASSET_PRICING_EXPIRATION_PURCHASE_INVALID = new ErrorCode(1_006_002_009, "到期购买金不能为负数，请检查总价或折旧配置");
    ErrorCode ASSET_PRICING_RESIDUAL_VALUE_REQUIRED = new ErrorCode(1_006_002_010, "未正确配置资产残值信息");
}
