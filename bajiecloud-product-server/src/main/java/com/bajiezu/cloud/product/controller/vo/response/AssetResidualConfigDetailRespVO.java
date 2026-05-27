package com.bajiezu.cloud.product.controller.vo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AssetResidualConfigDetailRespVO {

    private Long standardSpuId;
    private Long standardProductSkuId;
    private Long officialPrice;
    private Integer depreciationRuleType;
    private Integer depreciationRuleSubType;
    private String remark;
    private Integer status;
    private String spuName;
    private List<SkuSimpleVO> skuList;
    private List<AssetResidualYearConfigVO> yearConfigs;
    private List<AssetResidualMonthConfigVO> monthConfigs;

    @Data
    public static class SkuSimpleVO {
        private Long skuId;
        private String skuCode;
    }
}
