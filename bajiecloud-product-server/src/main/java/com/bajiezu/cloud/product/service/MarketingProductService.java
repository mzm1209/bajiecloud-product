package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.*;
import com.bajiezu.cloud.product.dto.MarketingProductReqVO;
import com.bajiezu.cloud.product.dto.ProductDetailRespVO;

import java.util.List;

public interface MarketingProductService {

    PageResult<MarketingProductRespVO> page(MarketingProductListReqVO reqVO);

    ProductTypeStatisticRespVO productTypeStatistic();

    void add(MarketingProductAddReqVO reqVO);

    void mod(MarketingProductAddReqVO reqVO);

    MarketingProductDetailRespVO detail(Long id);

    void del(List<Long> ids);

    void onOffShelves(OnOffShelvesReqVO reqVO);

    void approve(MarketingProductApproveReqVO reqVO);

    StatusStatisticRespVO statusStatistic(MarketingProductListReqVO reqVO);

    List<ProductDetailRespVO> batchGetProductDetail(MarketingProductReqVO reqVO);

    PageResult<SpuRespVO> spuListForAddCoupon(ProductListReqVO reqVO);

    PageResult<SkuRespVO> skuListForAddCoupon(ProductListReqVO reqVO);
}
