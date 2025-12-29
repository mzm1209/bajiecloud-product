package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.ExpressTemplateAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ExpressTemplateListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ExpressTemplateModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StatusChangeReqVo;
import com.bajiezu.cloud.product.controller.vo.response.ExpressTemplateRespVO;
import com.bajiezu.cloud.product.dal.entity.ExpressTemplateShippingFrom;
import com.bajiezu.cloud.product.dal.mapper.ExpressTemplateMapper;
import com.bajiezu.cloud.product.dal.mapper.ExpressTemplateShippingToMapper;
import com.bajiezu.cloud.product.service.ExpressTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExpressTemplateServiceImpl implements ExpressTemplateService {

    @Resource
    private ExpressTemplateMapper expressTemplateMapper;
    @Resource
    private ExpressTemplateShippingToMapper shippingToMapper;
    @Resource
    private ExpressTemplateShippingFrom shippingFromMapper;

    @Override
    public PageResult<ExpressTemplateRespVO> page(ExpressTemplateListReqVO reqVO) {
        return null;
    }

    @Override
    public void add(ExpressTemplateAddReqVO reqVO) {

    }

    @Override
    public void mod(ExpressTemplateModReqVO reqVO) {

    }

    @Override
    public ExpressTemplateRespVO detail(Long id) {
        return null;
    }

    @Override
    public void del(Long id) {

    }

    @Override
    public void changeStatus(StatusChangeReqVo reqVO) {

    }
}
