package com.bajiezu.cloud.product.dal.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateSkuStockDTO {

    private Long id;

    private Integer stock;

    private Long updateBy;

    private Date updateTime;
}
