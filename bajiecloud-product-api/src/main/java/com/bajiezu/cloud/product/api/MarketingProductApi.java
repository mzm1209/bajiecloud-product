package com.bajiezu.cloud.product.api;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.dto.MarketingProductReqVO;
import com.bajiezu.cloud.product.dto.ProductDetailRespVO;
import com.bajiezu.cloud.product.dto.SkuRespDto;
import com.bajiezu.cloud.product.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 营销商品")
public interface MarketingProductApi {

    String PREFIX = ApiConstants.PREFIX + "/mk";

    @PostMapping(PREFIX + "/batchGetProductDetail")
    @Operation(summary = "批量获取营销商品详情")
    CommonResult<List<ProductDetailRespVO>> batchGetProductDetail(@RequestBody MarketingProductReqVO reqVO);


    @GetMapping(PREFIX + "/getSkuInfoById")
    @Operation(summary = "根据skuId获取sku信息")
    CommonResult<SkuRespDto> getSkuInfoById(@RequestParam("skuId") Long skuId);
}