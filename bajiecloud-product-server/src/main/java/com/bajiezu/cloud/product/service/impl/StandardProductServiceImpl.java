package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StatusChangeReqVo;
import com.bajiezu.cloud.product.controller.vo.response.StandardProductRespVO;
import com.bajiezu.cloud.product.dal.mapper.StandardProductSpuMapper;
import com.bajiezu.cloud.product.dal.mapper.StandardProductSpuPropertyMapper;
import com.bajiezu.cloud.product.dal.mapper.StandardProductSpuPropertyValueMapper;
import com.bajiezu.cloud.product.service.StandardProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StandardProductServiceImpl implements StandardProductService {

    @Resource
    private StandardProductSpuMapper spuMapper;
    @Resource
    private StandardProductSpuPropertyMapper spuPropertyMapper;
    @Resource
    private StandardProductSpuPropertyValueMapper spuPropertyValueMapper;

    @Override
    public PageResult<StandardProductRespVO> page(StandardProductListReqVO reqVO) {
        return null;
    }

    @Override
    public void add(StandardProductAddReqVO reqVO) {

    }

    @Override
    public void mod(StandardProductModReqVO reqVO) {

    }

    @Override
    public StandardProductRespVO detail(Long id) {
        return null;
    }

    @Override
    public void del(Long id) {

    }

    @Override
    public void changeStatus(StatusChangeReqVo reqVO) {

    }
}
