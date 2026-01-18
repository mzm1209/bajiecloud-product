package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.constants.CommonStatusEnum;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedStatusChangeReqVO;
import com.bajiezu.cloud.product.controller.vo.response.ValueAddedRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ValueAddedSimpleInfoRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ValueAddedSkuRespVO;
import com.bajiezu.cloud.product.dal.dto.ValueAddedQuery;
import com.bajiezu.cloud.product.dal.entity.*;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.dto.PropertyVO;
import com.bajiezu.cloud.product.dto.PropertyValueVO;
import com.bajiezu.cloud.product.dto.ValueAddedRespDto;
import com.bajiezu.cloud.product.service.ValueAddedService;
import com.bajiezu.cloud.product.util.SequenceGenerator;
import com.bajiezu.cloud.system.api.user.AdminUserApi;
import com.bajiezu.cloud.system.dto.AdminUserRespDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.VALUE_ADDED_DELETED;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.VALUE_ADDED_NOT_EXIST;

@Slf4j
@Service
public class ValueAddedServiceImpl implements ValueAddedService {

    @Resource
    private SequenceGenerator sequenceGenerator;

    @Resource
    private ValueAddedMapper valueAddedMapper;
    @Resource
    private ValueAddedProductMapper valueAddedProductMapper;
    @Resource
    private MarketingProductSkuPropertyValueMapper skuPropertyValueMapper;
    @Resource
    private MarketingProductSpuPropertyValueMapper spuPropertyValueMapper;
    @Resource
    private ProductPropertyMapper productPropertyMapper;
    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;
    @Resource
    private MarketingProductSkuMapper skuMapper;
    @Resource
    private MarketingProductSpuMapper spuMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public PageResult<ValueAddedRespVO> page(ValueAddedListReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("valueAdded page reqVO: {},operatorId:{}", reqVO, loginUser.getId());
        ValueAddedQuery query = reqVO.convert2ValueAddedQuery();
        List<ValueAdded> valueAddedList = valueAddedMapper.selectListByQuery(query);
        if (CollUtil.isEmpty(valueAddedList)) {
            return PageResult.empty();
        }
        long count = valueAddedMapper.selectCountByQuery(query);

        List<Long> valueAddedIds = Lists.newArrayList();
        Set<Long> userIds = Sets.newHashSet();
        for (ValueAdded valueAdded : valueAddedList) {
            valueAddedIds.add(valueAdded.getId());
            userIds.add(valueAdded.getCreateBy());
            userIds.add(valueAdded.getUpdateBy());
        }
        CommonResult<List<AdminUserRespDTO>> userRespDTOResult = adminUserApi.getUserList(userIds);
        Map<Long, String> userId2NameMap = Maps.newHashMap();
        if (userRespDTOResult.isSuccess() && userRespDTOResult.getData() != null) {
            for (AdminUserRespDTO userRespDTO : userRespDTOResult.getData()) {
                userId2NameMap.put(userRespDTO.getId(), userRespDTO.getName());
            }
        }

        List<ValueAddedProduct> valueAddedProducts = valueAddedProductMapper.selectListByValueAddedIds(valueAddedIds);
        Map<Long, List<ValueAddedProduct>> valueAddedProductsMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(valueAddedProducts)) {
            valueAddedProductsMap = valueAddedProducts.stream().collect(Collectors.groupingBy(ValueAddedProduct::getValueAddedId));
        }

        List<ValueAddedRespVO> valueAddedRespVOS = Lists.newArrayList();
        for (ValueAdded valueAdded : valueAddedList) {
            ValueAddedRespVO valueAddedRespVO = new ValueAddedRespVO();
            valueAddedRespVOS.add(valueAddedRespVO);
            valueAddedRespVO.setId(valueAdded.getId());
            valueAddedRespVO.setCode(valueAdded.getCode());
            valueAddedRespVO.setName(valueAdded.getName());
            valueAddedRespVO.setServiceOverview(valueAdded.getServiceOverview());
            valueAddedRespVO.setServiceContent(valueAdded.getServiceContent());
            valueAddedRespVO.setSalePrice(valueAdded.getSalePrice());
            valueAddedRespVO.setRenewalPrice(valueAdded.getRenewalPrice());
            valueAddedRespVO.setStrikethroughPrice(valueAdded.getStrikethroughPrice());
            if (valueAddedProductsMap.containsKey(valueAdded.getId())) {
                valueAddedRespVO.setSkuCount(valueAddedProductsMap.get(valueAdded.getId()).size());
            } else {
                valueAddedRespVO.setSkuCount(0);
            }
            valueAddedRespVO.setStatus(valueAdded.getStatus());
            valueAddedRespVO.setCreateTime(valueAdded.getCreateTime());
            valueAddedRespVO.setCreatorName(userId2NameMap.get(valueAdded.getCreateBy()));
            valueAddedRespVO.setUpdateTime(valueAdded.getUpdateTime());
            valueAddedRespVO.setUpdaterName(userId2NameMap.get(valueAdded.getUpdateBy()));
        }

