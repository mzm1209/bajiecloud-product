package com.bajiezu.cloud.product.enums;

import com.bajiezu.cloud.common.web.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode PRODUCT_BRAND_NOT_EXIST = new ErrorCode(1_002_001_001, "品牌不存在");
    ErrorCode PRODUCT_MARKETING_CATEGORY_NOT_EXIST = new ErrorCode(1_002_001_002, "营销分类不存在");
    ErrorCode PRODUCT_PROPERTY_VALUE_NOT_EXIST = new ErrorCode(1_002_001_003, "属性值不存在");
    ErrorCode PRODUCT_TAG_NOT_EXIST = new ErrorCode(1_002_001_004, "商品标签不存在");
}
