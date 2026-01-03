package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 营销商品SPU表 实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("marketing_product_spu")
public class MarketingProductSpu extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标准商品spu表主键ID
     */
    private Long standardProductSpuId;

    /**
     * 编码
     */
    private String code;

    /**
     * 商品类型 1:租赁商品 2:售卖商品 3:回收商品 4:实物商品 5:虚拟商品
     */
    private Integer type;

    /**
     * 名称
     */
    private String name;

    /**
     * 商品成色 0:全新 1:非全新
     */
    private Integer productCondition;

    /**
     * 监管属性 0:监管 1:非监管
     */
    private Integer monitorAttribute;

    /**
     * 回收最低价
     */
    private Long minBuybackPrice;

    /**
     * 回收最高价
     */
    private Long maxBuybackPrice;

    /**
     * 商品主图URL（多个用逗号分隔）
     */
    private String mainPicUrls;

    /**
     * 商详轮播图URL（多个用逗号分隔）
     */
    private String carouselPicUrls;

    /**
     * 视频URL（多个用逗号分隔）
     */
    private String videoUrls;

    /**
     * 商详介绍图URL（多个用逗号分隔）
     */
    private String detailPicUrls;

    /**
     * 商详标签ID（多个用英文逗号分隔）
     */
    private String detailTagIds;

    /**
     * SKU页标签ID（多个用英文逗号分隔）
     */
    private String skuTagIds;

    /**
     * 租赁方式 0:租完归还 1:灵活租
     */
    private Integer rentWay;

    /**
     * 颜色（多个用英文逗号分隔）
     */
    private String color;

    /**
     * 规格
     */
    private String specification;

    /**
     * 租期 单位为天（多个用英文逗号分隔）
     */
    private String rentalPeriod;

    /**
     * 续租租期 单位为天（多个用英文逗号分隔）
     */
    private String renewalPeriod;

    /**
     * 灵活租可归还租期（天）
     */
    private Integer flexibleReturnPeriod;

    /**
     * 增值服务ID（多个用英文逗号分隔）
     */
    private String valueAddedIds;

    /**
     * 展示页面 0:SKU页 1:确认订单页
     */
    private Integer showPage;

    /**
     * 是否默认勾选 0:否 1:是
     */
    private Integer isDefaultSelected;

    /**
     * 默认勾选服务ID
     */
    private Long defaultSelectedValueAddedId;

    /**
     * 赔偿规则ID
     */
    private Long compensationRuleId;

    /**
     * 发货方式 1:快递 2:同城配送 3:门店自提
     */
    private Integer shippingWay;

    /**
     * 运费模版ID
     */
    private Long shippingTemplateId;

    /**
     * 发货地区ID
     */
    private Long shippingAreaId;

    /**
     * 收货地址
     */
    private String receivingAddress;

    /**
     * 上架方式 1:自动上架 2:手动上架 3:预约上架
     */
    private Integer shelvingWay;

    /**
     * 上架时间
     */
    private Date shelvingTime;

    /**
     * 上架渠道ID（多个用英文逗号分隔）
     */
    private String shelvingChannelId;

    /**
     * 是否已上架 0:待上架 1:已上架 2:已下架
     */
    private Integer shelvesStatus;

    /**
     * 是否为草稿 0:否 1:是
     */
    private Integer isDraft;

    /**
     * 状态 0:启用 1:禁用
     */
    private Integer status;

    /**
     * 审批状态 0:未审批 1:审批通过 2:审批拒绝
     */
    private Integer approvalStatus;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批备注
     */
    private String approvalRemark;
}