package com.bajiezu.cloud.product.enums;

import com.bajiezu.cloud.common.web.exception.ErrorCode;

public interface ErrorCodeConstants {

    // ========== 品牌 模块 1-003-001-000 ==========
    ErrorCode PRODUCT_BRAND_NOT_EXIST = new ErrorCode(1_003_001_001, "品牌不存在");

    // ========== 营销类目 模块 1-003-002-000 ==========
    ErrorCode PRODUCT_MARKETING_CATEGORY_NOT_EXIST = new ErrorCode(1_003_002_001, "营销类目不存在");
    ErrorCode PRODUCT_MARKETING_CATEGORY_NAME_EXIST = new ErrorCode(1_003_002_002, "营销类目已存在");
    ErrorCode PRODUCT_MARKETING_CATEGORY_DISABLED = new ErrorCode(1_003_002_003, "营销类目已禁用");

    // ========== 商品属性 模块 1-003-003-000 ==========
    ErrorCode PROPERTY_ALREADY_EXIST = new ErrorCode(1_003_003_001, "属性已存在");
    ErrorCode PROPERTY_NOT_EXIST = new ErrorCode(1_003_003_002, "属性不存在");

    // ========== 商品标签 模块 1-003-004-000 ==========
    ErrorCode PRODUCT_TAG_NOT_EXIST = new ErrorCode(1_003_004_004, "商品标签不存在");

    // ========== 快递模版 1-003-005-000 ==========
    ErrorCode EXPRESS_TEMPLATE_NOT_EXIST = new ErrorCode(1_003_005_001, "快递模版不存在");
    ErrorCode EXPRESS_TEMPLATE_DELETED = new ErrorCode(1_003_005_002, "快递模版已删除");

    // ========== 标准商品 1-003-006-000 ==========
    ErrorCode STANDARD_PRODUCT_NOT_EXIST = new ErrorCode(1_003_006_001, "标准商品不存在");
    ErrorCode STANDARD_PRODUCT_DELETED = new ErrorCode(1_003_006_002, "标准商品已删除");

    // ========== 增值服务 1-003-007-000 ==========
    ErrorCode VALUE_ADDED_NOT_EXIST = new ErrorCode(1_003_007_001, "增值服务不存在");
    ErrorCode VALUE_ADDED_DELETED = new ErrorCode(1_003_007_002, "增值服务已删除");
}
