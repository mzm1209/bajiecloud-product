package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import com.bajiezu.cloud.product.dal.dto.StandardProductQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "是否草稿 0:否 1:是")
    private Integer isDraft;

    @Schema(description = "商品成色")
    private Integer productCondition;

    @Schema(description = "创建时间开始")
    private Date createTimeBegin;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;

    @Schema(description = "下载来源")
    private Integer source;

    public StandardProductQuery convert2StandardProductQuery() {
        StandardProductQuery standardProductQuery = new StandardProductQuery();
        standardProductQuery.setName(name);
        standardProductQuery.setMarketingCategoryId(marketingCategoryId);
        standardProductQuery.setBrandId(brandId);
        standardProductQuery.setStatus(status);
        standardProductQuery.setIsDraft(isDraft);
        standardProductQuery.setProductCondition(productCondition);
        standardProductQuery.setCreateTimeBegin(createTimeBegin);
        standardProductQuery.setCreateTimeEnd(createTimeEnd);
        standardProductQuery.setOffset((getPageNo() - 1) * getPageSize());
        standardProductQuery.setPageSize(getPageSize());
        return standardProductQuery;
    }
}
