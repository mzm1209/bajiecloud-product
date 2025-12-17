package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyValueReqVO;
import com.bajiezu.cloud.product.dal.entity.ProductProperty;
import com.bajiezu.cloud.product.dal.entity.ProductPropertyValue;
import com.bajiezu.cloud.product.dal.mapper.ProductPropertyMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductPropertyValueMapper;
import com.bajiezu.cloud.product.service.ProductPropertyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

@Slf4j
@Service
public class ProductPropertyServiceImpl implements ProductPropertyService {

    @Autowired
    private ProductPropertyMapper productPropertyMapper;

    @Autowired
    private ProductPropertyValueMapper productPropertyValueMapper;


    @Override
    public void add(ProductPropertyReqVO reqVO) {
        log.info("add product property dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        ProductProperty property = buildProperty(reqVO, loginUser);
        productPropertyMapper.insert(property);
        if (property.getId() != null) {
            List<ProductPropertyValue> propertyValues = reqVO.getPropertyValues();
            if (!CollectionUtils.isEmpty(propertyValues)) {
                for (ProductPropertyValue value : propertyValues) {
                    ProductPropertyValue propertyValue = buildPropertyValue(value, loginUser);
                    propertyValue.setPropertyId(property.getId());
                    productPropertyValueMapper.insert(propertyValue);
                }
            }
        } else {
            log.error("Failed to get generated ID for product property");
            throw new RuntimeException("Failed to generate property ID");
        }
    }

    private ProductProperty buildProperty(ProductPropertyReqVO reqVO, LoginUser user) {
        ProductProperty property = new ProductProperty();
        property.setName(reqVO.getName());
        property.setSort(reqVO.getSort());
        property.setPartnerId(user.getPartnerId());
        property.setCreateBy(user.getId());
        property.setUpdateBy(user.getId());
        property.setCreateTime(new Date());
        property.setUpdateTime(new Date());
        property.setIsDeleted(0);
        return property;
    }

    private ProductPropertyValue buildPropertyValue(ProductPropertyValue reqVO, LoginUser user) {
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

    @Override
    public void mod(ProductPropertyReqVO reqVO) {
        log.info("mod product property dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductProperty property = productPropertyMapper.selectById(reqVO.getId());
        property.setName(reqVO.getName());
        property.setSort(reqVO.getSort());
        property.setUpdateBy(loginUser.getId());
        property.setUpdateTime(new Date());
        productPropertyMapper.updateById(property);

        //获取旧的属性值
        //获取旧的属性值
        List<ProductPropertyValue> oldPropertyValues = productPropertyValueMapper.selectByPropertyId(reqVO.getId());
        Set<Long> oldIds = oldPropertyValues.stream()
                .map(ProductPropertyValue::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        //将当前参数中的属性值更新
        List<ProductPropertyValue> propertyValues = reqVO.getPropertyValues();
        Set<Long> newIds = propertyValues.stream()
                .map(ProductPropertyValue::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        //找出需要删除的属性值（存在于旧列表但不在新列表中）
        Set<Long> toDeleteIds = oldIds.stream()
                .filter(id -> !newIds.contains(id))
                .collect(Collectors.toSet());

        // 删除需要删除的属性值(物理删除)
        if (!toDeleteIds.isEmpty()) {
            QueryWrapper<ProductPropertyValue> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.in("id", toDeleteIds);
            productPropertyValueMapper.delete(deleteWrapper);
        }

        // 处理新增和更新的属性值
        if (!CollectionUtils.isEmpty(propertyValues)) {
            for (ProductPropertyValue value : propertyValues) {
                if (value.getId() != null) {
                    // 更新已有属性值
                    ProductPropertyValue propertyValue = productPropertyValueMapper.selectById(value.getId());
                    if (propertyValue != null) {
                        propertyValue.setPropertyValue(value.getPropertyValue());
                        propertyValue.setUpdateBy(loginUser.getId());
                        propertyValue.setUpdateTime(new Date());
                        productPropertyValueMapper.updateById(propertyValue);
                    }
                } else {
                    // 新增属性值
                    ProductPropertyValue propertyValue = buildPropertyValue(value, loginUser);
                    propertyValue.setPropertyId(property.getId());
                    productPropertyValueMapper.insert(propertyValue);
                }
            }
        }




    }

    @Override
    public void del(ProductPropertyReqVO reqVO) {
        log.info("del product property dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductProperty property = productPropertyMapper.selectById(reqVO.getId());
        property.setUpdateBy(loginUser.getId());
        property.setUpdateTime(new Date());
        property.setIsDeleted(1);
        productPropertyMapper.updateById(property);
    }

    @Override
    public PageResult<ProductProperty> list(ProductPropertyReqVO reqVO) {
        log.info("list product property dto: {}", reqVO);
        Integer offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        Integer limit = reqVO.getPageSize();

        List<ProductProperty> propertyList = productPropertyMapper.queryList(
                reqVO.getName(), offset, limit);
        if (CollectionUtils.isEmpty(propertyList)) {
            return PageResult.empty();
        }
        Long count = productPropertyMapper.queryCount(reqVO.getName());
        log.info("list product property count : {}", count);

        // 为每个属性查询并拼接对应的属性值
        List<ProductProperty> resultList = propertyList.stream().map(property -> {
            // 查询该属性对应的属性值列表
            List<ProductPropertyValue> propertyValues = productPropertyValueMapper.selectByPropertyId(property.getId());

            // 将属性值拼接成字符串，用分号分隔
            property.setProductPropertyValues(propertyValues);

            return property;
        }).collect(Collectors.toList());

        return new PageResult<>(resultList, count);
    }
}
