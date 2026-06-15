package com.bajiezu.cloud.product.controller.vo.app.response;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AppProductSpuPageRespVO {
    private Long spuId;
    private String name;
    private String mainPicUrl;
    private String priceText;
    private List<String> tagList;
    private String marketingLabel;
    private Date shelvingTime;
    private List<IdNameItem> shelvingChannels;
    private Long defaultSkuId;
    private Integer stockStatus;
    private String shopButtonText;

    @Data
    public static class IdNameItem {
        private Long id;
        private String name;
    }
}
