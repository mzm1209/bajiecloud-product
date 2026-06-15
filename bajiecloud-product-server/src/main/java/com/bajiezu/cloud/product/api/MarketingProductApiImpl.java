package com.bajiezu.cloud.product.api;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.dto.*;
import com.bajiezu.cloud.product.enums.ErrorCodeConstants;
import com.bajiezu.cloud.product.enums.ProductApiConstants;
import com.bajiezu.cloud.product.service.MarketingProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Primary
@Slf4j
public class MarketingProductApiImpl implements MarketingProductApi {

    @Resource
    private MarketingProductService marketingProductService;


    @Override
    public CommonResult<List<ProductDetailRespVO>> batchGetProductDetail(MarketingProductReqVO reqVO) {
        log.info("[MarketingProductApiImpl.batchGetProductDetail] reqVO: {}", reqVO);
        if (CollectionUtil.isEmpty(reqVO.getIds())) {
            return CommonResult.success(null);
        }

        List<ProductDetailRespVO> productDetailRespVOS = marketingProductService.batchGetProductDetail(reqVO);
        return CommonResult.success(productDetailRespVOS);
    }

    @Override
    public CommonResult<SkuRespDto> getSkuInfoById(Long skuId) {
        log.info("[MarketingProductApiImpl.getSkuInfoById] skuId: {}", skuId);
        if (skuId == null) {
            return CommonResult.error(ErrorCodeConstants.SKU_ID_IS_NULL);
        }
        SkuRespDto skuRespDto = marketingProductService.getSkuInfoById(skuId);
        if (skuRespDto != null) {
            List<PropertyVO> properties = skuRespDto.getProperties();
            for (PropertyVO propertyVO : properties) {
                // 租期
                if (ProductApiConstants.RENTAL_PERIOD.equals(propertyVO.getPropertyName())) {
                    int leaseTermCount = Integer.parseInt(propertyVO.getPropertyValues().get(0).getPropertyValue()) / ProductApiConstants.THIRTY;
                    if (leaseTermCount == 0) {
                        leaseTermCount = 1;
                    }
                    skuRespDto.setLeaseTermCount(leaseTermCount);
                }
                // 续租租期
                if (ProductApiConstants.RENEWAL_TERM.equals(propertyVO.getPropertyName())) {
                    int renewalTermCount = Integer.parseInt(propertyVO.getPropertyValues().get(0).getPropertyValue()) / ProductApiConstants.THIRTY;
                    if (renewalTermCount == 0) {
                        renewalTermCount = 1;
                    }
                    skuRespDto.setRenewalTermCount(renewalTermCount);
                }
            }
        }

        return CommonResult.success(skuRespDto);
    }

    @Override
    public CommonResult<SkuRentalPriceRespDto> getSkuRentalPrice(Long skuId, Integer rentalMethod, Integer rentalPeriodMonth) {
        log.info("[getSkuRentalPrice] skuId:{},rentalMethod:{},rentalPeriodMonth:{}", skuId, rentalMethod, rentalPeriodMonth);
        if (skuId == null) {
            return CommonResult.error(ErrorCodeConstants.SKU_ID_IS_NULL);
        }
        if (rentalMethod == null || rentalPeriodMonth == null) {
            return CommonResult.error(ErrorCodeConstants.RENTAL_METHOD_PERIOD_REQUIRED);
        }
        return CommonResult.success(
                marketingProductService.getSkuRentalPrice(skuId, rentalMethod, rentalPeriodMonth));
    }

    @Override
    public CommonResult<Boolean> deductRentalStock(SkuRentalStockReqDto reqDto) {
        log.info("[deductRentalStock] reqDto:{}", reqDto);
        boolean success = marketingProductService.deductRentalStock(reqDto);
        if (!success) {
            return CommonResult.error(ErrorCodeConstants.RENTAL_STOCK_NOT_ENOUGH);
        }
        return CommonResult.success(true);
    }

    @Override
    public CommonResult<Boolean> restoreRentalStock(SkuRentalStockReqDto reqDto) {
        log.info("[restoreRentalStock] reqDto:{}", reqDto);
        return CommonResult.success(marketingProductService.restoreRentalStock(reqDto));
    }

    @Override
    public CommonResult<Long> getResidualAmount(Long skuId, Integer globalMonth) {
        log.info("[getResidualAmount] skuId:{}, globalMonth:{}", skuId, globalMonth);
        if (skuId == null || globalMonth == null || globalMonth < 1) {
            return CommonResult.success(null);
        }
        Long amount = marketingProductService.getResidualAmount(skuId, globalMonth);
        return CommonResult.success(amount);
    }

    @Override
    public CommonResult<List<SkuRentalPriceRespDto>> listSkuRentalPrices(Long skuId, Integer rentalMethod) {
        log.info("[listSkuRentalPrices] skuId:{}, rentalMethod:{}", skuId, rentalMethod);
        if (skuId == null || rentalMethod == null) {
            return CommonResult.success(List.of());
        }
        return CommonResult.success(marketingProductService.listSkuRentalPrices(skuId, rentalMethod));
    }

    @Override
    public CommonResult<AssetConfigDto> getAssetConfig(Long marketingSkuId) {
        log.info("[getAssetConfig] marketingSkuId:{}", marketingSkuId);
        if (marketingSkuId == null) {
            return CommonResult.success(null);
        }
        AssetConfigDto result = marketingProductService.getAssetConfig(marketingSkuId);
        return CommonResult.success(result);
    }
}