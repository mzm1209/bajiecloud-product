package com.bajiezu.cloud.product.controller.vo.response;

import com.bajiezu.cloud.product.controller.vo.MarketingProductPropertyVO;
import com.bajiezu.cloud.product.controller.vo.MarketingProductRentalMethodVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Schema(description = "营销商品响应VO")
@Data
public class MarketingProductRespVO {

    @Schema(description = "标准商品ID")
    private Long standardProductId;
    @Schema(description = "标准商品编码")
    private String standardProductCode;
    @Schema(description = "标准商品名称")
    private String standardProductName;

    @Schema(description = "营销商品ID")
    private Long marketingProductId;
    @Schema(description = "营销商品编码")
    private String marketingProductCode;
    @Schema(description = "营销商品名称")
    private String marketingProductName;

    @Schema(description = "商品主图")
    private List<String> mainPicUrls;

    @Schema(description = "SKU数量")
    private Long skuCount;

    @Schema(description = "商品属性")
    private List<MarketingProductPropertyVO> properties;

    @Schema(description = "租赁方式配置")
    private List<MarketingProductRentalMethodVO> rentalMethods;

    @Schema(description = "库存")
    private Long stock;

    @Schema(description = "上架渠道")
    private List<String> channelNames;

    @Schema(description = "最低日租金")
    private Long minDailyRentPrice;

    @Schema(description = "审批状态 0:待审核 1:审核通过 2:审核未通过")
    private Integer approveStatus;
    @Schema(description = "0:待上架 1:已上架 2:已下架")
    private Integer shelvesStatus;
    @Schema(description = "是否草稿 0:否 1:是")
    private Integer isDraft;



    @Schema(description = "最低采购价价格")
    private Long minOfficialPrice;
    @Schema(description = "最高采购价格")
    private Long maxOfficialPrice;

    @Schema(description = "最低建议售价")
    private Long minSuggestedRetailPrice;
    @Schema(description = "最高建议售价")
    private Long maxSuggestedRetailPrice;

    @Schema(description = "最低回收价")
    private Long minBuybackPrice;
    @Schema(description = "最高回收价")
    private Long maxBuybackPrice;

    @Schema(description = "创建人名称")
    private String creatorName;
    @Schema(description = "创建人名称")
    private Date createTime;
    @Schema(description = "更新人名称")
    private String updaterName;
    @Schema(description = "更新时间")
    private Date updateTime;
}
