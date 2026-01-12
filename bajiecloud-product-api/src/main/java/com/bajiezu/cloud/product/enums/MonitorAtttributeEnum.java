package com.bajiezu.cloud.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 监管属性枚举
 */
@Getter
@AllArgsConstructor
public enum MonitorAtttributeEnum {

    OVERSIGHT(0, "监管"),
    WITHOUT_OVERSIGHT(1, "非监管");

    private static final Map<Integer, MonitorAtttributeEnum> cache;

    static {
        cache = new HashMap<>();
        for (MonitorAtttributeEnum item : MonitorAtttributeEnum.values()) {
            cache.put(item.getValue(), item);
        }
    }

    private final Integer value;
    private final String desc;

    public static MonitorAtttributeEnum get(Integer value) {
        return cache.get(value);
    }

}