package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyValueReqVO;
import com.bajiezu.cloud.product.dal.entity.ProductPropertyValue;
import com.bajiezu.cloud.product.dal.mapper.ProductPropertyValueMapper;
import com.bajiezu.cloud.product.service.ProductPropertyValueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;

/**
 * 商品属性值服务实现类
 */
@Slf4j
@Service
public class ProductPropertyValueServiceImpl implements ProductPropertyValueService {

    @Autowired
    private ProductPropertyValueMapper productPropertyValueMapper;

    @Override
    public void add(ProductPropertyValueReqVO reqVO) {
        log.info("add product property value dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        ProductPropertyValue propertyValue = buildPropertyValue(reqVO, loginUser);
        productPropertyValueMapper.insert(propertyValue);
    }

    private ProductPropertyValue buildPropertyValue(ProductPropertyValueReqVO reqVO, LoginUser user) {
        ProductPropertyValue propertyValue = new ProductPropertyValue();
        propertyValue.setPropertyId(reqVO.getPropertyId());
        propertyValue.setPropertyValue(reqVO.getPropertyValue());
        propertyValue.setPartnerId(user.getPartnerId());
        propertyValue.setCreateBy(user.getId());
        propertyValue.setUpdateBy(user.getId());
        propertyValue.setCreateTime(new Date());
        propertyValue.setUpdateTime(new Date());
        propertyValue.setIsDeleted(0);
        return propertyValue;
    }


    //按当前设计，没有编辑操作
    @Override
    public void mod(ProductPropertyValueReqVO reqVO) {
        log.info("mod product property value dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductPropertyValue propertyValue = productPropertyValueMapper.selectById(reqVO.getId());
        propertyValue.setPropertyId(reqVO.getPropertyId());
        propertyValue.setPropertyValue(reqVO.getPropertyValue());
        propertyValue.setUpdateBy(loginUser.getId());
        propertyValue.setUpdateTime(new Date());
        productPropertyValueMapper.updateById(propertyValue);
    }

    @Override
    public void del(ProductPropertyValueReqVO reqVO) {
        log.info("del product property value dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductPropertyValue propertyValue = productPropertyValueMapper.selectById(reqVO.getId());
        propertyValue.setUpdateBy(loginUser.getId());
        propertyValue.setUpdateTime(new Date());
        propertyValue.setIsDeleted(1);
        productPropertyValueMapper.updateById(propertyValue);
    }

    @Override
    public List<ProductPropertyValue> list(Long propertyId) {
        log.info("list product property value by property id: {}", propertyId);
        return productPropertyValueMapper.selectByPropertyId(propertyId);
    }
}
