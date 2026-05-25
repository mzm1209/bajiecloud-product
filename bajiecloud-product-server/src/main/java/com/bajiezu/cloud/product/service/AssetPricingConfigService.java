package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.product.controller.vo.request.AssetPricingConfigQueryReqVO;
import com.bajiezu.cloud.product.controller.vo.request.AssetPricingConfigSaveReqVO;
import com.bajiezu.cloud.product.controller.vo.response.AssetPricingConfigDetailRespVO;

public interface AssetPricingConfigService {
    AssetPricingConfigDetailRespVO detail(AssetPricingConfigQueryReqVO reqVO);
    void save(AssetPricingConfigSaveReqVO reqVO);
}
