package com.bajiezu.cloud.product.dal.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateSkuStockDTO {

    private Long id;

    private Long skuId;

    private Integer rentalMethod;

    private Integer rentalPeriodMonth;

    private Integer stock;

    private Long updateBy;

    private Date updateTime;
}
