package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductBusinessCategoryRespVO {

    @Schema(description = "经营类目ID")
    private Long id;

    @Schema(description = "经营类目名称")
    private String name;

    @Schema(description = "上级经营类目ID")
    private Long parentId;

    @Schema(description = "合作商ID")
    private Long partnerId;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "更新人ID")
    private Long updateBy;

    @Schema(description = "创建时间")
    private java.util.Date createTime;

    @Schema(description = "更新时间")
    private java.util.Date updateTime;
}
