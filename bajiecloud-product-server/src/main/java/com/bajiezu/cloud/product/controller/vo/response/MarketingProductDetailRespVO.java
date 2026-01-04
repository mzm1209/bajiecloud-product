package com.bajiezu.cloud.product.controller.vo.response;

import com.bajiezu.cloud.product.controller.vo.AreaCodeAndNameVO;
import com.bajiezu.cloud.product.controller.vo.MarketingProductPropertyVO;
import com.bajiezu.cloud.product.controller.vo.MarketingProductSkuVO;
import com.bajiezu.cloud.product.controller.vo.IdAndNameVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Schema(description = "营销商品详情")
@Data
public class MarketingProductDetailRespVO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品编码")
    private String code;

    @Schema(description = "商品类型 1-租赁商品，2-售卖商品，3-回收商品，4-实物商品，5-虚拟商品")
    private Integer type;

    @Schema(description = "标准商品ID")
    private Long standardProductSpuId;
    @Schema(description = "标准商品名称")
    private String standardProductSpuName;
    @Schema(description = "经营类目")
    private String businessCategoryName;
    @Schema(description = "营销类目")
    private String marketingCategoryName;
    @Schema(description = "品牌")
    private String brandName;

    @Schema(description = "营销商品名称")
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
    private List<IdAndNameVO> detailTags;

    @Schema(description = "SKU页标签")
    private List<IdAndNameVO> skuTags;


    /**************************  租赁商品规格信息 *************************/
   /* @Schema(description = "租赁方式 0:租完归还 1:灵活租")
    private List<Integer> rentWays;
    @Schema(description = "颜色")
    private Set<String> colors;
    @Schema(description = "规格")
    private Set<String> specifications;
    @Schema(description = "租期")
    private Set<Integer> rentalPeriods;
    @Schema(description = "续租租期")
    private Set<Integer> renewalPeriods;
    @Schema(description = "灵活租可归还租期")
    private Integer flexibleReturnPeriod;*/

    private List<MarketingProductPropertyVO> spuProperties;

    /**************************  回收商品 *************************/
    @Schema(description = "最低回收价")
    private Long minBuybackPrice;
    @Schema(description = "最高回收价")
    private Long maxBuybackPrice;


    /**************************  增值服务 ********************************/
    @Schema(description = "增值服务")
    private List<IdAndNameVO> valueAddedList;
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
    @Schema(description = "快递模版名称")
    private String shippingTemplateName;
    @Schema(description = "发货地区")
    private List<AreaCodeAndNameVO> shippingAreaCodes;


    /**************************  商品上架信息 ********************************/
    @Schema(description = "1:自动上架 2:手动上架 3:预约上架")
    private Integer shelvingWay;
    @Schema(description = "上架时间")
    private Date shelvingTime;
    @Schema(description = "上架渠道")
    private List<IdAndNameVO> shelvingChannels;

    @Schema(description = "spu下的sku信息")
    private List<MarketingProductSkuVO> skus;

    @Schema(description = "审批状态 0:待审批 1:审批通过 2:审批拒绝")
    private Integer approveStatus;

    @Schema(description = "上下架状态 0:待上架 1:已上架 2:已下架")
    private Integer shelvesStatus;

    @Schema(description = "是否草稿 0:否 1:是")
    private Integer isDraft;
}
