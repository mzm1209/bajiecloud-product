package com.bajiezu.cloud.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ShelvesStatusEnum {

    WAIT_SHELVES(0, "待上架"),
    ON_SHELVES(1, "已上架"),
    OFF_SHELVES(2, "已下架");

    private static final Map<Integer, ShelvesStatusEnum> cache;

    static {
        cache = new HashMap<>();
        for (ShelvesStatusEnum item : ShelvesStatusEnum.values()) {
            cache.put(item.getValue(), item);
        }
    }

    private final Integer value;
    private final String desc;

    public static ShelvesStatusEnum get(Integer value) {
        return cache.get(value);
    }

}