package com.bajiezu.cloud.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品成色
 */
@Getter
@AllArgsConstructor
public enum ProductConditionEnum {

    NEW(0, "全新"),
    NOT_NEW(1, "非全新");


    private static final Map<Integer, ProductConditionEnum> cache;

    static {
        cache = new HashMap<>();
        for (ProductConditionEnum item : ProductConditionEnum.values()) {
            cache.put(item.getValue(), item);
        }
    }

    private final Integer value;
    private final String desc;

    public static ProductConditionEnum get(Integer value) {
        return cache.get(value);
    }

}