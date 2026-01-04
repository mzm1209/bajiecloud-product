package com.bajiezu.cloud.product.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Schema
@Data
public class ValueAddedQuery {

    private String name;

    private Integer status;

    private Date createTimeBegin;

    private Date createTimeEnd;

    private Integer offset;

    private Integer pageSize;
}
