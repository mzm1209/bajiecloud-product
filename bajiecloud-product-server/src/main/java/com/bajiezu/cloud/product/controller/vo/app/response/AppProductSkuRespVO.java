package com.bajiezu.cloud.product.controller.vo.app.response;

import lombok.Data;

import java.util.List;

@Data
public class AppProductSkuRespVO {
    private Long skuId;
    private List<PropertyValueItem> propertyValues;
    private Long dailyRent;
    private Long totalRent;
    private Long buyoutAmount;
    private Integer stock;
    private Integer isAllowOrder;
    private String picUrl;
    private Long officialPrice;
    private Long strikethroughPrice;

    @Data
    public static class PropertyValueItem {
        private Long propertyId;
        private String propertyName;
        private String propertyValue;
    }
}
