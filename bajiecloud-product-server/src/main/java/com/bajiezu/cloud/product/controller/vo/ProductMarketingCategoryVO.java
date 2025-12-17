package com.bajiezu.cloud.product.controller.vo;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Schema(description = "营销分类 VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductMarketingCategoryVO extends BaseDO {

    @Schema(description = "营销类目ID")
    private Long id;

    @Schema(description = "营销类目名称")
    private String name;

    @Schema(description = "上级营销类目ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "层级 1:一级 2:二级 3:三级")
    private Integer level;

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "分组描述")
    private String remark;

    @Schema(description = "合作商ID")
    private Long partnerId;

    @Schema(description = "子分类列表")
    private List<ProductMarketingCategoryVO> children;

    @Schema(description = "创建人id")
    private Long createBy;

    @Schema(description = "更新人id")
    private Long updateBy;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "是否删除 0:否 1:是")
    private Integer isDeleted;
}
