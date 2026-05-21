package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.PropertyAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.PropertyListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.PropertyModReqVO;
import com.bajiezu.cloud.product.controller.vo.response.PropertyRespVO;
import com.bajiezu.cloud.product.controller.vo.response.PropertySimpleInfoVO;
import com.bajiezu.cloud.product.controller.vo.response.PropertyValueSimpleInfoVO;
import com.bajiezu.cloud.product.dal.entity.ProductProperty;
import com.bajiezu.cloud.product.dal.entity.ProductPropertyValue;
import com.bajiezu.cloud.product.dal.mapper.ProductPropertyMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductPropertyValueMapper;
import com.bajiezu.cloud.product.service.ProductPropertyService;
import com.bajiezu.cloud.system.api.user.AdminUserApi;
import com.bajiezu.cloud.system.dto.AdminUserRespDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.PROPERTY_ALREADY_EXIST;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.PROPERTY_NOT_EXIST;

@Slf4j
@Service
public class ProductPropertyServiceImpl implements ProductPropertyService {

    @Resource
    private ProductPropertyMapper propertyMapper;

    @Resource
    private ProductPropertyValueMapper propertyValueMapper;
    @Resource
    private AdminUserApi adminUserApi;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(PropertyAddReqVO reqVO) {
        log.info("add product property dto: {}", reqVO);
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        // 校验属性名是否已存在
        validateNameUnique(null, reqVO.getName());

        // 保存属性记录
        Date now = new Date();
        ProductProperty property = buildProperty(reqVO, loginUser.getId(), loginUser.getPartnerId(), now);
        propertyMapper.insert(property);

        // 保存属性值记录
        List<ProductPropertyValue> propertyValues = Lists.newArrayList();
        for (String value : reqVO.getPropertyValues()) {
            ProductPropertyValue propertyValue = buildPropertyValue(property.getId(), value, loginUser, now);
            propertyValues.add(propertyValue);
        }
        propertyValueMapper.batchInsert(propertyValues);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mod(PropertyModReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("mod product property dto: {},operatorId:{}", reqVO, loginUser.getId());

        // 校验属性名是否已存在
        validateNameUnique(reqVO.getId(), reqVO.getName());

        // 校验属性是否存在
        ProductProperty property = propertyMapper.selectById(reqVO.getId());
        if (property == null) {
            throw exception(PROPERTY_NOT_EXIST);
        }
        // 更新属性记录
        Date now = new Date();
        property.setName(reqVO.getName());
        property.setSort(reqVO.getSort());
        property.setIsSkuProperty(defaultSkuProperty(reqVO.getIsSkuProperty()));
        property.setUpdateBy(loginUser.getId());
        property.setUpdateTime(now);
        propertyMapper.updateById(property);

        //获取已经存在未删除的属性值
        Set<String> existValues = propertyValueMapper.selectValuesByPropertyId(reqVO.getId());
        // 需要新增的属性值
        Set<String> addValues = reqVO.getPropertyValues().stream().filter(value -> !existValues.contains(value))
                .collect(Collectors.toSet());
        if (CollectionUtil.isNotEmpty(addValues)) {
            List<ProductPropertyValue> propertyValues = Lists.newArrayList();
            for (String value : addValues) {
                ProductPropertyValue propertyValue = buildPropertyValue(property.getId(), value, loginUser, now);
                propertyValues.add(propertyValue);
            }
            propertyValueMapper.batchInsert(propertyValues);
        }
        // 需要删除的属性值
        Set<String> delValues = existValues.stream().filter(value -> !reqVO.getPropertyValues().contains(value))
                .collect(Collectors.toSet());
        if (CollectionUtil.isNotEmpty(delValues)) {
            propertyValueMapper.logicDelByPropertyIdAndValues(property.getId(), delValues, loginUser.getId(), now);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void del(LongIdReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("del product property reqVO: {}, operatorId:{}", reqVO, loginUser.getId());

        ProductProperty property = propertyMapper.selectById(reqVO.getId());
        if (property == null) {
            throw exception(PROPERTY_NOT_EXIST);
        }
        Date now = new Date();
        propertyMapper.logicDelById(property.getId(), loginUser.getId(), now);
        propertyValueMapper.logicDelByPropertyId(property.getId(), loginUser.getId(), now);
    }

    @Override
    public PageResult<PropertyRespVO> page(PropertyListReqVO reqVO) {
        log.info("list product property dto: {}", reqVO);
        Integer offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        Integer limit = reqVO.getPageSize();

        // 获取属性记录
        List<ProductProperty> propertyList = propertyMapper.queryList(reqVO.getName(), offset, limit);
        if (CollectionUtils.isEmpty(propertyList)) {
            return PageResult.empty();
        }
        Long count = propertyMapper.queryCount(reqVO.getName());

        // 获取属性值
        List<Long> propertyIds = propertyList.stream().map(ProductProperty::getId).toList();
        List<ProductPropertyValue> propertyValueList = propertyValueMapper.selectListByPropertyIds(propertyIds);
        Map<Long, Set<String>> propertyId2ValuesMap = propertyValueList.stream().collect(Collectors.groupingBy(
                ProductPropertyValue::getPropertyId, Collectors.mapping(ProductPropertyValue::getPropertyValue, Collectors.toSet())));

        // 获取创建者名称
        Set<Long> creatorIds = propertyList.stream().map(ProductProperty::getCreateBy).collect(Collectors.toSet());
        CommonResult<List<AdminUserRespDTO>> userResult = adminUserApi.getUserList(creatorIds);
        Map<Long, String> userId2NameMap = Maps.newHashMap();
        if (userResult.isSuccess() && userResult.getData() != null) {
            userResult.getData().forEach(user -> userId2NameMap.put(user.getId(), user.getName()));
        }

        // 构造返回结果
        List<PropertyRespVO> resultList = Lists.newArrayList();
        for (ProductProperty property : propertyList) {
            PropertyRespVO respVO = new PropertyRespVO();
            resultList.add(respVO);
            respVO.setId(property.getId());
            respVO.setName(property.getName());
            respVO.setCreatorName(userId2NameMap.get(property.getCreateBy()));
            respVO.setPropertyValues(propertyId2ValuesMap.get(property.getId()));
            respVO.setSort(property.getSort());
            respVO.setIsSkuProperty(defaultSkuProperty(property.getIsSkuProperty()));
            respVO.setCreateTime(property.getCreateTime());
        }

        return new PageResult<>(resultList, count);
    }

    @Override
    public List<PropertySimpleInfoVO> simpleList() {
        // 获取所有未删除的属性记录
        List<ProductProperty> propertyList = propertyMapper.queryAll();
        if (CollectionUtil.isEmpty(propertyList)) {
            return Collections.emptyList();
        }

        // 获取所有的属性记录
        List<ProductPropertyValue> propertyValueList = propertyValueMapper.queryAll();
        Map<Long, List<ProductPropertyValue>> propertyId2ValuesMap = propertyValueList.stream().collect(
                Collectors.groupingBy(ProductPropertyValue::getPropertyId));

        // 构造返回结果
        List<PropertySimpleInfoVO> resultList = Lists.newArrayList();
        for (ProductProperty property : propertyList) {
            PropertySimpleInfoVO respVO = new PropertySimpleInfoVO();
            resultList.add(respVO);
            respVO.setId(property.getId());
            respVO.setName(property.getName());
            respVO.setIsSkuProperty(defaultSkuProperty(property.getIsSkuProperty()));

            List<ProductPropertyValue> propertyValues = propertyId2ValuesMap.get(property.getId());
            List<PropertyValueSimpleInfoVO> propertyValueSimpleInfoVOList = propertyValues.stream()
                    .map(propertyValue -> {
                        PropertyValueSimpleInfoVO vo = new PropertyValueSimpleInfoVO();
                        vo.setId(propertyValue.getId());
                        vo.setPropertyValue(propertyValue.getPropertyValue());
                        return vo;
                    })
                    .collect(Collectors.toList());
            respVO.setPropertyValues(propertyValueSimpleInfoVOList);
        }
        return resultList;
    }

    private void validateNameUnique(Long id, String name) {
        ProductProperty property = propertyMapper.selectByName(name);
        if (property == null) {
            return;
        }

        if (property.getId().equals(id)) {
            return;
        }
        throw exception(PROPERTY_ALREADY_EXIST);
    }

    private ProductProperty buildProperty(PropertyAddReqVO reqVO, Long userId, Long partnerId, Date now) {
        ProductProperty property = new ProductProperty();
        property.setName(reqVO.getName());
        property.setSort(reqVO.getSort());
        property.setIsSkuProperty(defaultSkuProperty(reqVO.getIsSkuProperty()));
        property.setPartnerId(partnerId);
        property.setCreateBy(userId);
        property.setUpdateBy(userId);
        property.setCreateTime(now);
        property.setUpdateTime(now);
        property.setIsDeleted(0);
        return property;
    }

    private ProductPropertyValue buildPropertyValue(Long propertyId, String value, LoginUser<?> user, Date now) {
        ProductPropertyValue propertyValue = new ProductPropertyValue();
        propertyValue.setPropertyId(propertyId);
        propertyValue.setPropertyValue(value);
        propertyValue.setPartnerId(user.getPartnerId());
        propertyValue.setCreateBy(user.getId());
        propertyValue.setUpdateBy(user.getId());
        propertyValue.setCreateTime(new Date());
        propertyValue.setUpdateTime(new Date());
        propertyValue.setIsDeleted(0);
        return propertyValue;
    }

    private Integer defaultSkuProperty(Integer isSkuProperty) {
        return isSkuProperty == null ? 0 : isSkuProperty;
    }
}
