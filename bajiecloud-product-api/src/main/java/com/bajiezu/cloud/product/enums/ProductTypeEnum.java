package com.bajiezu.cloud.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ProductTypeEnum {

    RENTAL_PRODUCT(1, "租赁商品"),
    PRODUCT_FOR_SALE(2, "售卖商品"),
    RECYCLED_PRODUCT(3, "回收商品"),
    PHYSICAL_PRODUCT(4, "实物商品"),
    VIRTUAL_PRODUCT(5, "虚拟商品");

    private static final Map<Integer, ProductTypeEnum> cache;

    static {
        cache = new HashMap<>();
        for (ProductTypeEnum item : ProductTypeEnum.values()) {
            cache.put(item.getValue(), item);
        }
    }

    private final Integer value;
    private final String desc;

    public static ProductTypeEnum get(Integer value) {
        return cache.get(value);
    }

}