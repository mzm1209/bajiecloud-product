package com.bajiezu.cloud.product.dal.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExpressTemplateQuery {

    private String templateName;

    private Integer status;

    private Integer offset;

    private Integer pageSize;
}
