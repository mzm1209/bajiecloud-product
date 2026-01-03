package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Schema(description = "营销商品列表查询参数")
@Data
public class MarketingProductListReqVO extends PageParam {

    @Schema(description = "商品类型 1:租赁商品 2:售卖商品 3:回收商品 4:实物商品 5:虚拟商品")
    private Integer productType;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "营销类目id")
    private Long marketingCategoryId;

    @Schema(description = "商品成色 0:全新 1:非全新")
    private Integer productCondition;

    @Schema(description = "商品创建时间开始")
    private Date createTimeBegin;

    @Schema(description = "商品创建时间结束")
    private Date createTimeEnd;

    @Schema(description = "商品上架状态 0:未上架 1:已上架")
    private Integer shelvesStatus;

    @Schema(description = "商品审核状态 0:待审核 1:审核通过 2:审核未通过")
    private Integer approvalStatus;

    @Schema(description = "是否草稿 0:否 1:是")
    private Integer isDraft;
}
