package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "标准商品列表查询参数")
@Data
public class StandardProductListReqVO extends PageParam {

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "营销类目ID")
    private Long marketingCategoryId;

    @Schema(description = "品牌ID")
    private Long brandId;

    @Schema(description = "商品属性列表")
    private List<ProductPropertyReqVO> properties;

    @Schema(description = "创建时间开始")
    private Date createTimeBegin;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;
}
