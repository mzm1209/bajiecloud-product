package com.bajiezu.cloud.product.dal.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ProductQuery {

    private String code;

    private String name;

    private Integer productType;

    private Set<Long> marketingSpuIds;

    private Integer offset;

    private Integer pageSize;

    private Set<Long> marketingSkuIds;
}
