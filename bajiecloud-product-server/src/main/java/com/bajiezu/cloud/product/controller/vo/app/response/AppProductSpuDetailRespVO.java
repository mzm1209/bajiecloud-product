package com.bajiezu.cloud.product.controller.vo.app.response;

import lombok.Data;

import java.util.List;

@Data
public class AppProductSpuDetailRespVO {
    private Long spuId;
    private String name;
    private String brand;
    private String model;
    private Integer condition;
    private List<String> mainPicUrls;
    private List<String> carouselPicUrls;
    private List<String> videoUrls;
    private List<String> detailPicUrls;
    private Long defaultSkuId;
    private Long dailyRent;
    private Long officialPrice;
    private Long strikethroughPrice;
    private List<AppProductSkuRespVO.PropertyValueItem> properties;
    private List<RentalMethodItem> rentalMethods;
    private List<ValueAddedItem> valueAddedList;

    @Data
    public static class RentalMethodItem {
        private Integer rentalMethod;
        private String rentalMethodName;
        private List<Integer> rentalPeriods;
    }

    @Data
    public static class ValueAddedItem {
        private Long id;
        private String name;
        private Long price;
        private Integer isDefault;
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
