package com.bajiezu.cloud.product.dal.dto;

import lombok.Data;

@Data
public class ShelvesStatisticCountDTO {

    /**
     * 0:待上架 1:已上架 2:已下架
     */
    private Integer shelvesStatus;

    private Integer count;
}
