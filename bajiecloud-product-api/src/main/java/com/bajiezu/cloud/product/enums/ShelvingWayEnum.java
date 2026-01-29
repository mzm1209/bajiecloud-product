package com.bajiezu.cloud.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ShelvingWayEnum {

    AUTO_SHELVES(1, "自动上架"),
    MANUAL_SHELVES(2, "手动上架"),
    APPOINT_SHELVES(3, "预约上架");



    private static final Map<Integer, ShelvingWayEnum> cache;

    static {
        cache = new HashMap<>();
        for (ShelvingWayEnum item : ShelvingWayEnum.values()) {
            cache.put(item.getValue(), item);
        }
    }

    private final Integer value;
    private final String desc;

    public static ShelvingWayEnum get(Integer value) {
        return cache.get(value);
    }

}