        return new PageResult<>(valueAddedRespVOS, count);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ValueAddedAddReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("valueAdded add dto: {},operatorId:{}", reqVO, loginUser.getId());

        Date now = new Date();
        ValueAdded valueAdded = buildValueAdded(reqVO, loginUser, now);
        valueAddedMapper.insert(valueAdded);

        if (CollUtil.isNotEmpty(reqVO.getMarketingProductSkuIds())) {
            List<ValueAddedProduct> valueAddedProducts = buildValueAddedProducts(reqVO.getMarketingProductSkuIds(),
                    valueAdded.getId(), loginUser, now);
            valueAddedProductMapper.batchInsert(valueAddedProducts);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mod(ValueAddedModReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("valueAdded mod dto: {},operatorId:{}", reqVO, loginUser.getId());
        ValueAdded valueAdded = valueAddedMapper.selectById(reqVO.getId());
        if (valueAdded == null) {
            throw exception(VALUE_ADDED_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(valueAdded.getIsDeleted())) {
            throw exception(VALUE_ADDED_DELETED);
        }

        // 更新增值服务
        Date now = new Date();
        valueAdded.setName(reqVO.getName());
        valueAdded.setStatus(reqVO.getStatus());
        valueAdded.setSalePrice(reqVO.getSalePrice());
        valueAdded.setRenewalPrice(reqVO.getRenewalPrice());
        valueAdded.setStrikethroughPrice(reqVO.getStrikethroughPrice());
        valueAdded.setServiceOverview(reqVO.getServiceOverview());
        valueAdded.setServiceContent(reqVO.getServiceContent());
        if (CollUtil.isNotEmpty(reqVO.getPicUrls())) {
            valueAdded.setPicUrl(StringUtils.join(reqVO.getPicUrls(), ","));
        } else {
            valueAdded.setPicUrl("");
        }
        valueAdded.setUpdateTime(now);
        valueAdded.setUpdateBy(loginUser.getId());
        valueAddedMapper.updateById(valueAdded);

        // 处理关联的商品
        List<Long> existMarketingProductSkuIds = valueAddedProductMapper.queryMarketingProductSkuIdsByValueAddedId(reqVO.getId());
        List<Long> marketingProductSkuIds = reqVO.getMarketingProductSkuIds();

        // 需要删除的商品
        List<Long> deleteMarketingProductSkuIds = CollUtil.subtractToList(existMarketingProductSkuIds, marketingProductSkuIds);
        if (CollUtil.isNotEmpty(deleteMarketingProductSkuIds)) {
            valueAddedProductMapper.logicDelByValueAddedIdAndMarketingProductSkuIds(reqVO.getId(), deleteMarketingProductSkuIds, loginUser.getId(), now);
        }
        // 需要新增的商品
        List<Long> addMarketingProductSkuIds = CollUtil.subtractToList(marketingProductSkuIds, existMarketingProductSkuIds);
        if (CollUtil.isNotEmpty(addMarketingProductSkuIds)) {
            List<ValueAddedProduct> valueAddedProducts = buildValueAddedProducts(addMarketingProductSkuIds, reqVO.getId(), loginUser, now);
            valueAddedProductMapper.batchInsert(valueAddedProducts);
        }
    }

    @Override
    public ValueAddedRespVO detail(Long id) {
        ValueAdded valueAdded = valueAddedMapper.selectById(id);
        if (valueAdded == null) {
            throw exception(VALUE_ADDED_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(valueAdded.getIsDeleted())) {
            throw exception(VALUE_ADDED_DELETED);
        }
        // 构造返回结果
        ValueAddedRespVO valueAddedRespVO = new ValueAddedRespVO();
        valueAddedRespVO.setId(valueAdded.getId());
        valueAddedRespVO.setCode(valueAdded.getCode());
        valueAddedRespVO.setName(valueAdded.getName());
        valueAddedRespVO.setSalePrice(valueAdded.getSalePrice());
        valueAddedRespVO.setRenewalPrice(valueAdded.getRenewalPrice());
        valueAddedRespVO.setStrikethroughPrice(valueAdded.getStrikethroughPrice());
        valueAddedRespVO.setServiceOverview(valueAdded.getServiceOverview());
        valueAddedRespVO.setServiceContent(valueAdded.getServiceContent());
        valueAddedRespVO.setPicUrls(List.of(StringUtils.split(valueAdded.getPicUrl(), ",")));
        valueAddedRespVO.setStatus(valueAdded.getStatus());

        List<ValueAddedSkuRespVO> valueAddedSkus = buildValueAddedSkus(id);
        valueAddedRespVO.setSkuRespVOList(valueAddedSkus);
        return valueAddedRespVO;
    }

    private List<ValueAddedSkuRespVO> buildValueAddedSkus(Long valueAddedId) {
        List<Long> skuIds = valueAddedProductMapper.queryMarketingProductSkuIdsByValueAddedId(valueAddedId);
        if (CollUtil.isEmpty(skuIds)) {
            return Collections.emptyList();
        }

        List<MarketingProductSkuPropertyValue> skuPropertyValues = skuPropertyValueMapper.selectListBySkuIds(skuIds);
        Map<Long, List<MarketingProductSkuPropertyValue>> skuId2SpuPropertyValueIdsMap = skuPropertyValues.stream().collect(Collectors.groupingBy(
                MarketingProductSkuPropertyValue::getMarketingProductSkuId));

        List<Long> spuPropertyValueIds = skuPropertyValues.stream().map(MarketingProductSkuPropertyValue::getMarketingSpuPropertyValueId).toList();
        List<MarketingProductSpuPropertyValue> spuPropertyValues = spuPropertyValueMapper.selectListByIds(spuPropertyValueIds);
        Map<Long, MarketingProductSpuPropertyValue> spuPropertyId2SpuPropertyValueMap = spuPropertyValues.stream().collect(
                Collectors.toMap(MarketingProductSpuPropertyValue::getId, Function.identity()));

        List<Long> productPropertyIds = Lists.newArrayList();
        List<Long> productPropertyValueIds = Lists.newArrayList();
        for (MarketingProductSpuPropertyValue spuPropertyValue : spuPropertyValues) {
            productPropertyIds.add(spuPropertyValue.getProductPropertyId());
            if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                productPropertyValueIds.add(spuPropertyValue.getProductPropertyValueId());
            }
        }
        List<ProductProperty> productProperties = productPropertyMapper.selectListByIds(productPropertyIds);
        Map<Long, String> propertyId2NameMap = productProperties.stream().collect(Collectors.toMap(ProductProperty::getId, ProductProperty::getName));
        Map<Long, String> propertyValueId2ValueMap = Maps.newHashMap();
        if (CollectionUtil.isNotEmpty(productPropertyValueIds)) {
            List<ProductPropertyValue> productPropertyValues = productPropertyValueMapper.selectListByIds(productPropertyValueIds);
            propertyValueId2ValueMap = productPropertyValues.stream().collect(Collectors.toMap(
                    ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue));
        }

        List<MarketingProductSku> marketingProductSKus = skuMapper.selectListByIds(skuIds);
        Set<Long> spuIds = marketingProductSKus.stream().map(MarketingProductSku::getMarketingSpuId).collect(Collectors.toSet());
        List<MarketingProductSpu> spus = spuMapper.selectListByIds(spuIds);
        Map<Long, MarketingProductSpu> spuId2SpuMap = spus.stream().collect(Collectors.toMap(MarketingProductSpu::getId, Function.identity()));

        List<ValueAddedSkuRespVO> skuRespVOS = Lists.newArrayList();
        for (MarketingProductSku sku : marketingProductSKus) {
            ValueAddedSkuRespVO skuRespVO = new ValueAddedSkuRespVO();
            skuRespVOS.add(skuRespVO);

            skuRespVO.setId(sku.getId());
            skuRespVO.setName(sku.getName());
            skuRespVO.setApproveStatus(spuId2SpuMap.get(sku.getMarketingSpuId()).getApprovalStatus());
            skuRespVO.setShelvesStatus(spuId2SpuMap.get(sku.getMarketingSpuId()).getShelvesStatus());
            List<PropertyVO> propertyVOS = Lists.newArrayList();
            skuRespVO.setProperties(propertyVOS);
            for (MarketingProductSkuPropertyValue skuPropertyValue : skuId2SpuPropertyValueIdsMap.get(sku.getId())) {
                PropertyVO propertyVO = new PropertyVO();
                propertyVOS.add(propertyVO);
                MarketingProductSpuPropertyValue spuPropertyValue = spuPropertyId2SpuPropertyValueMap.get(skuPropertyValue.getMarketingSpuPropertyValueId());
                propertyVO.setPropertyId(spuPropertyValue.getProductPropertyId());
                propertyVO.setPropertyName(propertyId2NameMap.get(spuPropertyValue.getProductPropertyId()));

                List<PropertyValueVO> propertyValueVOS = Lists.newArrayList();
                propertyVO.setPropertyValues(propertyValueVOS);
                PropertyValueVO propertyValueVO = new PropertyValueVO();
                propertyValueVOS.add(propertyValueVO);
                propertyValueVO.setPropertyValueId(spuPropertyValue.getProductPropertyValueId());
                if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                    propertyValueVO.setPropertyValue(propertyValueId2ValueMap.get(spuPropertyValue.getProductPropertyValueId()));
                } else {
                    propertyValueVO.setPropertyValue(spuPropertyValue.getPropertyValue());
                }
                propertyValueVO.setPropertyPics(Lists.newArrayList(spuPropertyValue.getPicUrl()));
            }
        }
        return skuRespVOS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void del(Long id) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("valueAdded del id: {},operatorId:{}", id, loginUser.getId());
        ValueAdded valueAdded = valueAddedMapper.selectById(id);
        if (valueAdded == null) {
            throw exception(VALUE_ADDED_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(valueAdded.getIsDeleted())) {
            throw exception(VALUE_ADDED_DELETED);
        }
        Date now = new Date();
        valueAdded.setIsDeleted(NumberUtils.INTEGER_ONE);
        valueAdded.setUpdateBy(loginUser.getId());
        valueAdded.setUpdateTime(now);
        valueAddedMapper.updateById(valueAdded);

        valueAddedProductMapper.logicDelByValueAddedId(id, loginUser.getId(), now);
    }

    @Override
    public void changeStatus(ValueAddedStatusChangeReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("valueAdded changeStatus reqVo: {},operatorId:{}", reqVO, loginUser.getId());
        ValueAdded valueAdded = valueAddedMapper.selectById(reqVO.getId());
        if (valueAdded == null) {
            throw exception(VALUE_ADDED_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(valueAdded.getIsDeleted())) {
            throw exception(VALUE_ADDED_DELETED);
        }
        if (valueAdded.getStatus().equals(reqVO.getStatus())) {
            return;
        }
        valueAdded.setStatus(reqVO.getStatus());
        valueAdded.setUpdateBy(loginUser.getId());
        valueAdded.setUpdateTime(new Date());
        valueAddedMapper.updateById(valueAdded);
    }

    @Override
    public List<ValueAddedSimpleInfoRespVO> simpleList() {
        List<ValueAdded> valueAddedList = valueAddedMapper.queryIdAndNameByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(valueAddedList)) {
            return Lists.newArrayList();
        }

        return valueAddedList.stream().map(valueAdded -> {
            ValueAddedSimpleInfoRespVO valueAddedSimpleInfoRespVO = new ValueAddedSimpleInfoRespVO();
            valueAddedSimpleInfoRespVO.setId(valueAdded.getId());
            valueAddedSimpleInfoRespVO.setName(valueAdded.getName());
            return valueAddedSimpleInfoRespVO;
        }).toList();
    }

    @Override
    public List<ValueAddedRespDto> getByIds(Collection<Long> ids) {
        log.info("valueAdded getByIds ids: {}", ids);
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ValueAdded> valueAddedList = valueAddedMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(valueAddedList)) {
            return Collections.emptyList();
        }

        return valueAddedList.stream().map(this::toValueAddedRespDto).toList();
    }

    private ValueAddedRespDto toValueAddedRespDto(ValueAdded valueAdded) {
        ValueAddedRespDto respDto = new ValueAddedRespDto();
        respDto.setId(valueAdded.getId());
        respDto.setCode(valueAdded.getCode());
        respDto.setName(valueAdded.getName());
        respDto.setStatus(valueAdded.getStatus());
        respDto.setSalePrice(valueAdded.getSalePrice());
        respDto.setRenewalPrice(valueAdded.getRenewalPrice());
        respDto.setStrikethroughPrice(valueAdded.getStrikethroughPrice());
        respDto.setIsDeleted(valueAdded.getIsDeleted());
        respDto.setPartnerId(valueAdded.getPartnerId());
        return respDto;
    }

    private ValueAdded buildValueAdded(ValueAddedAddReqVO reqVO, LoginUser<?> loginUser, Date now) {
        ValueAdded valueAdded = new ValueAdded();
        valueAdded.setCode(sequenceGenerator.getValueAddedSequence());
        valueAdded.setName(reqVO.getName());
        valueAdded.setStatus(reqVO.getStatus());
        valueAdded.setSalePrice(reqVO.getSalePrice());
        valueAdded.setRenewalPrice(reqVO.getRenewalPrice());
        valueAdded.setStrikethroughPrice(reqVO.getStrikethroughPrice());
        valueAdded.setServiceOverview(reqVO.getServiceOverview());
        valueAdded.setServiceContent(reqVO.getServiceContent());
        if (CollUtil.isNotEmpty(reqVO.getPicUrls())) {
            valueAdded.setPicUrl(StringUtils.join(reqVO.getPicUrls(), ","));
        } else {
            valueAdded.setPicUrl("");
        }
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
