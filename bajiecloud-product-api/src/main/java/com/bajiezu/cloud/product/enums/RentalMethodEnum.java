package com.bajiezu.cloud.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum RentalMethodEnum {

    RENT_AND_RETURN(1, "租完归还"),
    FLEXIBLE_RENT(2, "灵活租");

    private static final Map<Integer, RentalMethodEnum> CACHE = new HashMap<>();

    static {
        for (RentalMethodEnum item : RentalMethodEnum.values()) {
            CACHE.put(item.getValue(), item);
        }
    }

    private final Integer value;
    private final String desc;

    public static RentalMethodEnum get(Integer value) {
        return CACHE.get(value);
    }
}
