package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.common.constants.CommonStatusEnum;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.PBRespVO;
import com.bajiezu.cloud.product.dal.entity.ProductBrand;
import com.bajiezu.cloud.product.dal.mapper.ProductBrandMapper;
import com.bajiezu.cloud.product.service.ProductBrandService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.PRODUCT_BRAND_NOT_EXIST;
import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;


@Slf4j
@Service
public class ProductBrandServiceImpl implements ProductBrandService {


    @Resource
    private ProductBrandMapper productBrandMapper;

    @Override
    public void add(PBAddReqVO reqVO) {
        log.info("add dto: {}", reqVO);
        reqVO.validateParam();
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        ProductBrand brand = buildBrand(reqVO, loginUser);
        productBrandMapper.insert(brand);
    }

    private ProductBrand buildBrand(PBAddReqVO reqVO, LoginUser<?> user) {
        ProductBrand brand = new ProductBrand();
        brand.setName(reqVO.getBrandName());
        brand.setSort(reqVO.getSort());
        brand.setRemark(reqVO.getRemark());
        brand.setStatus(CommonStatusEnum.ENABLE.getStatus());
        brand.setPartnerId(user.getPartnerId());
        brand.setCreateBy(user.getId());
        brand.setUpdateBy(user.getId());
        brand.setCreateTime(new Date());
        brand.setUpdateTime(new Date());
        brand.setIsDeleted(0);
        return brand;
    }

    @Override
    public void mod(PBModReqVO reqVO) {
        log.info("mod dto: {}", reqVO);
        reqVO.validateParam();
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductBrand brand = productBrandMapper.selectById(reqVO.getId());
        if (brand == null) {
            throw exception(PRODUCT_BRAND_NOT_EXIST);
        }
        brand.setName(reqVO.getBrandName());
        brand.setSort(reqVO.getSort());
        brand.setRemark(reqVO.getRemark());
        brand.setUpdateBy(loginUser.getId());
        brand.setUpdateTime(new Date());
        productBrandMapper.updateById(brand);
    }

    @Override
    public void del(PBDelReqVO reqVO) {
        log.info("del dto: {}", reqVO);
        reqVO.validateParam();
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductBrand brand = productBrandMapper.selectById(reqVO.getId());
        if (brand == null) {
            throw exception(PRODUCT_BRAND_NOT_EXIST);
        }
        brand.setUpdateBy(loginUser.getId());
        brand.setUpdateTime(new Date());
        brand.setIsDeleted(1);
        productBrandMapper.updateById(brand);
    }

    @Override
    public PageResult<PBRespVO> list(PBListReqVO reqVO) {
        log.info("list dto: {}", reqVO);
        Integer offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        Integer limit = reqVO.getPageSize();

        List<ProductBrand> brandList = productBrandMapper.queryList(reqVO.getBrandName(), offset, limit);
        if (CollectionUtils.isEmpty(brandList)) {
            return PageResult.empty();
        }
        Long count = productBrandMapper.queryCount(reqVO.getBrandName());
        log.info("list count : {}", count);

        List<PBRespVO> list = brandList.stream().map(item -> {
            PBRespVO vo = new PBRespVO();
            vo.setId(item.getId());
            vo.setBrandName(item.getName());
            vo.setSort(item.getSort());
            vo.setRemark(item.getRemark());
            vo.setStatus(item.getStatus());
            return vo;
        }).toList();
        return new PageResult<>(list, count);
    }

    @Override
    public void statusChange(PBStatusChangeVO reqVO) {
        log.info("statusChange dto: {}", reqVO);
        reqVO.validateParam();
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductBrand brand = productBrandMapper.selectById(reqVO.getId());
        if (brand == null) {
            throw exception(PRODUCT_BRAND_NOT_EXIST);
        }
        brand.setStatus(reqVO.getStatus());
        brand.setUpdateBy(loginUser.getId());
        brand.setUpdateTime(new Date());
        productBrandMapper.updateById(brand);
    }
}
