package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.ProductTagReqVO;
import com.bajiezu.cloud.product.dal.entity.ProductTag;
import com.bajiezu.cloud.product.dal.mapper.ProductTagMapper;
import com.bajiezu.cloud.product.service.ProductTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.baijiazu.cloud.product.enums.ErrorCodeConstants.PRODUCT_TAG_NOT_EXIST;
/**
 * 商品标签服务实现类
 */
@Slf4j
@Service
public class ProductTagServiceImpl implements ProductTagService {

    @Autowired
    private ProductTagMapper productTagMapper;

    @Override
    public void add(ProductTagReqVO reqVO) {
        log.info("add product tag dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        ProductTag tag = buildTag(reqVO, loginUser);
        productTagMapper.insert(tag);
    }

    private ProductTag buildTag(ProductTagReqVO reqVO, LoginUser user) {
        ProductTag tag = new ProductTag();
        tag.setName(reqVO.getName());
        tag.setPicUrl(reqVO.getPicUrl());
        tag.setShowPage(reqVO.getShowPage());
        tag.setStatus(reqVO.getStatus());
        tag.setSort(reqVO.getSort());
        tag.setIsHighlight(reqVO.getIsHighlight());
        tag.setPartnerId(user.getPartnerId());
        tag.setCreateBy(user.getId());
        tag.setUpdateBy(user.getId());
        tag.setCreateTime(new Date());
        tag.setUpdateTime(new Date());
        tag.setIsDeleted(0);
        return tag;
    }

    @Override
    public void mod(ProductTagReqVO reqVO) {
        log.info("mod product tag dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductTag tag = productTagMapper.selectById(reqVO.getId());
        try {
            tag.setName(reqVO.getName());
            tag.setPicUrl(reqVO.getPicUrl());
            tag.setShowPage(reqVO.getShowPage());
            tag.setStatus(reqVO.getStatus());
            tag.setSort(reqVO.getSort());
            tag.setIsHighlight(reqVO.getIsHighlight());
            tag.setUpdateBy(loginUser.getId());
            tag.setUpdateTime(new Date());
            productTagMapper.updateById(tag);
        } catch (Exception e) {
            throw exception(PRODUCT_TAG_NOT_EXIST);
        }
    }

    @Override
    public void del(ProductTagReqVO reqVO) {
        log.info("del product tag dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductTag tag = productTagMapper.selectById(reqVO.getId());
        if (tag == null) {
            throw exception(PRODUCT_TAG_NOT_EXIST);
        }
        tag.setUpdateBy(loginUser.getId());
        tag.setUpdateTime(new Date());
        tag.setIsDeleted(1);
        productTagMapper.updateById(tag);
    }

    @Override
    public PageResult<ProductTag> list(ProductTagReqVO reqVO) {
        log.info("list product tag dto: {}", reqVO);
        Integer offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        Integer limit = reqVO.getPageSize();

        List<ProductTag> tagList = productTagMapper.queryList(
                reqVO.getName(), offset, limit);
        if (CollectionUtils.isEmpty(tagList)) {
            return PageResult.empty();
        }
        Long count = productTagMapper.queryCount(reqVO.getName());
        log.info("list product tag count : {}", count);

        List<ProductTag> list = tagList.stream().map(item -> {
            ProductTag vo = new ProductTag();
            vo.setId(item.getId());
            vo.setName(item.getName());
            vo.setPicUrl(item.getPicUrl());
            vo.setShowPage(item.getShowPage());
            vo.setStatus(item.getStatus());
            vo.setSort(item.getSort());
            vo.setIsHighlight(item.getIsHighlight());
            vo.setPartnerId(item.getPartnerId());
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, count);
    }


    @Override
    public void statusChange(ProductTagReqVO reqVO) {
        log.info("status change product tag dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductTag tag = productTagMapper.selectById(reqVO.getId());
        try {
            tag.setStatus(reqVO.getStatus());
            tag.setUpdateBy(loginUser.getId());
            tag.setUpdateTime(new Date());
            productTagMapper.updateById(tag);
        } catch (Exception e) {
            throw exception(PRODUCT_TAG_NOT_EXIST);
        }
    }
}
