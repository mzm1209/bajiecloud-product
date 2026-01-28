package com.bajiezu.cloud.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ApproveStatusEnum {

    WAIT_APPROVE(0, "待审核"),
    APPROVE_PASS(1, "审核通过"),
    APPROVE_REJECT(2, "审核失败");

    private static final Map<Integer, ApproveStatusEnum> cache;

    static {
        cache = new HashMap<>();
        for (ApproveStatusEnum item : ApproveStatusEnum.values()) {
            cache.put(item.getValue(), item);
        }
    }

    private final Integer value;
    private final String desc;

    public static ApproveStatusEnum get(Integer value) {
        return cache.get(value);
    }

}