package com.bajiezu.cloud.product.service.app.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
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
import com.bajiezu.cloud.product.dal.entity.MarketingProductSkuRentalMethodProperty;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpu;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpuProperty;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpuRentalMethodProperty;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpuPropertyValue;
import com.bajiezu.cloud.product.dal.entity.ProductProperty;
import com.bajiezu.cloud.product.dal.entity.ProductPropertyValue;
import com.bajiezu.cloud.product.dal.entity.StandardProductSpu;
import com.bajiezu.cloud.product.dal.entity.ValueAdded;
import com.bajiezu.cloud.product.dal.entity.ValueAddedCompensationAmountRule;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSkuMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSkuPropertyValueMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSkuRentalMethodPropertyMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSpuMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSpuPropertyMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSpuRentalMethodPropertyMapper;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSpuPropertyValueMapper;
import com.bajiezu.cloud.product.dal.mapper.ExpressTemplateMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductBrandMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductBusinessCategoryMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductMarketingCategoryMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductPropertyMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductPropertyValueMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductTagMapper;
import com.bajiezu.cloud.product.dal.mapper.StandardProductSpuMapper;
import com.bajiezu.cloud.product.dal.mapper.ValueAddedMapper;
import com.bajiezu.cloud.product.dal.mapper.ValueAddedCompensationAmountRuleMapper;
import com.bajiezu.cloud.product.enums.ApproveStatusEnum;
import com.bajiezu.cloud.product.enums.RentalMethodEnum;
import com.bajiezu.cloud.product.enums.ShelvesStatusEnum;
import com.bajiezu.cloud.product.service.app.AppProductQueryService;
import com.bajiezu.cloud.system.api.area.AreaApi;
import com.bajiezu.cloud.system.dto.AreaCodeAndNameDTO;
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
import java.util.function.Function;
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
    private MarketingProductSpuPropertyMapper spuPropertyMapper;
    @Resource
    private StandardProductSpuMapper standardProductSpuMapper;
    @Resource
    private ProductPropertyMapper productPropertyMapper;
    @Resource
    private ProductBusinessCategoryMapper businessCategoryMapper;
    @Resource
    private ProductMarketingCategoryMapper marketingCategoryMapper;
    @Resource
    private ProductBrandMapper brandMapper;
    @Resource
    private ProductTagMapper tagMapper;
    @Resource
    private ExpressTemplateMapper expressTemplateMapper;
    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;
    @Resource
    private ValueAddedMapper valueAddedMapper;
    @Resource
    private MarketingProductSpuRentalMethodPropertyMapper rentalMethodPropertyMapper;
    @Resource
    private MarketingProductSkuRentalMethodPropertyMapper skuRentalMethodPropertyMapper;
    @Resource
    private ValueAddedCompensationAmountRuleMapper compensationAmountRuleMapper;
    @Resource
    private AreaApi areaApi;

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
        resp.setCode(spu.getCode());
        resp.setType(spu.getType());
        resp.setName(spu.getName());
        resp.setCondition(spu.getProductCondition());
        resp.setProductCondition(spu.getProductCondition());
        resp.setMonitorAttribute(spu.getMonitorAttribute());
        resp.setMainPicUrls(split(spu.getMainPicUrls()));
        resp.setCarouselPicUrls(split(spu.getCarouselPicUrls()));
        resp.setVideoUrls(split(spu.getVideoUrls()));
        resp.setDetailPicUrls(split(spu.getDetailPicUrls()));
        resp.setDetailTags(buildTags(spu.getDetailTagIds()));
        resp.setSkuTags(buildTags(spu.getSkuTagIds()));
        resp.setDefaultSkuId(defaultSku.getId());
        resp.setDailyRent(defaultSku.getDailyRent());
        resp.setOfficialPrice(defaultSku.getOfficialPrice());
        resp.setStrikethroughPrice(defaultSku.getStrikethroughPrice());
        resp.setProperties(buildSkuProps(defaultSku.getId()));
        resp.setSpuProperties(buildSpuProperties(spu.getId()));
        resp.setRentalMethods(buildRentalMethods(spu.getId()));
        resp.setSkuRentalMethodProperties(buildSkuRentalMethodProperties(spu.getId()));
        resp.setSkus(buildSkuList(skus));
        resp.setShowPages(parseIntegerList(spu.getShowPage()));
        resp.setIsDefaultSelected(spu.getIsDefaultSelected());
        resp.setDefaultSelectedValueAddedId(spu.getDefaultSelectedValueAddedId());
        resp.setCompensationRuleId(spu.getCompensationRuleId());
        resp.setShippingWay(spu.getShippingWay());
        resp.setShippingTemplateId(spu.getShippingTemplateId());
        if (spu.getShippingTemplateId() != null) {
            resp.setShippingTemplateName(expressTemplateMapper.selectNameById(spu.getShippingTemplateId()));
        }
        resp.setShippingAreaCodes(buildShippingAreas(spu.getShippingAreaCodes()));
        resp.setReceivingAddress(spu.getReceivingAddress());

        fillStandardProductInfo(resp, spu);
        fillValueAddedInfo(resp, spu);
        return resp;
    }

    private void fillStandardProductInfo(AppProductSpuDetailRespVO resp, MarketingProductSpu spu) {
        if (spu.getStandardProductSpuId() == null) {
            return;
        }
        StandardProductSpu standardProductSpu = standardProductSpuMapper.selectById(spu.getStandardProductSpuId());
        if (standardProductSpu == null) {
            return;
        }
        resp.setStandardProductSpuId(standardProductSpu.getId());
        resp.setStandardProductSpuCode(standardProductSpu.getCode());
        resp.setStandardProductSpuName(standardProductSpu.getName());
        List<String> businessCategoryNames = standardProductSpu.getBusinessCategoryId() == null ? Collections.emptyList() :
                businessCategoryMapper.selectSelfAndParentNamesById(standardProductSpu.getBusinessCategoryId());
        resp.setBusinessCategoryName(CollectionUtil.isEmpty(businessCategoryNames) ? null : String.join(">", businessCategoryNames));
        List<String> marketingCategoryNames = standardProductSpu.getMarketingCategoryId() == null ? Collections.emptyList() :
                marketingCategoryMapper.selectSelfAndParentNamesById(standardProductSpu.getMarketingCategoryId());
        resp.setMarketingCategoryName(CollectionUtil.isEmpty(marketingCategoryNames) ? null : String.join(">", marketingCategoryNames));
        String brandName = standardProductSpu.getProductBrandId() == null ? null : brandMapper.selectNameById(standardProductSpu.getProductBrandId());
        resp.setBrand(brandName);
        resp.setBrandName(brandName);
    }

    private void fillValueAddedInfo(AppProductSpuDetailRespVO resp, MarketingProductSpu spu) {
        List<Long> ids = parseLongList(spu.getValueAddedIds());
        List<Long> queryIds = new ArrayList<>(ids);
        if (spu.getDefaultSelectedValueAddedId() != null && !queryIds.contains(spu.getDefaultSelectedValueAddedId())) {
            queryIds.add(spu.getDefaultSelectedValueAddedId());
        }
        if (CollectionUtil.isEmpty(queryIds)) {
            return;
        }
        List<ValueAdded> values = valueAddedMapper.selectListByIds(queryIds);
        Map<Long, ValueAdded> valueMap = CollectionUtil.isEmpty(values) ? Collections.emptyMap() :
                values.stream().collect(Collectors.toMap(ValueAdded::getId, Function.identity(), (a, b) -> a));
        if (spu.getDefaultSelectedValueAddedId() != null) {
            ValueAdded defaultValue = valueMap.get(spu.getDefaultSelectedValueAddedId());
            resp.setDefaultSelectedValueAddedName(defaultValue == null ? null : defaultValue.getName());
        }
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        Map<Long, List<ValueAddedCompensationAmountRule>> ruleMap = buildCompensationAmountRuleMap(ids);
        resp.setValueAddedList(ids.stream()
                .map(valueMap::get)
                .filter(Objects::nonNull)
                .map(v -> {
                    AppProductSpuDetailRespVO.ValueAddedItem item = new AppProductSpuDetailRespVO.ValueAddedItem();
                    item.setId(v.getId());
                    item.setName(v.getName());
                    item.setPrice(v.getSalePrice());
                    item.setIsDefault(Objects.equals(spu.getDefaultSelectedValueAddedId(), v.getId()) ? 1 : 0);
                    item.setServiceOverview(v.getServiceOverview());
                    item.setServiceContent(v.getServiceContent());
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

    private List<AppProductSpuDetailRespVO.IdNameItem> buildTags(String tagIds) {
        List<Long> ids = parseLongList(tagIds);
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<AppProductSpuDetailRespVO.IdNameItem> tags = tagMapper.selectTagsByIds(ids).stream().map(tag -> {
            AppProductSpuDetailRespVO.IdNameItem item = new AppProductSpuDetailRespVO.IdNameItem();
            item.setId(tag.getId());
            item.setName(tag.getName());
            return item;
        }).toList();
        Map<Long, String> tagNameMap = tags.stream()
                .collect(Collectors.toMap(AppProductSpuDetailRespVO.IdNameItem::getId, AppProductSpuDetailRespVO.IdNameItem::getName, (a, b) -> a));
        return ids.stream().map(id -> {
            AppProductSpuDetailRespVO.IdNameItem item = new AppProductSpuDetailRespVO.IdNameItem();
            item.setId(id);
            item.setName(tagNameMap.get(id));
            return item;
        }).toList();
    }

    private List<AppProductSpuDetailRespVO.SpuPropertyItem> buildSpuProperties(Long spuId) {
        List<MarketingProductSpuProperty> spuProperties = spuPropertyMapper.selectListByMarketingSpuId(spuId);
        if (CollectionUtil.isEmpty(spuProperties)) {
            return Collections.emptyList();
        }
        List<Long> propertyIds = spuProperties.stream()
                .map(MarketingProductSpuProperty::getProductPropertyId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> propertyNameMap = CollectionUtil.isEmpty(propertyIds) ? Collections.emptyMap() :
                productPropertyMapper.selectListByIds(propertyIds).stream()
                        .collect(Collectors.toMap(ProductProperty::getId, ProductProperty::getName, (a, b) -> a));

        List<MarketingProductSpuPropertyValue> spuPropertyValues = spuPropertyValueMapper.selectListByMarketingSpuId(spuId);
        Map<Long, List<MarketingProductSpuPropertyValue>> spuPropertyIdValueMap = CollectionUtil.isEmpty(spuPropertyValues) ? Collections.emptyMap() :
                spuPropertyValues.stream().collect(Collectors.groupingBy(MarketingProductSpuPropertyValue::getSpuPropertyId));
        List<Long> propertyValueIds = CollectionUtil.isEmpty(spuPropertyValues) ? Collections.emptyList() :
                spuPropertyValues.stream()
                        .map(MarketingProductSpuPropertyValue::getProductPropertyValueId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        Map<Long, String> propertyValueMap = CollectionUtil.isEmpty(propertyValueIds) ? Collections.emptyMap() :
                productPropertyValueMapper.selectListByIds(propertyValueIds).stream()
                        .collect(Collectors.toMap(ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue, (a, b) -> a));

        return spuProperties.stream().map(spuProperty -> {
            AppProductSpuDetailRespVO.SpuPropertyItem item = new AppProductSpuDetailRespVO.SpuPropertyItem();
            item.setPropertyId(spuProperty.getProductPropertyId());
            item.setPropertyName(propertyNameMap.get(spuProperty.getProductPropertyId()));
            item.setSort(spuProperty.getSort());
            item.setIsAddPropertyPic(spuProperty.getIsAddPropertyPic());
            item.setIsAddMarketingCorner(spuProperty.getIsAddMarketingCorner());
            item.setIsSkuProperty(spuProperty.getIsSkuProperty());
            item.setPropertyValues(spuPropertyIdValueMap.getOrDefault(spuProperty.getId(), Collections.emptyList()).stream()
                    .map(value -> {
                        AppProductSpuDetailRespVO.SpuPropertyValueItem valueItem = new AppProductSpuDetailRespVO.SpuPropertyValueItem();
                        valueItem.setProductPropertyValueId(value.getProductPropertyValueId());
                        valueItem.setValue(value.getProductPropertyValueId() == null ? value.getPropertyValue() :
                                propertyValueMap.get(value.getProductPropertyValueId()));
                        valueItem.setSort(value.getSort());
                        valueItem.setPicUrl(value.getPicUrl());
                        valueItem.setMarketingCornerText(value.getMarketingCornerText());
                        return valueItem;
                    }).toList());
            return item;
        }).toList();
    }

    private List<AppProductSpuDetailRespVO.AreaItem> buildShippingAreas(String shippingAreaCodes) {
        List<String> areaCodes = split(shippingAreaCodes);
        if (CollectionUtil.isEmpty(areaCodes)) {
            return Collections.emptyList();
        }
        CommonResult<List<AreaCodeAndNameDTO>> areaResult = areaApi.getByAreaCodes(Set.copyOf(areaCodes));
        Map<String, String> areaNameMap = areaResult != null && areaResult.isSuccess() && CollectionUtil.isNotEmpty(areaResult.getData()) ?
                areaResult.getData().stream().collect(Collectors.toMap(AreaCodeAndNameDTO::getCode, AreaCodeAndNameDTO::getName, (a, b) -> a)) :
                Collections.emptyMap();
        return areaCodes.stream().map(areaCode -> {
            AppProductSpuDetailRespVO.AreaItem item = new AppProductSpuDetailRespVO.AreaItem();
            item.setAreaCode(areaCode);
            item.setAreaName(areaNameMap.get(areaCode));
            return item;
        }).toList();
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

    private List<AppProductSpuDetailRespVO.SkuRentalMethodPropertyItem> buildSkuRentalMethodProperties(Long spuId) {
        List<MarketingProductSkuRentalMethodProperty> properties =
                skuRentalMethodPropertyMapper.selectListByMarketingSpuId(spuId);
        if (CollectionUtil.isEmpty(properties)) {
            return Collections.emptyList();
        }
        return properties.stream().map(property -> {
            AppProductSpuDetailRespVO.SkuRentalMethodPropertyItem item =
                    new AppProductSpuDetailRespVO.SkuRentalMethodPropertyItem();
            item.setSkuId(property.getMarketingSkuId());
            item.setRentalMethod(property.getRentalMethod());
            RentalMethodEnum rentalMethodEnum = RentalMethodEnum.get(property.getRentalMethod());
            item.setRentalMethodName(rentalMethodEnum == null ? null : rentalMethodEnum.getDesc());
            item.setRentalPeriodMonth(property.getRentalPeriodMonth());
            item.setTotalRent(property.getTotalRent());
            item.setMonthlyRent(property.getMonthlyRent());
            item.setDailyRent(property.getDailyRent());
            item.setBuyoutAmount(property.getBuyoutAmount());
            item.setPremium(property.getPremium());
            item.setStock(property.getStock());
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
        return buildSkuList(saleableSkusBySpu(req.getSpuId()));
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

    private List<AppProductSkuRespVO> buildSkuList(List<MarketingProductSku> skus) {
        if (CollectionUtil.isEmpty(skus)) {
            return Collections.emptyList();
        }
        List<Long> skuIds = skus.stream().map(MarketingProductSku::getId).toList();
        Map<Long, List<MarketingProductSkuRentalMethodProperty>> rentalPropertyMap =
                skuRentalMethodPropertyMapper.selectListByMarketingSkuIds(skuIds).stream()
                        .collect(Collectors.groupingBy(MarketingProductSkuRentalMethodProperty::getMarketingSkuId));
        return skus.stream().map(sku -> toSku(sku, rentalPropertyMap.get(sku.getId()))).toList();
    }

    private AppProductSkuRespVO toSku(MarketingProductSku sku) {
        List<MarketingProductSkuRentalMethodProperty> rentalProperties =
                skuRentalMethodPropertyMapper.selectListByMarketingSkuIds(List.of(sku.getId()));
        return toSku(sku, rentalProperties);
    }

    private AppProductSkuRespVO toSku(MarketingProductSku sku, List<MarketingProductSkuRentalMethodProperty> rentalProperties) {
        AppProductSkuRespVO vo = new AppProductSkuRespVO();
        vo.setSkuId(sku.getId());
        vo.setSkuCode(sku.getSkuCode());
        vo.setDailyRent(sku.getDailyRent());
        vo.setTotalRent(sku.getTotalRent());
        vo.setBuyoutAmount(sku.getBuyoutAmount());
        vo.setStock(sku.getStock());
        vo.setIsAllowOrder(sku.getIsAllowOrder());
        vo.setOfficialPrice(sku.getOfficialPrice());
        vo.setStrikethroughPrice(sku.getStrikethroughPrice());
        vo.setTotalPriceFactor(sku.getTotalPriceFactor());
        vo.setTotalRentFactor(sku.getTotalRentFactor());
        vo.setTotalPrice(sku.getTotalPrice());
        vo.setPremium(sku.getPremium());
        vo.setSuggestedRetailPrice(sku.getSuggestedRetailPrice());
        vo.setCashUsageRatio(sku.getCashUsageRatio());
        vo.setPointsUsageRatio(sku.getPointsUsageRatio());
        vo.setPointsCount(sku.getPointsCount());
        vo.setCashPrice(sku.getCashPrice());
        vo.setRentalMethodProperties(buildSkuRentalMethodPropertyItems(rentalProperties));

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

    private List<AppProductSkuRespVO.RentalMethodPropertyItem> buildSkuRentalMethodPropertyItems(
            List<MarketingProductSkuRentalMethodProperty> properties) {
        if (CollectionUtil.isEmpty(properties)) {
            return Collections.emptyList();
        }
        return properties.stream().map(property -> {
            AppProductSkuRespVO.RentalMethodPropertyItem item = new AppProductSkuRespVO.RentalMethodPropertyItem();
            item.setRentalMethod(property.getRentalMethod());
            RentalMethodEnum rentalMethodEnum = RentalMethodEnum.get(property.getRentalMethod());
            item.setRentalMethodName(rentalMethodEnum == null ? null : rentalMethodEnum.getDesc());
            item.setRentalPeriodMonth(property.getRentalPeriodMonth());
            item.setTotalRent(property.getTotalRent());
            item.setMonthlyRent(property.getMonthlyRent());
            item.setDailyRent(property.getDailyRent());
            item.setBuyoutAmount(property.getBuyoutAmount());
            item.setPremium(property.getPremium());
            item.setStock(property.getStock());
            return item;
        }).toList();
    }

    private List<AppProductSkuRespVO.PropertyValueItem> buildSkuProps(Long skuId) {
        List<MarketingProductSkuPropertyValue> links = skuPropertyValueMapper.selectListBySkuIds(List.of(skuId));
        if (CollectionUtil.isEmpty(links)) {
            return Collections.emptyList();
        }
        List<Long> spuValueIds = links.stream()
                .map(MarketingProductSkuPropertyValue::getMarketingSpuPropertyValueId)
                .filter(Objects::nonNull)
                .toList();
        if (CollectionUtil.isEmpty(spuValueIds)) {
            return Collections.emptyList();
        }
        Map<Long, MarketingProductSpuPropertyValue> spuValueMap = spuPropertyValueMapper.selectListByIds(spuValueIds).stream()
                .collect(Collectors.toMap(MarketingProductSpuPropertyValue::getId, e -> e, (a, b) -> a));

        Set<Long> propertyIds = spuValueMap.values().stream()
                .map(MarketingProductSpuPropertyValue::getProductPropertyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> propertyValueIds = spuValueMap.values().stream()
                .map(MarketingProductSpuPropertyValue::getProductPropertyValueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> propertyNameMap = propertyIds.isEmpty() ? Collections.emptyMap() :
                productPropertyMapper.selectListByIds(propertyIds).stream()
                        .collect(Collectors.toMap(ProductProperty::getId, ProductProperty::getName, (a, b) -> a));
        Map<Long, String> propertyValueMap = propertyValueIds.isEmpty() ? Collections.emptyMap() :
                productPropertyValueMapper.selectListByIds(propertyValueIds).stream()
                        .collect(Collectors.toMap(ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue, (a, b) -> a));

        List<AppProductSkuRespVO.PropertyValueItem> result = new ArrayList<>();
        for (MarketingProductSkuPropertyValue link : links) {
            MarketingProductSpuPropertyValue spuValue = spuValueMap.get(link.getMarketingSpuPropertyValueId());
            if (spuValue == null) {
                continue;
            }
            AppProductSkuRespVO.PropertyValueItem item = new AppProductSkuRespVO.PropertyValueItem();
            item.setPropertyId(spuValue.getProductPropertyId());
            item.setPropertyName(propertyNameMap.get(spuValue.getProductPropertyId()));
            item.setPropertyValueId(spuValue.getProductPropertyValueId());
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

    private List<Long> parseLongList(String value) {
        return StringUtils.isBlank(value) ? Collections.emptyList() :
                Arrays.stream(value.split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::valueOf)
                        .toList();
    }

    private List<Integer> parseIntegerList(String value) {
        return StringUtils.isBlank(value) ? Collections.emptyList() :
                Arrays.stream(value.split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Integer::valueOf)
                        .toList();
    }

    private List<String> split(String value) {
        return StringUtils.isBlank(value) ? Collections.emptyList() :
                Arrays.stream(value.split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .toList();
    }

    private String splitFirst(String value) {
        List<String> list = split(value);
        return CollectionUtil.isEmpty(list) ? null : list.get(0);
    }
}
