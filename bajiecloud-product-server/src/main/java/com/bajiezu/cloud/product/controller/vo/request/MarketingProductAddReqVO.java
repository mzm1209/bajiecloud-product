package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "营销商品添加请求VO")
@Data
public class MarketingProductAddReqVO {

    @Schema(description = "操作类型 1-保存（草稿），2-提交")
    private Integer operateType;

    @Schema(description = "商品类型 1-租赁商品，2-售卖商品，3-回收商品，4-实物商品，5-虚拟商品")
    private Integer type;

    @Schema(description = "标准商品ID")
    private Long standardProductSpuId;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品成色 0:全新 1:非全新")
    private List<Integer> productConditions;

    @Schema(description = "商品监控属性 0:监管 1:非监管")
    private List<Integer> monitorAttributes;

    @Schema(description = "商品主图")
    private List<String> mainPicUrls;

    @Schema(description = "商详轮播图")
    private List<String> carouselPicUrls;

    @Schema(description = "商品视频")
    private List<String> videoUrls;

    @Schema(description = "商详介绍图")
    private List<String> detailPicUrls;

    @Schema(description = "商详标签")
    private List<Long> detailTagIds;

    @Schema(description = "SKU页标签")
    private List<Long> skuTagIds;

    /**************************  增值服务 ********************************/
    @Schema(description = "增值服务")
    private List<Long> valueAddedIds;
    @Schema(description = "展示页面 0:SKU页 1:确认订单页")
    private List<Integer> showPages;
    @Schema(description = "是否默认勾选 0:否 1:是")
    private Integer isDefaultSelected;
    @Schema(description = "默认勾选增值服务ID")
    private Long defaultSelectedValueAddedId;

    /**************************  增值服务 ********************************/
    @Schema(description = "补偿规则ID")
    private Long compensationRuleId;
    @Schema(description = "发货方式 1:快递 2:同城配送 3:门店自提")
    private Integer shippingWay;
    @Schema(description = "快递模版ID")
    private Long shippingTemplateId;
    @Schema(description = "发货地区")
    private List<String> shippingAreaCodes;

}
