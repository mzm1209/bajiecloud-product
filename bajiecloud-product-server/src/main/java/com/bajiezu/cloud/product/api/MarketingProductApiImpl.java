package com.bajiezu.cloud.product.api;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSku;
import com.bajiezu.cloud.product.dto.MarketingProductReqVO;
import com.bajiezu.cloud.product.dto.McSimpleInfoRespVO;
import com.bajiezu.cloud.product.dto.ProductDetailRespVO;
import com.bajiezu.cloud.product.dto.SkuRespDto;
import com.bajiezu.cloud.product.enums.ErrorCodeConstants;
import com.bajiezu.cloud.product.service.MarketingProductService;
import com.bajiezu.cloud.product.service.ProductMarketingCategoryService;
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
        return CommonResult.success(skuRespDto);
    }
}