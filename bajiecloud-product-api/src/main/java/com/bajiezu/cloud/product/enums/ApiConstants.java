package com.bajiezu.cloud.product.enums;


import com.bajiezu.cloud.common.web.cloud.constants.RpcConstants;

/**
 * API 相关的枚举
 */
public class ApiConstants {

    /**
     * 服务名
     * <p>
     * 注意，需要保证和 spring.application.name 保持一致
     */
    public static final String NAME = "product-service";

    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/product";

}
