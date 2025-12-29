package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.ExpressTemplateAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ExpressTemplateListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ExpressTemplateModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StatusChangeReqVo;
import com.bajiezu.cloud.product.controller.vo.response.ExpressTemplateRespVO;

public interface ExpressTemplateService {

    PageResult<ExpressTemplateRespVO> page(ExpressTemplateListReqVO reqVO);

    void add(ExpressTemplateAddReqVO reqVO);

    void mod(ExpressTemplateModReqVO reqVO);

    ExpressTemplateRespVO detail(Long id);

    void del(Long id);

    void changeStatus(StatusChangeReqVo reqVO);
}
