package com.baijiazu.cloud.product.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductBrandStatusEnum {

    ENABLE(1, "启用"),

    DISABLE(0, "停用");

    private final Integer status;

    private final String desc;
}
