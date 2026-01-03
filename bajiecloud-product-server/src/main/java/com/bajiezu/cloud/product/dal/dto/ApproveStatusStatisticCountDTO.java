package com.bajiezu.cloud.product.dal.dto;

import lombok.Data;

@Data
public class ApproveStatusStatisticCountDTO {

    /**
     * 0:待审核 1:审核通过 2:审核未通过
     */
    private Integer approveStatus;

    private Integer count;
}
