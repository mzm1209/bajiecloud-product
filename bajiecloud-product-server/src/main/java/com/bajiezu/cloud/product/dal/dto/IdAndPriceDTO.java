package com.bajiezu.cloud.product.dal.dto;

import lombok.Data;

@Data
public class IdAndPriceDTO {

    private Long id;

    private Long minPrice;

    private Long maxPrice;
}
