package com.bajiezu.cloud.product.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Schema(description = "标准产品查询参数")
@Data
public class StandardProductQuery {

    private String name;

    private Long marketingCategoryId;

    private Long brandId;

    private Integer status;

    private Integer isDraft;

    private Integer productCondition;

    private Date createTimeBegin;

    private Date createTimeEnd;

    private Integer offset;

    private Integer pageSize;
}
