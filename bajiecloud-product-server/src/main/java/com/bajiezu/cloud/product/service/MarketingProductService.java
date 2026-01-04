package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductApproveReqVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.OnOffShelvesReqVO;
import com.bajiezu.cloud.product.controller.vo.response.MarketingProductDetailRespVO;
import com.bajiezu.cloud.product.controller.vo.response.MarketingProductRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductTypeStatisticRespVO;
import com.bajiezu.cloud.product.controller.vo.response.StatusStatisticRespVO;

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
}
