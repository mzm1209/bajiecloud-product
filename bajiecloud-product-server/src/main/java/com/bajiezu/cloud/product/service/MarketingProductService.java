package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.product.controller.vo.request.MarketingProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductTypeStatisticRespVO;
import com.bajiezu.cloud.product.controller.vo.response.StatusStatisticRespVO;

public interface MarketingProductService {

    ProductTypeStatisticRespVO productTypeStatistic();


    StatusStatisticRespVO statusStatistic(MarketingProductListReqVO reqVO);
}
