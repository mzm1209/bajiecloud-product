package com.bajiezu.cloud.product.enums;


import com.bajiezu.cloud.common.web.cloud.constants.RpcConstants;

/**
 * API 相关的枚举
 */
public class ProductApiConstants {

    /**
     * 服务名
     * <p>
     * 注意，需要保证和 spring.application.name 保持一致
     */
    public static final String NAME = "product-service";

    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/product";

    public static final String SKU = "SKU";
    public static final String SPU = "SPU";
    public static final int THIRTY = 30;

    /*************************  属性名常量   ********************************/
    public static final String PRODUCT_CONDITION = "商品成色";

    public static final String MONITOR_ATTRIBUTE = "监管属性";

    public static final String COLOR = "颜色";

    public static final String BRAND = "品牌";

    public static final String SPECIFICATIONS = "规格";

    public static final String RENTAL_METHOD = "租赁方式";

    public static final String RENTAL_PERIOD  = "租期(天)";

    public static final String RENEWAL_TERM  = "续租租期(天)";

    /*************************  属性值常量   ********************************/
    public static final String RETURN_AFTER_RENTAL  = "租完归还（到期可购买/续租）";
    public static final String FLEXIBLE_RENTAL  = "灵活租（可灵活选择 归还/购买）";
}
