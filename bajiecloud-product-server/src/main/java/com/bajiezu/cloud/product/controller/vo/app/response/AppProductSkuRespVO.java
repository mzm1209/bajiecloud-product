package com.bajiezu.cloud.product.controller.vo.app.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AppProductSkuRespVO {
    private Long skuId;
    private String skuCode;
    private List<PropertyValueItem> propertyValues;
    private Long dailyRent;
    private Long totalRent;
    private Long buyoutAmount;
    private Integer stock;
    private Integer isAllowOrder;
    private String picUrl;
    private Long officialPrice;
    private Long strikethroughPrice;
    private BigDecimal totalPriceFactor;
    private BigDecimal totalRentFactor;
    private Long totalPrice;
    private Long premium;
    private Long suggestedRetailPrice;
    private BigDecimal cashUsageRatio;
    private BigDecimal pointsUsageRatio;
    private Integer pointsCount;
    private Long cashPrice;
    private List<RentalMethodPropertyItem> rentalMethodProperties;

    @Data
    public static class PropertyValueItem {
        private Long propertyId;
        private String propertyName;
        private Long propertyValueId;
        private String propertyValue;
        private String picUrl;
        private String marketingCornerText;
    }

    @Data
    public static class RentalMethodPropertyItem {
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
}
