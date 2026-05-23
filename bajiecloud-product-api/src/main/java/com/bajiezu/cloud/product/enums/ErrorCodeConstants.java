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
    ErrorCode VALUE_ADDED_COMPENSATION_LEVEL_LIMIT_REQUIRED = new ErrorCode(1_003_007_003, "赔付等级为限制时，赔付等级限制不能为空");
    ErrorCode VALUE_ADDED_COMPENSATION_RATIO_INVALID = new ErrorCode(1_003_007_004, "平台赔付比例需在0~100之间");
    ErrorCode VALUE_ADDED_NUMBER_INVALID = new ErrorCode(1_003_007_005, "数值字段不能为负数");
    ErrorCode VALUE_ADDED_ACCESS_CONDITION_LIMIT_REQUIRED = new ErrorCode(1_003_007_006, "准入条件为限制时，准入条件限制不能为空");
    ErrorCode VALUE_ADDED_ACCESS_BREACH_AMOUNT_REQUIRED = new ErrorCode(1_003_007_007, "选择违约金额时，违约金额不能为空");
    ErrorCode VALUE_ADDED_ACCESS_BREACH_COUNT_REQUIRED = new ErrorCode(1_003_007_008, "选择违约次数时，违约次数不能为空");
    ErrorCode VALUE_ADDED_COMPENSATION_AMOUNT_REQUIRED = new ErrorCode(1_003_007_009, "赔付标准为金额时，赔付金额不能为空");
    ErrorCode VALUE_ADDED_COMPENSATION_AMOUNT_RATIO_REQUIRED = new ErrorCode(1_003_007_010, "赔付标准为金额时，平台赔付比例不能为空");

    // ========== 营销商品 1-003-008-000 ==========
    ErrorCode MARKETING_PRODUCT_NOT_EXIST = new ErrorCode(1_003_008_001, "营销商品不存在");
    ErrorCode MARKETING_PRODUCT_DELETED = new ErrorCode(1_003_008_002, "营销商品已删除");
    ErrorCode MARKETING_PRODUCT_IS_DRAFT = new ErrorCode(1_003_008_003, "当前商品为草稿状态,暂不能进行审核操作");
    ErrorCode MARKETING_PRODUCT_STATUS_NOT_WAIT_APPROVE = new ErrorCode(1_003_008_004, "商品审批状态不为待审批");
    ErrorCode SKU_ID_IS_NULL = new ErrorCode(1_003_008_005, "SKU ID不能为空");
    ErrorCode ON_SHELVES_NOT_ALLOWED = new ErrorCode(1_003_008_006, "待上架/已下架的商品才能操作上架");
    ErrorCode OFF_SHELVES_NOT_ALLOWED = new ErrorCode(1_003_008_007, "已上架的商品才能操作下架");
    ErrorCode MARKETING_PROPERTY_OUT_OF_STANDARD_SCOPE = new ErrorCode(1_003_008_008, "营销商品属性超出标准商品范围");
    ErrorCode MARKETING_PROPERTY_VALUE_OUT_OF_STANDARD_SCOPE = new ErrorCode(1_003_008_009, "营销商品属性值超出标准商品范围");
    ErrorCode STANDARD_PRODUCT_PROPERTY_SCOPE_EMPTY = new ErrorCode(1_003_008_010, "标准商品属性范围为空");
}
