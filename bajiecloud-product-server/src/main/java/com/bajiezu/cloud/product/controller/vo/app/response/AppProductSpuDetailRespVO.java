package com.bajiezu.cloud.product.controller.vo.app.response;

import lombok.Data;

import java.util.List;

@Data
public class AppProductSpuDetailRespVO {
    private Long spuId;
    private String code;
    private Integer type;
    private String name;
    private String brand;
    private String brandName;
    private String model;
    private Integer condition;
    private Integer productCondition;
    private Integer monitorAttribute;
    private Long standardProductSpuId;
    private String standardProductSpuCode;
    private String standardProductSpuName;
    private String businessCategoryName;
    private String marketingCategoryName;
    private List<String> mainPicUrls;
    private List<String> carouselPicUrls;
    private List<String> videoUrls;
    private List<String> detailPicUrls;
    private List<IdNameItem> detailTags;
    private List<IdNameItem> skuTags;
    private Long defaultSkuId;
    private Long dailyRent;
    private Long officialPrice;
    private Long strikethroughPrice;
    private List<AppProductSkuRespVO.PropertyValueItem> properties;
    private List<SpuPropertyItem> spuProperties;
    private List<RentalMethodItem> rentalMethods;
    private List<SkuRentalMethodPropertyItem> skuRentalMethodProperties;
    private List<AppProductSkuRespVO> skus;
    private List<ValueAddedItem> valueAddedList;
    private List<Integer> showPages;
    private Integer isDefaultSelected;
    private Long defaultSelectedValueAddedId;
    private String defaultSelectedValueAddedName;
    private Long compensationRuleId;
    private Integer shippingWay;
    private Long shippingTemplateId;
    private String shippingTemplateName;
    private List<AreaItem> shippingAreaCodes;
    private String receivingAddress;

    @Data
    public static class IdNameItem {
        private Long id;
        private String name;
    }

    @Data
    public static class SpuPropertyItem {
        private Long propertyId;
        private String propertyName;
        private Integer sort;
        private Integer isAddPropertyPic;
        private Integer isAddMarketingCorner;
        private Integer isSkuProperty;
        private List<SpuPropertyValueItem> propertyValues;
    }

    @Data
    public static class SpuPropertyValueItem {
        private Long productPropertyValueId;
        private String value;
        private Integer sort;
        private String picUrl;
        private String marketingCornerText;
    }

    @Data
    public static class AreaItem {
        private String areaCode;
        private String areaName;
        private Long shippingCost;
    }

    @Data
    public static class RentalMethodItem {
        private Integer rentalMethod;
        private String rentalMethodName;
        private List<Integer> rentalPeriods;
    }

    @Data
    public static class SkuRentalMethodPropertyItem {
        private Long skuId;
        private Integer rentalMethod;
        private String rentalMethodName;
        private Integer rentalPeriodMonth;
        private Long totalRent;
        private Long monthlyRent;
        private Long dailyRent;
        private Long buyoutAmount;
        private Long premium;
        private Integer stock;
    }

    @Data
    public static class ValueAddedItem {
        private Long id;
        private String name;
        private Long price;
        private Integer isDefault;
        private String serviceOverview;
        private String serviceContent;
        private String serviceTypes;
        private String effectiveChannels;
        private Integer compensationStandard;
        private Integer compensationLevel;
        private String compensationLevelLimits;
        private Integer slightCompensationRatio;
        private Integer mediumCompensationRatio;
        private Integer severeCompensationRatio;
        private Integer scrapCompensationRatio;
        private Long compensationAmount;
        private Integer compensationAmountRatio;
        private List<CompensationAmountRuleItem> compensationAmountRules;
        private String saleLimits;
        private Integer annualLimitPurchaseCount;
        private Integer monthlyLimitPurchaseCount;
        private Integer dailyLimitPurchaseCount;
        private Integer accessCondition;
        private String accessConditionLimits;
        private Long accessConditionBreachAmount;
        private Integer accessConditionBreachCount;
    }

    @Data
    public static class CompensationAmountRuleItem {
        private Long id;
        private Long compensationAmount;
        private Integer compensationAmountRatio;
        private Integer sortOrder;
    }
}
