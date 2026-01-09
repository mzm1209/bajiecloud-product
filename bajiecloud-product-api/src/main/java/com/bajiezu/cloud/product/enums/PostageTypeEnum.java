package com.bajiezu.cloud.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮费类型
 */
@Getter
@AllArgsConstructor
public enum PostageTypeEnum {

    FREE_SHIPPING(1, "包邮"),
    SOME_REGIONS_FREE_SHIPPING(2, "除部分地区包邮"),
    SHIPPING_NOT_INCLUDED(3, "不包邮");

    private static final Map<Integer, PostageTypeEnum> cache;

    static {
        cache = new HashMap<>();
        for (PostageTypeEnum item : PostageTypeEnum.values()) {
            cache.put(item.getValue(), item);
        }
    }

    private final Integer value;
    private final String desc;

    public static PostageTypeEnum get(Integer value) {
        return cache.get(value);
    }

}