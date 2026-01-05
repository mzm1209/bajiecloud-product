package com.bajiezu.cloud.product.dal.dto;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MarketingProductQuery {


    private Integer productType;

    private String name;

    private Integer productCondition;

    private Date createTimeBegin;

    private Date createTimeEnd;

    private Integer shelvesStatus;

    private Integer approvalStatus;

    private Integer isDraft;

    private Integer offset;

    private Integer pageSize;

    private List<Long> standardProductSpuIds;
}
