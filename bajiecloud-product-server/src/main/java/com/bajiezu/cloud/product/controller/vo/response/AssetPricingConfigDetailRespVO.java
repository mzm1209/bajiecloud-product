package com.bajiezu.cloud.product.controller.vo.response;

import lombok.Data;

import java.util.List;

@Data
public class AssetPricingConfigDetailRespVO {
    private Long standardSpuId;
    private Long standardProductSkuId;
    private Long partnerId;
    private List<LeaseModeConfigVO> leaseModeConfigs;

    @Data
    public static class LeaseModeConfigVO {
        private Integer leaseMode;
        private List<AssetPricingYearConfigVO> yearConfigs;
    }
}
