package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.bajiezu.cloud.common.constants.CommonStatusEnum;
import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.ProductTagRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductTagSimpleInfoRespVO;
import com.bajiezu.cloud.product.dal.entity.ProductTag;
import com.bajiezu.cloud.product.dal.mapper.ProductTagMapper;
import com.bajiezu.cloud.product.service.ProductTagService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.PRODUCT_TAG_NOT_EXIST;
/**
 * 商品标签服务实现类
 */
@Slf4j
@Service
public class ProductTagServiceImpl implements ProductTagService {

    @Resource
    private ProductTagMapper productTagMapper;

    @Override
    public void add(ProductTagAddReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("add product tag dto: {}", reqVO);

        ProductTag tag = buildTag(reqVO, loginUser);
        productTagMapper.insert(tag);
    }

    @Override
    public void mod(ProductTagModReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("mod product tag dto: {},operatorId:{}", reqVO, loginUser.getId());


        ProductTag tag = productTagMapper.selectById(reqVO.getId());
        if (tag == null || NumberUtils.INTEGER_ONE.equals(tag.getIsDeleted())) {
            throw exception(PRODUCT_TAG_NOT_EXIST);
        }
        tag.setName(reqVO.getName());
        tag.setPicUrl(reqVO.getPicUrl());
        tag.setShowPage(CollUtil.join(reqVO.getShowPages(), ","));
        tag.setSort(reqVO.getSort());
        tag.setIsHighlight(reqVO.getIsHighlight());
        tag.setRemark(reqVO.getRemark());
        tag.setUpdateBy(loginUser.getId());
        tag.setUpdateTime(new Date());
        productTagMapper.updateById(tag);
    }

    @Override
    public void del(LongIdReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("del product tag dto: {},operatorId:{}", reqVO, loginUser.getId());

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
    public PageResult<ProductTagRespVO> page(ProductTagListReqVO reqVO) {
        log.info("list product tag dto: {}", reqVO);
        Integer offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        Integer limit = reqVO.getPageSize();

        List<ProductTag> tagList = productTagMapper.queryList(reqVO.getName(), reqVO.getStatus(), offset, limit);
        if (CollectionUtils.isEmpty(tagList)) {
            return PageResult.empty();
        }
        Long count = productTagMapper.queryCount(reqVO.getName(), reqVO.getStatus());

        List<ProductTagRespVO> list = tagList.stream().map(item -> {
            ProductTagRespVO vo = new ProductTagRespVO();
            vo.setId(item.getId());
            vo.setName(item.getName());
            vo.setPicUrl(item.getPicUrl());
            vo.setRemark(item.getRemark());
            vo.setStatus(item.getStatus());
            vo.setCreateTime(item.getCreateTime());
            vo.setSort(item.getSort());
            vo.setIsHighlight(item.getIsHighlight());
            if (StrUtil.isNotBlank(item.getShowPage())) {
                int[] intArray = StrUtil.splitToInt(item.getShowPage(), ',');
                vo.setShowPages(Arrays.stream(intArray).boxed().collect(Collectors.toList()));
            } else {
                vo.setShowPages(Collections.emptyList());
            }
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, count);
    }


    @Override
    public void statusChange(ProductTagStatusChangeVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("status change product tag dto: {},operatorId:{}", reqVO, loginUser.getId());

        ProductTag tag = productTagMapper.selectById(reqVO.getId());
        if (tag == null) {
            throw exception(PRODUCT_TAG_NOT_EXIST);
        }
        tag.setStatus(reqVO.getStatus());
        tag.setUpdateBy(loginUser.getId());
        tag.setUpdateTime(new Date());
        productTagMapper.updateById(tag);
    }

    @Override
    public List<ProductTagSimpleInfoRespVO> simpleList(ProductTagReqVO reqVO) {
        List<ProductTag> productTags = productTagMapper.querySimpleList(reqVO.getShowPage());
        if (CollectionUtils.isEmpty(productTags)) {
            return Collections.emptyList();
        }
        return productTags.stream().map(item -> {
            ProductTagSimpleInfoRespVO vo = new ProductTagSimpleInfoRespVO();
            vo.setId(item.getId());
            vo.setName(item.getName());
            return vo;
        }).collect(Collectors.toList());
    }

    private ProductTag buildTag(ProductTagAddReqVO reqVO, LoginUser<?> user) {
        ProductTag tag = new ProductTag();
        tag.setName(reqVO.getName());
        tag.setPicUrl(reqVO.getPicUrl());
        tag.setShowPage(CollUtil.join(reqVO.getShowPages(), ","));
        tag.setStatus(CommonStatusEnum.ENABLE.getStatus());
        tag.setSort(reqVO.getSort());
        tag.setIsHighlight(reqVO.getIsHighlight());
        tag.setRemark(reqVO.getRemark());
        tag.setPartnerId(user.getPartnerId());
        tag.setCreateBy(user.getId());
        tag.setUpdateBy(user.getId());
        tag.setCreateTime(new Date());
        tag.setUpdateTime(new Date());
        tag.setIsDeleted(0);
        return tag;
    }
}
