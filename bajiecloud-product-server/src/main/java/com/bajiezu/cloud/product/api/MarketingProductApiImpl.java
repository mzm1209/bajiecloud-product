package com.bajiezu.cloud.product.api;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.dto.MarketingProductReqVO;
import com.bajiezu.cloud.product.dto.McSimpleInfoRespVO;
import com.bajiezu.cloud.product.dto.ProductDetailRespVO;
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
}