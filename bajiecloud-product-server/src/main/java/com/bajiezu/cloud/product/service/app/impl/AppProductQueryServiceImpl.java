package com.bajiezu.cloud.product.service.app.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.app.request.AppProductSkuDetailReqVO;
import com.bajiezu.cloud.product.controller.vo.app.request.AppProductSkuListReqVO;
import com.bajiezu.cloud.product.controller.vo.app.request.AppProductSpuDetailReqVO;
import com.bajiezu.cloud.product.controller.vo.app.request.AppProductSpuPageReqVO;
import com.bajiezu.cloud.product.controller.vo.app.response.AppProductSkuRespVO;
import com.bajiezu.cloud.product.controller.vo.app.response.AppProductSpuDetailRespVO;
import com.bajiezu.cloud.product.controller.vo.app.response.AppProductSpuPageRespVO;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSku;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSkuPropertyValue;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpu;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpuRentalMethodProperty;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpuPropertyValue;
import com.bajiezu.cloud.product.dal.entity.ProductProperty;
import com.bajiezu.cloud.product.dal.entity.ProductPropertyValue;
import com.bajiezu.cloud.product.dal.entity.ValueAdded;
import com.bajiezu.cloud.product.dal.entity.ValueAddedCompensationAmountRule;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSkuMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSkuPropertyValueMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSpuMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSpuRentalMethodPropertyMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSpuPropertyValueMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductPropertyMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductPropertyValueMapper;
import com.bajiezu.cloud.product.dal.mapper.ValueAddedMapper;
import com.bajiezu.cloud.product.dal.mapper.ValueAddedCompensationAmountRuleMapper;
import com.bajiezu.cloud.product.enums.ApproveStatusEnum;
import com.bajiezu.cloud.product.enums.RentalMethodEnum;
import com.bajiezu.cloud.product.enums.ShelvesStatusEnum;
import com.bajiezu.cloud.product.service.app.AppProductQueryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppProductQueryServiceImpl implements AppProductQueryService {

    @Resource
    private MarketingProductSpuMapper spuMapper;
    @Resource
    private MarketingProductSkuMapper skuMapper;
    @Resource
    private MarketingProductSkuPropertyValueMapper skuPropertyValueMapper;
    @Resource
    private MarketingProductSpuPropertyValueMapper spuPropertyValueMapper;
    @Resource
    private ProductPropertyMapper productPropertyMapper;
    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;
    @Resource
    private ValueAddedMapper valueAddedMapper;
    @Resource
    private MarketingProductSpuRentalMethodPropertyMapper rentalMethodPropertyMapper;
    @Resource
    private ValueAddedCompensationAmountRuleMapper compensationAmountRuleMapper;

    @Override
    public PageResult<AppProductSpuPageRespVO> spuPage(AppProductSpuPageReqVO req) {
        LambdaQueryWrapper<MarketingProductSpu> query = baseSpu();
        if (StringUtils.isNotBlank(req.getKeyword())) {
            query.like(MarketingProductSpu::getName, req.getKeyword());
        }

        long count = spuMapper.selectCount(query);
        query.orderByDesc(MarketingProductSpu::getCreateTime)
                .last("limit " + ((req.getPageNo() - 1) * req.getPageSize()) + "," + req.getPageSize());

        List<MarketingProductSpu> spus = spuMapper.selectList(query);
        if (CollectionUtil.isEmpty(spus)) {
            return PageResult.empty();
        }

        List<AppProductSpuPageRespVO> list = new ArrayList<>();
        for (MarketingProductSpu spu : spus) {
            List<MarketingProductSku> skus = saleableSkusBySpu(spu.getId());
            if (CollectionUtil.isEmpty(skus)) {
                continue;
            }
            MarketingProductSku defaultSku = skus.get(0);
            AppProductSpuPageRespVO vo = new AppProductSpuPageRespVO();
            vo.setSpuId(spu.getId());
            vo.setName(spu.getName());
            vo.setMainPicUrl(splitFirst(spu.getMainPicUrls()));
            vo.setDefaultSkuId(defaultSku.getId());
            vo.setPriceText(defaultSku.getDailyRent() == null ? null : "¥" + defaultSku.getDailyRent() + "/天");
            vo.setStockStatus(defaultSku.getStock() != null && defaultSku.getStock() > 0 ? 1 : 0);
            vo.setShopButtonText("去下单");
            list.add(vo);
        }
        return new PageResult<>(list, count);
    }

    @Override
    public AppProductSpuDetailRespVO spuDetail(AppProductSpuDetailReqVO req) {
        MarketingProductSpu spu = spuMapper.selectOne(baseSpu().eq(MarketingProductSpu::getId, req.getSpuId()));
        if (spu == null) {
            return null;
        }
        List<MarketingProductSku> skus = saleableSkusBySpu(spu.getId());
        if (CollectionUtil.isEmpty(skus)) {
            return null;
        }

        MarketingProductSku defaultSku = skus.get(0);
        AppProductSpuDetailRespVO resp = new AppProductSpuDetailRespVO();
        resp.setSpuId(spu.getId());
        resp.setName(spu.getName());
        resp.setCondition(spu.getProductCondition());
        resp.setMainPicUrls(split(spu.getMainPicUrls()));
        resp.setCarouselPicUrls(split(spu.getCarouselPicUrls()));
        resp.setVideoUrls(split(spu.getVideoUrls()));
        resp.setDetailPicUrls(split(spu.getDetailPicUrls()));
        resp.setDefaultSkuId(defaultSku.getId());
        resp.setDailyRent(defaultSku.getDailyRent());
        resp.setOfficialPrice(defaultSku.getOfficialPrice());
        resp.setStrikethroughPrice(defaultSku.getStrikethroughPrice());
        resp.setProperties(buildSkuProps(defaultSku.getId()));
        resp.setRentalMethods(buildRentalMethods(spu.getId()));

        if (StringUtils.isNotBlank(spu.getValueAddedIds())) {
            List<Long> ids = Arrays.stream(spu.getValueAddedIds().split(","))
                    .filter(StringUtils::isNotBlank)
                    .map(Long::valueOf)
                    .toList();
            if (CollectionUtil.isEmpty(ids)) {
                return resp;
            }
            List<ValueAdded> values = valueAddedMapper.selectListByIds(ids);
            Map<Long, List<ValueAddedCompensationAmountRule>> ruleMap = buildCompensationAmountRuleMap(ids);
            resp.setValueAddedList(values.stream().map(v -> {
                AppProductSpuDetailRespVO.ValueAddedItem item = new AppProductSpuDetailRespVO.ValueAddedItem();
                item.setId(v.getId());
                item.setName(v.getName());
                item.setPrice(v.getSalePrice());
                item.setIsDefault(Objects.equals(spu.getDefaultSelectedValueAddedId(), v.getId()) ? 1 : 0);
                item.setServiceTypes(v.getServiceTypes());
                item.setEffectiveChannels(v.getEffectiveChannels());
                item.setCompensationStandard(v.getCompensationStandard());
                item.setCompensationLevel(v.getCompensationLevel());
                item.setCompensationLevelLimits(v.getCompensationLevelLimits());
                item.setSlightCompensationRatio(v.getSlightCompensationRatio());
                item.setMediumCompensationRatio(v.getMediumCompensationRatio());
                item.setSevereCompensationRatio(v.getSevereCompensationRatio());
                item.setScrapCompensationRatio(v.getScrapCompensationRatio());
                item.setCompensationAmount(v.getCompensationAmount());
                item.setCompensationAmountRatio(v.getCompensationAmountRatio());
                item.setCompensationAmountRules(buildCompensationAmountRules(ruleMap.get(v.getId())));
                item.setSaleLimits(v.getSaleLimits());
                item.setAnnualLimitPurchaseCount(v.getAnnualLimitPurchaseCount());
                item.setMonthlyLimitPurchaseCount(v.getMonthlyLimitPurchaseCount());
                item.setDailyLimitPurchaseCount(v.getDailyLimitPurchaseCount());
                item.setAccessCondition(v.getAccessCondition());
                item.setAccessConditionLimits(v.getAccessConditionLimits());
                item.setAccessConditionBreachAmount(v.getAccessConditionBreachAmount());
                item.setAccessConditionBreachCount(v.getAccessConditionBreachCount());
                return item;
            }).toList());
        }
        return resp;
    }

    private List<AppProductSpuDetailRespVO.RentalMethodItem> buildRentalMethods(Long spuId) {
        List<MarketingProductSpuRentalMethodProperty> rentalMethodProperties =
                rentalMethodPropertyMapper.selectListByMarketingSpuId(spuId);
        if (CollectionUtil.isEmpty(rentalMethodProperties)) {
            return Collections.emptyList();
        }

        return rentalMethodProperties.stream()
                .collect(Collectors.groupingBy(MarketingProductSpuRentalMethodProperty::getRentalMethod))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    AppProductSpuDetailRespVO.RentalMethodItem item = new AppProductSpuDetailRespVO.RentalMethodItem();
                    item.setRentalMethod(entry.getKey());
                    RentalMethodEnum rentalMethodEnum = RentalMethodEnum.get(entry.getKey());
                    item.setRentalMethodName(rentalMethodEnum == null ? null : rentalMethodEnum.getDesc());
                    item.setRentalPeriods(entry.getValue().stream()
                            .map(MarketingProductSpuRentalMethodProperty::getRentalPeriodMonth)
                            .filter(Objects::nonNull)
                            .distinct()
                            .sorted()
                            .toList());
                    return item;
                }).toList();
    }

    private Map<Long, List<ValueAddedCompensationAmountRule>> buildCompensationAmountRuleMap(List<Long> valueAddedIds) {
        if (CollectionUtil.isEmpty(valueAddedIds)) {
            return Collections.emptyMap();
        }
        List<ValueAddedCompensationAmountRule> rules = compensationAmountRuleMapper.selectListByValueAddedIds(valueAddedIds);
        if (CollectionUtil.isEmpty(rules)) {
            return Collections.emptyMap();
        }
        return rules.stream().collect(Collectors.groupingBy(ValueAddedCompensationAmountRule::getValueAddedId));
    }

    private List<AppProductSpuDetailRespVO.CompensationAmountRuleItem> buildCompensationAmountRules(
            List<ValueAddedCompensationAmountRule> rules) {
        if (CollectionUtil.isEmpty(rules)) {
            return Collections.emptyList();
        }
        return rules.stream().map(rule -> {
            AppProductSpuDetailRespVO.CompensationAmountRuleItem item =
                    new AppProductSpuDetailRespVO.CompensationAmountRuleItem();
            item.setId(rule.getId());
            item.setCompensationAmount(rule.getCompensationAmount());
            item.setCompensationAmountRatio(rule.getCompensationAmountRatio());
            item.setSortOrder(rule.getSortOrder());
            return item;
        }).toList();
    }

    @Override
    public List<AppProductSkuRespVO> skuList(AppProductSkuListReqVO req) {
        return saleableSkusBySpu(req.getSpuId()).stream().map(this::toSku).toList();
    }

    @Override
    public AppProductSkuRespVO skuDetail(AppProductSkuDetailReqVO req) {
        MarketingProductSku sku = skuMapper.selectById(req.getSkuId());
        if (sku == null || !isSkuSaleable(sku)) {
            return null;
        }
        MarketingProductSpu spu = spuMapper.selectById(sku.getMarketingSpuId());
        if (spu == null || Boolean.TRUE.equals(spu.getIsDeleted())) {
            return null;
        }
        return toSku(sku);
    }

    private AppProductSkuRespVO toSku(MarketingProductSku sku) {
        AppProductSkuRespVO vo = new AppProductSkuRespVO();
        vo.setSkuId(sku.getId());
        vo.setDailyRent(sku.getDailyRent());
        vo.setTotalRent(sku.getTotalRent());
        vo.setBuyoutAmount(sku.getBuyoutAmount());
        vo.setStock(sku.getStock());
        vo.setIsAllowOrder(sku.getIsAllowOrder());
        vo.setOfficialPrice(sku.getOfficialPrice());
        vo.setStrikethroughPrice(sku.getStrikethroughPrice());

        List<AppProductSkuRespVO.PropertyValueItem> properties = buildSkuProps(sku.getId());
        vo.setPropertyValues(properties);

        String skuPicUrl = properties.stream()
                .map(AppProductSkuRespVO.PropertyValueItem::getPicUrl)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
        vo.setPicUrl(skuPicUrl);
        return vo;
    }

    private List<AppProductSkuRespVO.PropertyValueItem> buildSkuProps(Long skuId) {
        List<MarketingProductSkuPropertyValue> links = skuPropertyValueMapper.selectListBySkuIds(List.of(skuId));
        if (CollectionUtil.isEmpty(links)) {
            return Collections.emptyList();
        }
        List<Long> spuValueIds = links.stream().map(MarketingProductSkuPropertyValue::getMarketingSpuPropertyValueId).toList();
        Map<Long, MarketingProductSpuPropertyValue> spuValueMap = spuPropertyValueMapper.selectListByIds(spuValueIds).stream()
                .collect(Collectors.toMap(MarketingProductSpuPropertyValue::getId, e -> e));

        Set<Long> propertyIds = spuValueMap.values().stream()
                .map(MarketingProductSpuPropertyValue::getProductPropertyId)
                .collect(Collectors.toSet());
        Set<Long> propertyValueIds = spuValueMap.values().stream()
                .map(MarketingProductSpuPropertyValue::getProductPropertyValueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> propertyNameMap = productPropertyMapper.selectListByIds(propertyIds).stream()
                .collect(Collectors.toMap(ProductProperty::getId, ProductProperty::getName));
        Map<Long, String> propertyValueMap = propertyValueIds.isEmpty() ? Collections.emptyMap() :
                productPropertyValueMapper.selectListByIds(propertyValueIds).stream()
                        .collect(Collectors.toMap(ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue));

        List<AppProductSkuRespVO.PropertyValueItem> result = new ArrayList<>();
        for (MarketingProductSkuPropertyValue link : links) {
            MarketingProductSpuPropertyValue spuValue = spuValueMap.get(link.getMarketingSpuPropertyValueId());
            if (spuValue == null) {
                continue;
            }
            AppProductSkuRespVO.PropertyValueItem item = new AppProductSkuRespVO.PropertyValueItem();
            item.setPropertyId(spuValue.getProductPropertyId());
            item.setPropertyName(propertyNameMap.get(spuValue.getProductPropertyId()));
            item.setPropertyValue(spuValue.getProductPropertyValueId() == null ? spuValue.getPropertyValue() :
                    propertyValueMap.get(spuValue.getProductPropertyValueId()));
            item.setPicUrl(spuValue.getPicUrl());
            item.setMarketingCornerText(spuValue.getMarketingCornerText());
            result.add(item);
        }
        return result;
    }

    private List<MarketingProductSku> saleableSkusBySpu(Long spuId) {
        return skuMapper.selectList(new LambdaQueryWrapper<MarketingProductSku>()
                .eq(MarketingProductSku::getMarketingSpuId, spuId)
                .eq(MarketingProductSku::getIsDeleted, false)
                .eq(MarketingProductSku::getIsAllowOrder, 1)
                .gt(MarketingProductSku::getStock, 0)
                .orderByAsc(MarketingProductSku::getDailyRent));
    }

    private boolean isSkuSaleable(MarketingProductSku sku) {
        return !Boolean.TRUE.equals(sku.getIsDeleted())
                && Objects.equals(sku.getIsAllowOrder(), 1)
                && sku.getStock() != null
                && sku.getStock() > 0;
    }

    private LambdaQueryWrapper<MarketingProductSpu> baseSpu() {
        return new LambdaQueryWrapper<MarketingProductSpu>()
                .eq(MarketingProductSpu::getIsDeleted, false)
                .eq(MarketingProductSpu::getShelvesStatus, ShelvesStatusEnum.ON_SHELVES.getValue())
                .eq(MarketingProductSpu::getApprovalStatus, ApproveStatusEnum.APPROVE_PASS.getValue());
    }

    private List<String> split(String value) {
        return StringUtils.isBlank(value) ? Collections.emptyList() :
                Arrays.stream(value.split(",")).filter(StringUtils::isNotBlank).toList();
    }

    private String splitFirst(String value) {
        List<String> list = split(value);
        return CollectionUtil.isEmpty(list) ? null : list.get(0);
    }
}
