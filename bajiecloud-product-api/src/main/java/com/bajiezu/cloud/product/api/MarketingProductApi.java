package com.bajiezu.cloud.product.api;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.dto.AssetConfigDto;
import com.bajiezu.cloud.product.dto.MarketingProductReqVO;
import com.bajiezu.cloud.product.dto.ProductDetailRespVO;
import com.bajiezu.cloud.product.dto.SkuRentalPriceRespDto;
import com.bajiezu.cloud.product.dto.SkuRentalStockReqDto;
import com.bajiezu.cloud.product.dto.SkuRespDto;
import com.bajiezu.cloud.product.enums.ProductApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = ProductApiConstants.NAME)
@Tag(name = "RPC 服务 - 营销商品")
public interface MarketingProductApi {

    String PREFIX = ProductApiConstants.PREFIX + "/mk";

    @PostMapping(PREFIX + "/batchGetProductDetail")
    @Operation(summary = "批量获取营销商品详情")
    CommonResult<List<ProductDetailRespVO>> batchGetProductDetail(@RequestBody MarketingProductReqVO reqVO);


    @GetMapping(PREFIX + "/getSkuInfoById")
    @Operation(summary = "根据skuId获取sku信息")
    CommonResult<SkuRespDto> getSkuInfoById(@RequestParam("skuId") Long skuId);

    @GetMapping(PREFIX + "/getSkuRentalPrice")
    @Operation(summary = "按租赁方式+租期获取SKU价格与库存")
    CommonResult<SkuRentalPriceRespDto> getSkuRentalPrice(
            @RequestParam("skuId") Long skuId,
            @RequestParam("rentalMethod") Integer rentalMethod,
            @RequestParam("rentalPeriodMonth") Integer rentalPeriodMonth);

    @PostMapping(PREFIX + "/deductRentalStock")
    @Operation(summary = "扣减租期维度库存（原子，库存不足返回失败）")
    CommonResult<Boolean> deductRentalStock(@RequestBody SkuRentalStockReqDto reqDto);

    @PostMapping(PREFIX + "/restoreRentalStock")
    @Operation(summary = "回补租期维度库存（订单取消/超时/创单回滚）")
    CommonResult<Boolean> restoreRentalStock(@RequestBody SkuRentalStockReqDto reqDto);

    @GetMapping(PREFIX + "/getResidualAmount")
    @Operation(summary = "按营销SKU+已用月数查询精确残值（当期购买金）")
    CommonResult<Long> getResidualAmount(
            @RequestParam("skuId") Long skuId,
            @RequestParam("globalMonth") Integer globalMonth);

    @GetMapping(PREFIX + "/listSkuRentalPrices")
    @Operation(summary = "按营销SKU+租赁方式查询所有可用租期价格列表")
    CommonResult<List<SkuRentalPriceRespDto>> listSkuRentalPrices(
            @RequestParam("skuId") Long skuId,
            @RequestParam("rentalMethod") Integer rentalMethod);

    @GetMapping(PREFIX + "/getAssetConfig")
    @Operation(summary = "按营销SKU查询关联的资产配置（残值+定价）全量数据")
    CommonResult<AssetConfigDto> getAssetConfig(@RequestParam("marketingSkuId") Long marketingSkuId);
}