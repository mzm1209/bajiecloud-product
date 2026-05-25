package com.bajiezu.cloud.product.enums;
import com.bajiezu.cloud.common.web.exception.ErrorCode;
public interface AssetResidualErrorCodeConstants {
    ErrorCode ASSET_RESIDUAL_PARAM_INVALID = new ErrorCode(1_006_001_000, "资产残值配置参数不合法");
    ErrorCode ASSET_RESIDUAL_SPU_SKU_MISMATCH = new ErrorCode(1_006_001_001, "标准SPU与SKU不匹配");
}
