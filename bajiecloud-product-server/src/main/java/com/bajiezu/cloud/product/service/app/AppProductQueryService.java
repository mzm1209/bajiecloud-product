package com.bajiezu.cloud.product.service.app;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.app.request.*;
import com.bajiezu.cloud.product.controller.vo.app.response.*;

import java.util.List;

public interface AppProductQueryService {
    PageResult<AppProductSpuPageRespVO> spuPage(AppProductSpuPageReqVO reqVO);
    AppProductSpuDetailRespVO spuDetail(AppProductSpuDetailReqVO reqVO);
    List<AppProductSkuRespVO> skuList(AppProductSkuListReqVO reqVO);
    AppProductSkuRespVO skuDetail(AppProductSkuDetailReqVO reqVO);
}
