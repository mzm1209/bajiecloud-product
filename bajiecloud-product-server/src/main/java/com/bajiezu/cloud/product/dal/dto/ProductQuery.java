package com.bajiezu.cloud.product.dal.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductQuery {

    private String code;

    private String name;

    private Integer productType;

    private List<Long> marketingSpuIds;

    private Integer offset;

    private Integer pageSize;
}
