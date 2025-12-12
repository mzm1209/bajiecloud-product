package com.bajiezu.cloud.product.service;

import cn.hutool.db.PageResult;
import com.bajiezu.cloud.product.controller.vo.*;
import com.bajiezu.cloud.product.dal.mapper.ProductBrandMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class ProductBrandServiceImpl implements ProductBrandService{


    @Autowired
    private ProductBrandMapper productBrandMapper;

    @Override
    public void add(PBAddReqVO reqVO) {
        log.info("add dto: {}", reqVO);
    }

    @Override
    public void mod(PBModReqVO reqVO) {

    }

    @Override
    public void del(PBDelReqVO reqVO) {

    }

    @Override
    public PageResult<PBRespVO> list(PBListReqVO reqVO) {
        return null;
    }

    @Override
    public void statusChange(PBStatusChangeVO reqVO) {

    }
}
