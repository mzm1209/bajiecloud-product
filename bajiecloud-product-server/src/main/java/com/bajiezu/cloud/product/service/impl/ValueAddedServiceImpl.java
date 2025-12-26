package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.date.DateUtil;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedStatusChangeReqVO;
import com.bajiezu.cloud.product.controller.vo.response.ValueAddedRespVO;
import com.bajiezu.cloud.product.dal.entity.ValueAdded;
import com.bajiezu.cloud.product.dal.entity.ValueAddedProduct;
import com.bajiezu.cloud.product.dal.mapper.ValueAddedMapper;
import com.bajiezu.cloud.product.dal.mapper.ValueAddedProductMapper;
import com.bajiezu.cloud.product.service.ValueAddedService;
import com.bajiezu.cloud.product.util.SequenceGenerator;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ValueAddedServiceImpl implements ValueAddedService {

    @Resource
    private SequenceGenerator sequenceGenerator;

    @Resource
    private ValueAddedMapper valueAddedMapper;
    @Resource
    private ValueAddedProductMapper valueAddedProductMapper;

    @Override
    public PageResult<ValueAddedRespVO> page(ValueAddedListReqVO reqVO) {
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ValueAddedAddReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("valueAdded add dto: {},operatorId:{}", reqVO, loginUser.getId());

        Date now = new Date();
        ValueAdded valueAdded = buildValueAdded(reqVO, loginUser, now);
        valueAddedMapper.insert(valueAdded);

        List<ValueAddedProduct> valueAddedProducts = buildValueAddedProducts(reqVO.getMarketingProductSkuIds(),
                valueAdded.getId(), loginUser, now);
        valueAddedProductMapper.batchInsert(valueAddedProducts);
    }

    @Override
    public void mod(ValueAddedModReqVO reqVO) {

    }

    @Override
    public ValueAddedRespVO detail(Long id) {
        return null;
    }

    @Override
    public void del(Long id) {

    }

    @Override
    public void changeStatus(ValueAddedStatusChangeReqVO reqVO) {

    }

    private ValueAdded buildValueAdded(ValueAddedAddReqVO reqVO, LoginUser<?> loginUser, Date now) {
        ValueAdded valueAdded = new ValueAdded();
        String code = "ZZ" + DateUtil.format(new Date(), "yyMMdd") + sequenceGenerator.getValueAddedSequence();
        valueAdded.setCode(code);
        valueAdded.setName(reqVO.getName());
        valueAdded.setStatus(reqVO.getStatus());
        valueAdded.setSalePrice(reqVO.getSalePrice());
        valueAdded.setRenewalPrice(reqVO.getRenewalPrice());
        valueAdded.setStrikethroughPrice(reqVO.getStrikethroughPrice());
        valueAdded.setServiceOverview(reqVO.getServiceOverview());
        valueAdded.setServiceContent(reqVO.getServiceContent());
        valueAdded.setPicUrl(reqVO.getPicUrl());
        valueAdded.setPartnerId(loginUser.getPartnerId());
        valueAdded.setCreateTime(now);
        valueAdded.setUpdateTime(now);
        valueAdded.setCreateBy(loginUser.getId());
        valueAdded.setUpdateBy(loginUser.getId());
        valueAdded.setIsDeleted(0);
        return valueAdded;
    }

    private List<ValueAddedProduct> buildValueAddedProducts(List<Long> marketingProductSkuIds, Long valueAddedId,
                                                            LoginUser<?> loginUser, Date now) {
        List<ValueAddedProduct> valueAddedProducts = Lists.newArrayList();
        for (Long marketingProductSkuId : marketingProductSkuIds) {
            ValueAddedProduct valueAddedProduct = new ValueAddedProduct();
            valueAddedProducts.add(valueAddedProduct);
            valueAddedProduct.setValueAddedId(valueAddedId);
            valueAddedProduct.setMarketingProductSkuId(marketingProductSkuId);
            valueAddedProduct.setPartnerId(loginUser.getPartnerId());
            valueAddedProduct.setCreateBy(loginUser.getId());
            valueAddedProduct.setCreateTime(now);
            valueAddedProduct.setUpdateBy(loginUser.getId());
            valueAddedProduct.setUpdateTime(now);
            valueAddedProduct.setIsDeleted(0);
        }
        return valueAddedProducts;
    }
}
