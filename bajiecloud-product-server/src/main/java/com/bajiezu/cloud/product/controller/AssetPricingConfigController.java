package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.controller.vo.request.AssetPricingConfigQueryReqVO;
import com.bajiezu.cloud.product.controller.vo.request.AssetPricingConfigSaveReqVO;
import com.bajiezu.cloud.product.controller.vo.response.AssetPricingConfigDetailRespVO;
import com.bajiezu.cloud.product.service.AssetPricingConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理后台 - 资产定价配置")
@RestController
@RequestMapping("/product/assetPricing")
@Validated
public class AssetPricingConfigController {
    @Resource private AssetPricingConfigService assetPricingConfigService;

    @PostMapping("/detail")
    @Operation(summary = "资产定价配置详情")
    public CommonResult<AssetPricingConfigDetailRespVO> detail(@Valid @RequestBody AssetPricingConfigQueryReqVO reqVO){return CommonResult.success(assetPricingConfigService.detail(reqVO));}

    @PostMapping("/save")
    @Operation(summary = "保存资产定价配置")
    public CommonResult<Boolean> save(@Valid @RequestBody AssetPricingConfigSaveReqVO reqVO){assetPricingConfigService.save(reqVO);return CommonResult.success(true);}    
}
