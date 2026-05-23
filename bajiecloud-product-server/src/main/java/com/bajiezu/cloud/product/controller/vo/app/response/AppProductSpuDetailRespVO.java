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
    private List<ValueAddedItem> valueAddedList;

    @Data
    public static class ValueAddedItem {
        private Long id;
        private String name;
        private Long price;
        private Integer isDefault;
    }
}
