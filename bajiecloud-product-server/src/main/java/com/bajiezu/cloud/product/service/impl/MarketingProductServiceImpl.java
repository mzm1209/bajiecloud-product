package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.bajiezu.cloud.common.constants.OperateTypeEnum;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.FeginMethodExecuteUtils;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.marketing.api.channel.MarketingChannelApi;
import com.bajiezu.cloud.marketing.dto.channel.req.MarketingChannelIdsReqDTO;
import com.bajiezu.cloud.marketing.dto.channel.resp.MarketingChannelRespDTO;
import com.bajiezu.cloud.product.controller.MarketingProductPropertyValueVO;
import com.bajiezu.cloud.product.controller.vo.*;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.*;
import com.bajiezu.cloud.product.dal.dto.*;
import com.bajiezu.cloud.product.dal.entity.*;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.dto.*;
import com.bajiezu.cloud.product.enums.*;
import com.bajiezu.cloud.product.service.MarketingProductService;
import com.bajiezu.cloud.product.util.SequenceGenerator;
import com.bajiezu.cloud.system.api.area.AreaApi;
import com.bajiezu.cloud.system.api.user.AdminUserApi;
import com.bajiezu.cloud.system.dto.AdminUserRespDTO;
import com.bajiezu.cloud.system.dto.AreaCodeAndNameDTO;
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
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.*;

@Service
@Slf4j
public class MarketingProductServiceImpl implements MarketingProductService {
    private static final int MARKETING_CORNER_TEXT_MAX_LENGTH = 64;
    private static final Set<Integer> SUPPORTED_RENTAL_PERIOD_MONTHS = Set.of(3, 6, 12);

    @Resource
    private SequenceGenerator sequenceGenerator;
    @Resource
    private MarketingProductSpuMapper marketingProductSpuMapper;
    @Resource
    private MarketingProductSpuPropertyMapper spuPropertyMapper;
    @Resource
    private MarketingProductSpuPropertyValueMapper spuPropertyValueMapper;
    @Resource
    private MarketingProductSkuMapper skuMapper;
    @Resource
    private MarketingProductSkuPropertyValueMapper skuPropertyValueMapper;
    @Resource
    private StandardProductSpuMapper standardProductSpuMapper;
    @Resource
    private StandardProductSpuPropertyMapper standardProductSpuPropertyMapper;
    @Resource
    private StandardProductSpuPropertyValueMapper standardProductSpuPropertyValueMapper;
    @Resource
    private ProductPropertyMapper productPropertyMapper;
    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;
    @Resource
    private ProductBusinessCategoryMapper businessCategoryMapper;
    @Resource
    private ProductMarketingCategoryMapper marketingCategoryMapper;
    @Resource
    private ProductBrandMapper brandMapper;
    @Resource
    private ProductTagMapper tagMapper;
    @Resource
    private ValueAddedMapper valueAddedMapper;
    @Resource
    private ExpressTemplateMapper expressTemplateMapper;
    @Resource
    private MarketingProductSpuRentalMethodPropertyMapper rentalMethodPropertyMapper;
    @Resource
    private MarketingProductSkuRentalMethodPropertyMapper skuRentalMethodPropertyMapper;
    @Resource
    private StandardProductSkuPropertyValueMapper standardProductSkuPropertyValueMapper;
    @Resource
    private AssetResidualConfigMapper assetResidualConfigMapper;
    @Resource
    private AssetResidualMonthConfigMapper assetResidualMonthConfigMapper;
    @Resource
    private AssetResidualYearConfigMapper assetResidualYearConfigMapper;
    @Resource
    private AssetPricingConfigMapper assetPricingConfigMapper;

    @Resource
    private MarketingChannelApi marketingChannelApi;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private AreaApi areaApi;

    @Override
    public PageResult<MarketingProductRespVO> page(MarketingProductListReqVO reqVO) {
        log.info("list marketingProduct reqVO: {}", reqVO);

        MarketingProductQuery query = reqVO.toQuery();
        // 审批状态 和上下架状态的不应该是草稿状态的商品
        if (reqVO.getApprovalStatus() != null || reqVO.getShelvesStatus() != null) {
            query.setIsDraft(NumberUtils.INTEGER_ZERO);
        }

        // 根据品牌、营销类目搜索
        if (reqVO.getBrandId() != null || reqVO.getMarketingCategoryId() != null) {
            List<Long> standardProductSpuIds = standardProductSpuMapper.selectIdsByBrandIdAndMarketingCategoryId(
                    reqVO.getBrandId(), reqVO.getMarketingCategoryId());
            if (CollectionUtil.isEmpty(standardProductSpuIds)) {
                log.info("根据brandId:{},marketingCategoryId:{}未查询到记录", reqVO.getBrandId(), reqVO.getMarketingCategoryId());
                return PageResult.empty();
            }
            query.setStandardProductSpuIds(standardProductSpuIds);
        }

        // 根据颜色、规格搜索
        if (StringUtils.isNotBlank(reqVO.getColor()) || StringUtils.isNotBlank(reqVO.getSpecification())) {
            List<Long> propertyValueIds = Lists.newArrayList();
            if (StringUtils.isNotBlank(reqVO.getColor())) {
                Long colorPropertyValueId = productPropertyValueMapper.selectPropertyValueIdByPropertyNameAndValue("颜色", reqVO.getColor());
                if (Objects.isNull(colorPropertyValueId)) {
                    log.info("根据颜色:{}未查询到记录", reqVO.getColor());
                    return PageResult.empty();
                }
                propertyValueIds.add(colorPropertyValueId);
            }
            if (StringUtils.isNotBlank(reqVO.getSpecification())) {
                Long colorPropertyValueId = productPropertyValueMapper.selectPropertyValueIdByPropertyNameAndValue("规格", reqVO.getSpecification());
                if (Objects.isNull(colorPropertyValueId)) {
                    log.info("根据规格:{}未查询到记录", reqVO.getColor());
                    return PageResult.empty();
                }
                propertyValueIds.add(colorPropertyValueId);
            }
            List<Long> marketingProductSpuIds = spuPropertyValueMapper.selectMarketingSpuIdsByPropertyValueIds(propertyValueIds, propertyValueIds.size());
            if (CollUtil.isEmpty(marketingProductSpuIds)) {
                log.info("根据color:{},specification:{}未查询到记录", reqVO.getColor(), reqVO.getSpecification());
                return PageResult.empty();
            }
            query.setIds(marketingProductSpuIds);
        }

        // 获取列表
        List<MarketingProductSpu> marketingProductSpus = marketingProductSpuMapper.selectListByQuery(query);
        if (CollUtil.isEmpty(marketingProductSpus)) {
            return PageResult.empty();
        }
        // 获取总数
        long count = marketingProductSpuMapper.selectCountByQuery(query);

        // 构造返回结果
        List<MarketingProductRespVO> marketingProductRespVOS = buildListResult(marketingProductSpus, reqVO.getProductType());

        return new PageResult<>(marketingProductRespVOS, count);
    }

    @Override
    public ProductTypeStatisticRespVO productTypeStatistic() {
        List<ProductTypeStatisticCountDTO> productTypeStatisticCountDTOS = marketingProductSpuMapper.productTypeStatistic();
        ProductTypeStatisticRespVO productTypeStatisticRespVO = new ProductTypeStatisticRespVO();
        for (ProductTypeStatisticCountDTO productTypeStatisticCountDTO : productTypeStatisticCountDTOS) {
            if (productTypeStatisticCountDTO.getType() == null) {
                continue;
            }
            ProductTypeEnum productTypeEnum = ProductTypeEnum.get(productTypeStatisticCountDTO.getType());
            switch (productTypeEnum) {
                case RENTAL_PRODUCT:
                    productTypeStatisticRespVO.setRentalProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                case PRODUCT_FOR_SALE:
                    productTypeStatisticRespVO.setProductForSaleCount(productTypeStatisticCountDTO.getCount());
                    break;
                case RECYCLED_PRODUCT:
                    productTypeStatisticRespVO.setRecycledProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                case PHYSICAL_PRODUCT:
                    productTypeStatisticRespVO.setPhysicalProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                case VIRTUAL_PRODUCT:
                    productTypeStatisticRespVO.setVirtualProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                default:
                    break;
            }
        }
        return productTypeStatisticRespVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(MarketingProductAddReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("add marketingProduct reqVO: {},operatorId:{}", reqVO, loginUser.getId());

        // 获取标准商品
        StandardProductSpu standardProductSpu = standardProductSpuMapper.selectById(reqVO.getStandardProductSpuId());
        if (standardProductSpu == null) {
            throw exception(STANDARD_PRODUCT_NOT_EXIST);
        }
        if (Objects.equals(standardProductSpu.getIsDeleted(), NumberUtils.INTEGER_ONE)) {
            throw exception(STANDARD_PRODUCT_DELETED);
        }
        validateSpuPropertiesInStandardScope(reqVO.getStandardProductSpuId(), reqVO.getSpuProperties());

        // 构建并保存营销商品spu
        Date now = new Date();
        MarketingProductSpu marketingProductSpu = buildMarketingProductSpu(reqVO, loginUser, now);
        marketingProductSpuMapper.insert(marketingProductSpu);
        Long marketingProductSpuId = marketingProductSpu.getId();

        // 保存租赁方式配置，独立于SPU/SKU属性，不参与SKU生成
        saveRentalMethods(marketingProductSpuId, reqVO.getType(), reqVO.getRentalMethods(), loginUser, now);

        // 保存商品SPU属性
        List<MarketingProductSpuProperty> spuProperties = buildMarketingProductSpuProperty(reqVO.getSpuProperties(),
                loginUser, marketingProductSpuId, now);
        if (CollUtil.isEmpty(spuProperties)) {
            log.info("no spu property");
            return;
        }
        spuPropertyMapper.insertBatch(spuProperties);

        // 保存商品SPU属性值
        Map<Long, Long> spuPropertyIdMap = spuProperties.stream().collect(Collectors.toMap(MarketingProductSpuProperty::getProductPropertyId,
                MarketingProductSpuProperty::getId));
        List<MarketingProductSpuPropertyValue> spuPropertyValues = buildMarketingProductSpuPropertyValues(reqVO.getSpuProperties(),
                loginUser, spuPropertyIdMap, marketingProductSpuId, now);
        spuPropertyValueMapper.insertBatch(spuPropertyValues);

        List<MarketingProductPropertyVO> skuProperties = filterSkuProperties(reqVO.getSpuProperties());
        List<MarketingProductSkuVO> normalizedSkus = buildSkuCombinationsBySkuProperties(skuProperties, reqVO.getSkus());

        // 保存商品SKU
        List<MarketingProductSku> skus = buildMarketingProductSkus(normalizedSkus, loginUser, marketingProductSpuId, now);
        skuMapper.insertBatch(skus);

        // 保存商品SKU属性值
        Map<String, Long> skuCode2IdMap = skus.stream().collect(Collectors.toMap(MarketingProductSku::getSkuCode, MarketingProductSku::getId));
        List<MarketingProductSkuPropertyValue> skuPropertyValues = buildMarketingProductSkuPropertyValues(normalizedSkus,
                skuCode2IdMap, spuPropertyValues, loginUser, marketingProductSpuId, now);
        skuPropertyValueMapper.insertBatch(skuPropertyValues);

        saveSkuRentalMethodProperties(marketingProductSpuId, normalizedSkus, skus, loginUser, now);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mod(MarketingProductModReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("mod marketingProduct reqVO: {},operatorId:{}", reqVO, loginUser.getId());
        MarketingProductSpu marketingProductSpu = marketingProductSpuMapper.selectById(reqVO.getId());
        if (marketingProductSpu == null) {
            throw exception(MARKETING_PRODUCT_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(marketingProductSpu.getIsDeleted())) {
            throw exception(MARKETING_PRODUCT_DELETED);
        }
        validateSpuPropertiesInStandardScope(reqVO.getStandardProductSpuId(), reqVO.getSpuProperties());

        Date now = new Date();

        // 租赁方式配置独立维护，编辑时先逻辑删除再按最新提交重建
        rentalMethodPropertyMapper.logicDeleteByMarketingSpuId(reqVO.getId(), loginUser.getId(), now);
        skuRentalMethodPropertyMapper.logicDeleteByMarketingSpuId(reqVO.getId(), loginUser.getId(), now);
        saveRentalMethods(reqVO.getId(), reqVO.getType(), reqVO.getRentalMethods(), loginUser, now);

        // 编辑保存商品SPU
        modSaveMarketingProductSpu(marketingProductSpu, reqVO, loginUser.getId(), now);

        // 处理spu属性
        List<MarketingProductSpuProperty> existSpuPropertyList = spuPropertyMapper.selectListByMarketingSpuId(reqVO.getId());
        Map<Long, MarketingProductPropertyVO> reqPropertyMap = reqVO.getSpuProperties().stream()
                .collect(Collectors.toMap(MarketingProductPropertyVO::getPropertyId, Function.identity()));
        List<MarketingProductSpuProperty> needUpdateSpuProperties = Lists.newArrayList();
        for (MarketingProductSpuProperty existSpuProperty : existSpuPropertyList) {
            MarketingProductPropertyVO reqProperty = reqPropertyMap.get(existSpuProperty.getProductPropertyId());
            if (reqProperty == null) {
                continue;
            }
            existSpuProperty.setSort(reqProperty.getSort());
            existSpuProperty.setIsAddPropertyPic(defaultSwitch(reqProperty.getIsAddPropertyPic()));
            existSpuProperty.setIsAddMarketingCorner(defaultSwitch(reqProperty.getIsAddMarketingCorner()));
            existSpuProperty.setIsSkuProperty(defaultSwitch(reqProperty.getIsSkuProperty()));
            existSpuProperty.setUpdateBy(loginUser.getId());
            existSpuProperty.setUpdateTime(now);
            needUpdateSpuProperties.add(existSpuProperty);
        }
        if (CollectionUtil.isNotEmpty(needUpdateSpuProperties)) {
            spuPropertyMapper.updateBatch(needUpdateSpuProperties);
        }
        Map<Long, Long> productPropertyId2SpuPropertyIdMap = existSpuPropertyList.stream().collect(Collectors.toMap(MarketingProductSpuProperty::getProductPropertyId,
                MarketingProductSpuProperty::getId));
        List<Long> existProductPropertyIds = existSpuPropertyList.stream().map(MarketingProductSpuProperty::getProductPropertyId).toList();
        List<Long> newProductPropertyIds = reqVO.getSpuProperties().stream().map(MarketingProductPropertyVO::getPropertyId).toList();

        // 删除不存在的SPU属性及其属性值
        List<Long> deletePropertyIds = existProductPropertyIds.stream()
                .filter(id -> !newProductPropertyIds.contains(id))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(deletePropertyIds)) {
            spuPropertyMapper.logicDeleteByMarketingSpuIdAndPropertyIds(reqVO.getId(), deletePropertyIds, loginUser.getId(), now);
            List<Long> deleteSpuPropertyIds = Lists.newArrayList();
            for (Long deletePropertyId : deletePropertyIds) {
                deleteSpuPropertyIds.add(productPropertyId2SpuPropertyIdMap.get(deletePropertyId));
            }
            spuPropertyValueMapper.logicDeleteByMarketingSpuIdAndSpuPropertyIds(reqVO.getId(), deleteSpuPropertyIds, loginUser.getId(), now);
        }

        // 需要新增的SPU属性及其属性值
        List<Long> addProductPropertyIds = newProductPropertyIds.stream().filter(id -> !existProductPropertyIds.contains(id)).toList();
        if (CollectionUtil.isNotEmpty(addProductPropertyIds)) {
            List<MarketingProductPropertyVO> addProductProperties = reqVO.getSpuProperties().stream()
                    .filter(property -> addProductPropertyIds.contains(property.getPropertyId()))
                    .toList();

            List<MarketingProductSpuProperty> addSpuProperties = buildMarketingProductSpuProperty(addProductProperties,
                    loginUser, reqVO.getId(), now);
            spuPropertyMapper.insertBatch(addSpuProperties);
            Map<Long, Long> addProductPropertyId2SpuPropertyIdMap = addSpuProperties.stream().collect(Collectors.toMap(MarketingProductSpuProperty::getProductPropertyId,
                    MarketingProductSpuProperty::getId));
            Map<Long, List<MarketingProductPropertyValueVO>> addProductPropertyId2PropertyValuesMap = addProductProperties.stream()
                    .collect(Collectors.toMap(MarketingProductPropertyVO::getPropertyId,
                            MarketingProductPropertyVO::getPropertyValues));
            List<MarketingProductSpuPropertyValue> addSpuPropertyValues = Lists.newArrayList();
            for (Long productPropertyId : addProductPropertyIds) {
                for (MarketingProductPropertyValueVO propertyValue : addProductPropertyId2PropertyValuesMap.get(productPropertyId)) {
                    MarketingProductSpuPropertyValue spuPropertyValue = new MarketingProductSpuPropertyValue();
                    addSpuPropertyValues.add(spuPropertyValue);

                    spuPropertyValue.setMarketingSpuId(reqVO.getId());
                    spuPropertyValue.setSpuPropertyId(addProductPropertyId2SpuPropertyIdMap.get(productPropertyId));
                    spuPropertyValue.setProductPropertyValueId(propertyValue.getProductPropertyValueId());
                    spuPropertyValue.setPropertyValue(propertyValue.getValue());
                    spuPropertyValue.setPicUrl(normalizePicUrl(propertyValue.getPicUrl(), addProductProperties.stream()
                            .filter(property -> Objects.equals(property.getPropertyId(), productPropertyId))
                            .findFirst()
                            .map(MarketingProductPropertyVO::getIsAddPropertyPic)
                            .orElse(NumberUtils.INTEGER_ZERO)));
                    spuPropertyValue.setMarketingCornerText(normalizeMarketingCornerText(propertyValue.getMarketingCornerText(), addProductProperties.stream()
                            .filter(property -> Objects.equals(property.getPropertyId(), productPropertyId))
                            .findFirst()
                            .map(MarketingProductPropertyVO::getIsAddMarketingCorner)
                            .orElse(NumberUtils.INTEGER_ZERO)));
                    spuPropertyValue.setSort(propertyValue.getSort());
                    spuPropertyValue.setPartnerId(marketingProductSpu.getPartnerId());
                    spuPropertyValue.setCreateBy(loginUser.getId());
                    spuPropertyValue.setUpdateBy(loginUser.getId());
                    spuPropertyValue.setCreateTime(now);
                    spuPropertyValue.setUpdateTime(now);
                    spuPropertyValue.setIsDeleted(NumberUtils.INTEGER_ZERO);
                }
            }
            spuPropertyValueMapper.insertBatch(addSpuPropertyValues);
        }

        // 需要更新的商品SPU属性值
        List<Long> updateProductPropertyIds = newProductPropertyIds.stream().filter(existProductPropertyIds::contains).toList();
        List<Long> updateSpuPropertyIds = updateProductPropertyIds.stream().map(productPropertyId2SpuPropertyIdMap::get).toList();
        List<MarketingProductSpuPropertyValue> needUpdateSpuPropertyValues = Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(updateSpuPropertyIds)) {
            List<MarketingProductSpuPropertyValue> updateSpuPropertyValues = spuPropertyValueMapper.selectListByMarketingSpuIdAndSpuPropertyIds(reqVO.getId(), updateSpuPropertyIds);
            updateSpuPropertyValues.forEach(spuPropertyValue -> {
                String unqKey = spuPropertyValue.getProductPropertyId() + "_" + spuPropertyValue.getProductPropertyValueId() + "_" + spuPropertyValue.getPropertyValue();
                spuPropertyValue.setUnqKey(unqKey);
            });

            // spuPropertyId -> 属性值
            Map<Long, List<MarketingProductSpuPropertyValue>> spuPropertyId2PropertyValuesMap = updateSpuPropertyValues.stream().collect(Collectors.groupingBy(MarketingProductSpuPropertyValue::getSpuPropertyId));

            // spu属性值id对应的属性id
            Map<Long, Long> spuPropertyId2ProductPropertyIdMap = existSpuPropertyList.stream().collect(Collectors.toMap(MarketingProductSpuProperty::getId,
                    MarketingProductSpuProperty::getProductPropertyId));
            List<Long> deleteSpuPropertyValueIds = Lists.newArrayList();
            List<MarketingProductSpuPropertyValue> needAddSpuPropertyValues = Lists.newArrayList();
            // 属性对应的属性值
            Map<Long, List<MarketingProductPropertyValueVO>> productPropertyId2PropertyValuesMap = reqVO.getSpuProperties().stream().collect(
                    Collectors.toMap(MarketingProductPropertyVO::getPropertyId,
                    MarketingProductPropertyVO::getPropertyValues));
            for (Long updateSpuPropertyId : updateSpuPropertyIds) {
                // 属性对应的已存在的属性值记录
                List<MarketingProductSpuPropertyValue> existSpuPropertyValues = spuPropertyId2PropertyValuesMap.get(updateSpuPropertyId);
                Map<String, MarketingProductSpuPropertyValue> unqKey2ExistSpuPropertyValueMap = existSpuPropertyValues.stream().collect(Collectors.toMap(MarketingProductSpuPropertyValue::getUnqKey,
                        Function.identity()));

                // 前端传过来的属性对应的属性值
                Long productPropertyId = spuPropertyId2ProductPropertyIdMap.get(updateSpuPropertyId);
                List<MarketingProductPropertyValueVO> newPropertyValues = productPropertyId2PropertyValuesMap.get(productPropertyId);
                newPropertyValues.forEach(newPropertyValue -> {
                    String unqKey = productPropertyId + "_" + newPropertyValue.getProductPropertyValueId() + "_" + newPropertyValue.getValue();
                    newPropertyValue.setUnqKey(unqKey);
                });
                Map<String, MarketingProductPropertyValueVO> unqKey2NewPropertyValueMap = newPropertyValues.stream().collect(Collectors.toMap(MarketingProductPropertyValueVO::getUnqKey,
                        Function.identity()));

                // 删除不存在的属性值
                for (MarketingProductSpuPropertyValue existSpuPropertyValue : existSpuPropertyValues) {
                    if (!unqKey2NewPropertyValueMap.containsKey(existSpuPropertyValue.getUnqKey())) {
                        deleteSpuPropertyValueIds.add(existSpuPropertyValue.getId());
                    } else {
                        String normalizedPicUrl = normalizePicUrl(unqKey2NewPropertyValueMap.get(existSpuPropertyValue.getUnqKey()).getPicUrl(),
                                reqPropertyMap.get(productPropertyId).getIsAddPropertyPic());
                        if (!Objects.equals(existSpuPropertyValue.getPicUrl(), normalizedPicUrl)) {
                            existSpuPropertyValue.setPicUrl(normalizedPicUrl);
                            existSpuPropertyValue.setUpdateTime(now);
                            existSpuPropertyValue.setUpdateBy(loginUser.getId());
                            needUpdateSpuPropertyValues.add(existSpuPropertyValue);
                        }
                        String normalizedMarketingCornerText = normalizeMarketingCornerText(
                                unqKey2NewPropertyValueMap.get(existSpuPropertyValue.getUnqKey()).getMarketingCornerText(),
                                reqPropertyMap.get(productPropertyId).getIsAddMarketingCorner());
                        if (!Objects.equals(existSpuPropertyValue.getMarketingCornerText(), normalizedMarketingCornerText)) {
                            existSpuPropertyValue.setMarketingCornerText(normalizedMarketingCornerText);
                            existSpuPropertyValue.setUpdateTime(now);
                            existSpuPropertyValue.setUpdateBy(loginUser.getId());
                            needUpdateSpuPropertyValues.add(existSpuPropertyValue);
                        }
                    }
                }

                // 新增不存在的属性值
                for (MarketingProductPropertyValueVO newPropertyValue : newPropertyValues) {
                    if (!unqKey2ExistSpuPropertyValueMap.containsKey(newPropertyValue.getUnqKey())) {
                        MarketingProductSpuPropertyValue spuPropertyValue = new MarketingProductSpuPropertyValue();
                        spuPropertyValue.setMarketingSpuId(reqVO.getId());
                        spuPropertyValue.setSpuPropertyId(updateSpuPropertyId);
                        spuPropertyValue.setProductPropertyValueId(newPropertyValue.getProductPropertyValueId());
                        spuPropertyValue.setPropertyValue(newPropertyValue.getValue());
                        spuPropertyValue.setPicUrl(normalizePicUrl(newPropertyValue.getPicUrl(), reqPropertyMap.get(productPropertyId).getIsAddPropertyPic()));
                        spuPropertyValue.setMarketingCornerText(normalizeMarketingCornerText(newPropertyValue.getMarketingCornerText(),
                                reqPropertyMap.get(productPropertyId).getIsAddMarketingCorner()));
                        spuPropertyValue.setSort(newPropertyValue.getSort());
                        spuPropertyValue.setPartnerId(marketingProductSpu.getPartnerId());
                        spuPropertyValue.setCreateBy(loginUser.getId());
                        spuPropertyValue.setUpdateBy(loginUser.getId());
                        spuPropertyValue.setCreateTime(now);
                        spuPropertyValue.setUpdateTime(now);
                        spuPropertyValue.setIsDeleted(NumberUtils.INTEGER_ZERO);
                        needAddSpuPropertyValues.add(spuPropertyValue);
                    }
                }
            }

            if (CollectionUtil.isNotEmpty(deleteSpuPropertyValueIds)) {
                spuPropertyValueMapper.logicDelByIds(deleteSpuPropertyValueIds, loginUser.getId(), now);
            }

            if (CollectionUtil.isNotEmpty(needAddSpuPropertyValues)) {
                spuPropertyValueMapper.insertBatch(needAddSpuPropertyValues);
            }

            if (CollectionUtil.isNotEmpty(needUpdateSpuPropertyValues)) {
                spuPropertyValueMapper.updateBatch(needUpdateSpuPropertyValues);
            }
        }

        // 处理SKU
        List<MarketingProductPropertyVO> skuProperties = filterSkuProperties(reqVO.getSpuProperties());
        List<MarketingProductSkuVO> incomingSkus = buildSkuCombinationsBySkuProperties(skuProperties, reqVO.getSkus());
        List<MarketingProductSku> existingSkus = skuMapper.selectListByMarketingSpuId(reqVO.getId());
        Map<Long, MarketingProductSku> existingSkuMap = existingSkus.stream()
                .collect(Collectors.toMap(MarketingProductSku::getId, sku -> sku));

        List<Long> updateSkuIds = Lists.newArrayList();
        List<MarketingProductSku> addSkus = Lists.newArrayList();
        List<MarketingProductSkuVO> needAddSkuVOs = Lists.newArrayList();
        List<MarketingProductSku> updateSkus = Lists.newArrayList();
        List<MarketingProductSkuVO> needUpdateSkuVOs = Lists.newArrayList();
        List<Long> deleteSkuIds = Lists.newArrayList();
        for (MarketingProductSkuVO skuVO : incomingSkus) {
            if (skuVO.getId() == null) {
                needAddSkuVOs.add(skuVO);
                MarketingProductSku marketingProductSku = new MarketingProductSku();
                addSkus.add(marketingProductSku);
                String skuCode = "SKU" + DateUtil.format(new Date(), "yyMMdd") + sequenceGenerator.getSkuSequence();
                skuVO.setSkuCode(skuCode);
                marketingProductSku.setSkuCode(skuCode);
                marketingProductSku.setMarketingSpuId(reqVO.getId());
                marketingProductSku.setOfficialPrice(skuVO.getOfficialPrice());
                marketingProductSku.setTotalPriceFactor(skuVO.getTotalPriceFactor());
                marketingProductSku.setTotalRentFactor(skuVO.getTotalRentFactor());
                marketingProductSku.setTotalPrice(skuVO.getTotalPrice());
                marketingProductSku.setTotalRent(skuVO.getTotalRent());
                marketingProductSku.setBuyoutAmount(skuVO.getBuyoutAmount());
                marketingProductSku.setDailyRent(skuVO.getDailyRent());
                marketingProductSku.setStock(skuVO.getStock());
                marketingProductSku.setPremium(skuVO.getPremium());
                marketingProductSku.setSuggestedRetailPrice(skuVO.getSuggestedRetailPrice());
                marketingProductSku.setStrikethroughPrice(skuVO.getStrikethroughPrice());
                marketingProductSku.setCashUsageRatio(skuVO.getCashUsageRatio());
                marketingProductSku.setPointsUsageRatio(skuVO.getPointsUsageRatio());
                marketingProductSku.setPointsCount(skuVO.getPointsCount());
                marketingProductSku.setCashPrice(skuVO.getCashPrice());
                marketingProductSku.setIsAllowOrder(NumberUtils.INTEGER_ONE);
                marketingProductSku.setPartnerId(loginUser.getPartnerId());
                marketingProductSku.setCreateBy(loginUser.getId());
                marketingProductSku.setUpdateBy(loginUser.getId());
                marketingProductSku.setCreateTime(now);
                marketingProductSku.setUpdateTime(now);
                marketingProductSku.setIsDeleted(NumberUtils.INTEGER_ZERO);
            } else {
                needUpdateSkuVOs.add(skuVO);
                updateSkuIds.add(skuVO.getId());
                MarketingProductSku marketingProductSku = existingSkuMap.get(skuVO.getId());
                updateSkus.add(marketingProductSku);
                marketingProductSku.setOfficialPrice(skuVO.getOfficialPrice());
                marketingProductSku.setTotalPriceFactor(skuVO.getTotalPriceFactor());
                marketingProductSku.setTotalRentFactor(skuVO.getTotalRentFactor());
                marketingProductSku.setTotalPrice(skuVO.getTotalPrice());
                marketingProductSku.setTotalRent(skuVO.getTotalRent());
                marketingProductSku.setBuyoutAmount(skuVO.getBuyoutAmount());
                marketingProductSku.setDailyRent(skuVO.getDailyRent());
                marketingProductSku.setStock(skuVO.getStock());
                marketingProductSku.setPremium(skuVO.getPremium());
                marketingProductSku.setSuggestedRetailPrice(skuVO.getSuggestedRetailPrice());
                marketingProductSku.setStrikethroughPrice(skuVO.getStrikethroughPrice());
                marketingProductSku.setCashUsageRatio(skuVO.getCashUsageRatio());
                marketingProductSku.setPointsUsageRatio(skuVO.getPointsUsageRatio());
                marketingProductSku.setPointsCount(skuVO.getPointsCount());
                marketingProductSku.setCashPrice(skuVO.getCashPrice());
                marketingProductSku.setIsAllowOrder(NumberUtils.INTEGER_ONE);
                marketingProductSku.setUpdateBy(loginUser.getId());
                marketingProductSku.setUpdateTime(now);
                marketingProductSku.setIsDeleted(NumberUtils.INTEGER_ZERO);
            }
        }

        // 获取需要删除的sku ids
        for (MarketingProductSku existingSku : existingSkus) {
            if (!updateSkuIds.contains(existingSku.getId())) {
                deleteSkuIds.add(existingSku.getId());
            }
        }

        // 获取所有的属性值
        List<MarketingProductSpuPropertyValue> existingSpuPropertyValues = spuPropertyValueMapper.selectListByMarketingProductSpuIds(Lists.newArrayList(reqVO.getId()));
        existingSpuPropertyValues.forEach(spuPropertyValue -> {
            // 唯一键由属性id、属性值id、属性值组成
            String uniqueKey = spuPropertyValue.getProductPropertyId() + "_" + spuPropertyValue.getProductPropertyValueId() + "_" +spuPropertyValue.getPropertyValue();
            spuPropertyValue.setUnqKey(uniqueKey);
        });
        Map<String, MarketingProductSpuPropertyValue> unqKey2SpuPropertyValueMap = existingSpuPropertyValues.stream().collect(
                Collectors.toMap(MarketingProductSpuPropertyValue::getUnqKey, Function.identity()));

        // 添加sku
        if (CollectionUtil.isNotEmpty(addSkus)) {
            skuMapper.insertBatch(addSkus);
            Map<String, Long> skuCode2IdMap = addSkus.stream().collect(Collectors.toMap(MarketingProductSku::getSkuCode, MarketingProductSku::getId));
            List<MarketingProductSkuPropertyValue> skuPropertyValues = buildMarketingProductSkuPropertyValues(needAddSkuVOs,
                    skuCode2IdMap, existingSpuPropertyValues, loginUser, reqVO.getId(), now);
            skuPropertyValueMapper.insertBatch(skuPropertyValues);
        }

        // 更新sku
        if (CollectionUtil.isNotEmpty(updateSkus)) {
            skuMapper.updateBatch(updateSkus);
            // 获取待更新的sku关联的属性值
            List<MarketingProductSkuPropertyValue> skuPropertyValues = skuPropertyValueMapper.selectListBySkuIds(updateSkuIds);
            // skuId -> spuPropertyValueIds
            Map<Long, List<Long>> skuId2SpuPropertyValueIdsMap = skuPropertyValues.stream().collect(Collectors.groupingBy(
                    MarketingProductSkuPropertyValue::getMarketingProductSkuId,
                    Collectors.mapping(MarketingProductSkuPropertyValue::getMarketingSpuPropertyValueId, Collectors.toList())));

            // 处理需要更新的sku的属性
            List<MarketingProductSkuPropertyValue> addSkuPropertyValues = Lists.newArrayList();
            for (MarketingProductSkuVO updateSkuVO : needUpdateSkuVOs) {
                // 编辑后sku对应的所有spuPropertyValueIds;
                List<Long> incomingSpuPropertyValueIds = Lists.newArrayList();
                List<Long> delSpuPropertyValueIds = Lists.newArrayList();
                for (SkuPropertyValueVO skuPropertyValueVO : updateSkuVO.getPropertyValues()) {
                    // 获取sku关联的属性值
                    List<Long> existingSpuPropertyValueIds = skuId2SpuPropertyValueIdsMap.get(updateSkuVO.getId());

                    String uniqueKey = skuPropertyValueVO.getPropertyId() + "_" + skuPropertyValueVO.getPropertyValueId() + "_" +skuPropertyValueVO.getPropertyValue();
                    Long spuPropertyValueId = unqKey2SpuPropertyValueMap.get(uniqueKey).getId();
                    incomingSpuPropertyValueIds.add(spuPropertyValueId);
                    if (!existingSpuPropertyValueIds.contains(spuPropertyValueId)) {
                        MarketingProductSkuPropertyValue skuPropertyValue = new MarketingProductSkuPropertyValue();
                        addSkuPropertyValues.add(skuPropertyValue);
                        skuPropertyValue.setMarketingProductSkuId(updateSkuVO.getId());
                        skuPropertyValue.setMarketingSpuPropertyValueId(spuPropertyValueId);
                        skuPropertyValue.setMarketingSpuId(reqVO.getId());
                        skuPropertyValue.setPartnerId(loginUser.getPartnerId());
                        skuPropertyValue.setCreateBy(loginUser.getId());
                        skuPropertyValue.setCreateTime(now);
                        skuPropertyValue.setUpdateBy(loginUser.getId());
                        skuPropertyValue.setUpdateTime(now);
                        skuPropertyValue.setIsDeleted(NumberUtils.INTEGER_ZERO);
                    }
                    delSpuPropertyValueIds = existingSpuPropertyValueIds.stream().filter(existingSpuPropertyValueId -> !incomingSpuPropertyValueIds.contains(existingSpuPropertyValueId)).toList();
                }

                // 删除指定sku的属性值
                if (CollectionUtil.isNotEmpty(delSpuPropertyValueIds)) {
                    skuPropertyValueMapper.logicDelBySkuIdAndSpuPropertyValueIds(updateSkuVO.getId(), delSpuPropertyValueIds, loginUser.getId(), now);
                }
            }

            // 新增属性值
            if (CollectionUtil.isNotEmpty(addSkuPropertyValues)) {
                skuPropertyValueMapper.insertBatch(addSkuPropertyValues);
            }
        }

        // 删除sku
        if (CollectionUtil.isNotEmpty(deleteSkuIds)) {
            skuMapper.logicDelByIds(deleteSkuIds, loginUser.getId(), now);
            skuPropertyValueMapper.logicDelBySkuIds(deleteSkuIds, loginUser.getId(), now);
        }

        List<MarketingProductSku> savedSkus = Lists.newArrayList();
        savedSkus.addAll(addSkus);
        savedSkus.addAll(updateSkus);
        saveSkuRentalMethodProperties(reqVO.getId(), incomingSkus, savedSkus, loginUser, now);
    }

    @Override
    public MarketingProductDetailRespVO detail(Long id) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("detail id: {},operatorId:{}", id, loginUser.getId());
        // 查询营销商品
        MarketingProductSpu marketingProductSpu = marketingProductSpuMapper.selectById(id);
        if (marketingProductSpu == null) {
            throw exception(MARKETING_PRODUCT_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(marketingProductSpu.getIsDeleted())) {
            throw exception(MARKETING_PRODUCT_DELETED);
        }
        // 查询标准商品
        StandardProductSpu standardProductSpu = standardProductSpuMapper.selectById(marketingProductSpu.getStandardProductSpuId());

        MarketingProductDetailRespVO detailRespVO = new MarketingProductDetailRespVO();
        detailRespVO.setId(marketingProductSpu.getId());
        detailRespVO.setCode(marketingProductSpu.getCode());
        detailRespVO.setType(marketingProductSpu.getType());
        detailRespVO.setName(marketingProductSpu.getName());
        detailRespVO.setProductCondition(marketingProductSpu.getProductCondition());
        detailRespVO.setMonitorAttribute(marketingProductSpu.getMonitorAttribute());

        detailRespVO.setStandardProductSpuId(standardProductSpu.getId());
        detailRespVO.setStandardProductSpuCode(standardProductSpu.getCode());
        detailRespVO.setStandardProductSpuName(standardProductSpu.getName());
        List<String> selfAndParentBusinessCategoryNames = businessCategoryMapper.selectSelfAndParentNamesById(standardProductSpu.getBusinessCategoryId());
        detailRespVO.setBusinessCategoryName(String.join(">", selfAndParentBusinessCategoryNames));
        List<String> selfAndParentMarketingCategoryNames = marketingCategoryMapper.selectSelfAndParentNamesById(standardProductSpu.getMarketingCategoryId());
        detailRespVO.setMarketingCategoryName(String.join(">", selfAndParentMarketingCategoryNames));
        String brandName = brandMapper.selectNameById(standardProductSpu.getProductBrandId());
        detailRespVO.setBrandName(brandName);

        detailRespVO.setMainPicUrls(List.of(StringUtils.split(marketingProductSpu.getMainPicUrls(), ",")));
        detailRespVO.setCarouselPicUrls(List.of(StringUtils.split(marketingProductSpu.getCarouselPicUrls(), ",")));
        detailRespVO.setVideoUrls(List.of(StringUtils.split(marketingProductSpu.getVideoUrls(), ",")));
        detailRespVO.setDetailPicUrls(List.of(StringUtils.split(marketingProductSpu.getDetailPicUrls(), ",")));

        // 标签
        Set<Long> tagIds = Sets.newHashSet();
        Set<Long> detailTagIds = null;
        if (StringUtils.isNotBlank(marketingProductSpu.getDetailTagIds())) {
            detailTagIds = parseIds(marketingProductSpu.getDetailTagIds());
            tagIds.addAll(detailTagIds);
        }
        Set<Long> skuTagIds = null;
        if (StringUtils.isNotBlank(marketingProductSpu.getSkuTagIds())) {
            skuTagIds = parseIds(marketingProductSpu.getSkuTagIds());
            tagIds.addAll(skuTagIds);
        }
        if (CollUtil.isNotEmpty(tagIds)) {
            List<IdAndNameVO> tags = tagMapper.selectTagsByIds(tagIds);
            Map<Long, String> tagIdNameMap = tags.stream().collect(Collectors.toMap(IdAndNameVO::getId, IdAndNameVO::getName));
            if (CollUtil.isNotEmpty(detailTagIds)) {
                List<IdAndNameVO> detailTags = buildIdAndNameList(detailTagIds, tagIdNameMap);
                detailRespVO.setDetailTags(detailTags);
            }
            if (CollUtil.isNotEmpty(skuTagIds)) {
                List<IdAndNameVO> skuTags = buildIdAndNameList(skuTagIds, tagIdNameMap);
                detailRespVO.setSkuTags(skuTags);
            }
        }

        detailRespVO.setMinBuybackPrice(marketingProductSpu.getMinBuybackPrice());
        detailRespVO.setMaxBuybackPrice(marketingProductSpu.getMaxBuybackPrice());

        // 增值服务
        Set<Long> valueAddedIds = Sets.newHashSet();
        if (StringUtils.isNotBlank(marketingProductSpu.getValueAddedIds())) {
            valueAddedIds.addAll(parseIds(marketingProductSpu.getValueAddedIds()));
        }
        if (Objects.nonNull(marketingProductSpu.getDefaultSelectedValueAddedId())) {
            valueAddedIds.add(marketingProductSpu.getDefaultSelectedValueAddedId());
        }
        if (CollUtil.isNotEmpty(valueAddedIds)) {
            List<IdAndNameVO> valueAddedList = valueAddedMapper.selectIdAndNamesByIds(valueAddedIds);
            Map<Long, String> valueAddedIdNameMap = valueAddedList.stream().collect(Collectors.toMap(IdAndNameVO::getId, IdAndNameVO::getName));
            Set<Long> selectedValueAddedIds = parseIds(marketingProductSpu.getValueAddedIds());
            List<IdAndNameVO> selectedValueAddedList = buildIdAndNameList(selectedValueAddedIds, valueAddedIdNameMap);
            detailRespVO.setValueAddedList(selectedValueAddedList);

            detailRespVO.setDefaultSelectedValueAddedId(marketingProductSpu.getDefaultSelectedValueAddedId());
            detailRespVO.setDefaultSelectedValueAddedName(valueAddedIdNameMap.get(marketingProductSpu.getDefaultSelectedValueAddedId()));
        }
        if (StringUtils.isNotBlank(marketingProductSpu.getShowPage())) {
            detailRespVO.setShowPages(Arrays.stream(StringUtils.split(marketingProductSpu.getShowPage(), ","))
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .collect(Collectors.toList()));
        }
        detailRespVO.setIsDefaultSelected(marketingProductSpu.getIsDefaultSelected());

        // 订单服务
        detailRespVO.setCompensationRuleId(marketingProductSpu.getCompensationRuleId());
        detailRespVO.setShippingWay(marketingProductSpu.getShippingWay());
        if (Objects.nonNull(marketingProductSpu.getShippingTemplateId())) {
            detailRespVO.setShippingTemplateId(marketingProductSpu.getShippingTemplateId());
            String expressTemplateName = expressTemplateMapper.selectNameById(marketingProductSpu.getShippingTemplateId());
            detailRespVO.setShippingTemplateName(expressTemplateName);
        }
        if (StringUtils.isNotBlank(marketingProductSpu.getShippingAreaCodes())) {
            Set<String> shippingAreaCodes = Set.of(StringUtils.split(marketingProductSpu.getShippingAreaCodes(), ","));
            CommonResult<List<AreaCodeAndNameDTO>> areaResult = areaApi.getByAreaCodes(shippingAreaCodes);
            if (areaResult.isSuccess() && CollectionUtil.isNotEmpty(areaResult.getData())) {
                Map<String, String> areaCodeeAndNameMap = areaResult.getData().stream().collect(Collectors.toMap(
                        AreaCodeAndNameDTO::getCode, AreaCodeAndNameDTO::getName));
                List<AreaCodeAndNameVO> areaCodeAndNameVOS = Lists.newArrayList();
                for (String shippingAreaCode : shippingAreaCodes) {
                    AreaCodeAndNameVO areaCodeAndNameVO = new AreaCodeAndNameVO();
                    areaCodeAndNameVO.setAreaCode(shippingAreaCode);
                    areaCodeAndNameVO.setAreaName(areaCodeeAndNameMap.get(shippingAreaCode));
                    areaCodeAndNameVOS.add(areaCodeAndNameVO);
                }
                detailRespVO.setShippingAreaCodes(areaCodeAndNameVOS);
            }
        }
        detailRespVO.setReceivingAddress(marketingProductSpu.getReceivingAddress());

        // 商品上架信息
        detailRespVO.setShelvingWay(marketingProductSpu.getShelvingWay());
        detailRespVO.setShelvingTime(marketingProductSpu.getShelvingTime());
        if (StringUtils.isNotBlank(marketingProductSpu.getShelvingChannelId())) {
            Set<Long> shelvingChannelIds = parseIds(marketingProductSpu.getShelvingChannelId());
            Map<Long, MarketingChannelRespDTO> channelId2ChannelMap = buildChannelId2ChannelMap(Lists.newArrayList(shelvingChannelIds));
            List<IdAndNameVO> shelvingChannelList = Lists.newArrayList();
            for (Long shelvingChannelId : shelvingChannelIds) {
                IdAndNameVO idAndNameVO = new IdAndNameVO();
                idAndNameVO.setId(shelvingChannelId);
                idAndNameVO.setName(channelId2ChannelMap.get(shelvingChannelId).getChannelName());
                shelvingChannelList.add(idAndNameVO);
            }
            detailRespVO.setShelvingChannels(shelvingChannelList);
        }

        detailRespVO.setApproveStatus(marketingProductSpu.getApprovalStatus());
        detailRespVO.setShelvesStatus(marketingProductSpu.getShelvesStatus());
        detailRespVO.setIsDraft(marketingProductSpu.getIsDraft());
        detailRespVO.setRentalMethods(buildRentalMethodVOs(
                rentalMethodPropertyMapper.selectListByMarketingSpuId(marketingProductSpu.getId())));

        // SPU属性、属性值信息
        List<MarketingProductSpuProperty> spuProperties = spuPropertyMapper.selectListByMarketingSpuId(marketingProductSpu.getId());
        List<Long> productPropertyIds = spuProperties.stream().map(MarketingProductSpuProperty::getProductPropertyId).toList();
        List<IdAndNameVO> productProperties = productPropertyMapper.selectIdAndNamesByIds(productPropertyIds);
        Map<Long, String> propertyIdNameMap = productProperties.stream().collect(Collectors.toMap(IdAndNameVO::getId, IdAndNameVO::getName));

        List<MarketingProductSpuPropertyValue> spuPropertyValues = spuPropertyValueMapper.selectListByMarketingSpuId(marketingProductSpu.getId());
        Map<Long, List<MarketingProductSpuPropertyValue>> spuPropertyId2PropertyValues = spuPropertyValues.stream().collect(Collectors.groupingBy(MarketingProductSpuPropertyValue::getSpuPropertyId));
        Map<Long, MarketingProductSpuPropertyValue> id2SpuPropertyValueMap = spuPropertyValues.stream().collect(Collectors.toMap(MarketingProductSpuPropertyValue::getId, Function.identity()));

        List<Long> productPropertyValueIds = spuPropertyValues.stream().map(MarketingProductSpuPropertyValue::getProductPropertyValueId).toList();
        List<ProductPropertyValue> productPropertyValues = productPropertyValueMapper.selectListByIds(productPropertyValueIds);
        Map<Long, String> productPropertyId2ValueMap = productPropertyValues.stream().collect(Collectors.toMap(ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue));

        List<MarketingProductPropertyVO> spuPropertyVOS = Lists.newArrayList();
        for (MarketingProductSpuProperty spuProperty : spuProperties) {
            MarketingProductPropertyVO spuPropertyVO = new MarketingProductPropertyVO();
            spuPropertyVOS.add(spuPropertyVO);
            spuPropertyVO.setPropertyId(spuProperty.getProductPropertyId());
            spuPropertyVO.setPropertyName(propertyIdNameMap.get(spuProperty.getProductPropertyId()));
            spuPropertyVO.setSort(spuProperty.getSort());
            spuPropertyVO.setIsAddPropertyPic(spuProperty.getIsAddPropertyPic());
            spuPropertyVO.setIsAddMarketingCorner(spuProperty.getIsAddMarketingCorner());
            spuPropertyVO.setIsSkuProperty(spuProperty.getIsSkuProperty());

            List<MarketingProductPropertyValueVO> spuPropertyValueVOS = Lists.newArrayList();
            spuPropertyVO.setPropertyValues(spuPropertyValueVOS);
            for (MarketingProductSpuPropertyValue spuPropertyValue : spuPropertyId2PropertyValues.get(spuProperty.getId())) {
                MarketingProductPropertyValueVO spuPropertyValueVO = new MarketingProductPropertyValueVO();
                spuPropertyValueVOS.add(spuPropertyValueVO);
                spuPropertyValueVO.setProductPropertyValueId(spuPropertyValue.getProductPropertyValueId());
                if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                    spuPropertyValueVO.setValue(productPropertyId2ValueMap.get(spuPropertyValue.getProductPropertyValueId()));
                } else {
                    spuPropertyValueVO.setValue(spuPropertyValue.getPropertyValue());
                }
                spuPropertyValueVO.setSort(spuPropertyValue.getSort());
                spuPropertyValueVO.setPicUrl(spuPropertyValue.getPicUrl());
                spuPropertyValueVO.setMarketingCornerText(spuPropertyValue.getMarketingCornerText());
            }
        }
        detailRespVO.setSpuProperties(spuPropertyVOS);


        // SKU信息
        List<MarketingProductSku> skus = skuMapper.selectListByMarketingSpuId(marketingProductSpu.getId());
        List<MarketingProductSkuPropertyValue> skuPropertyValues = skuPropertyValueMapper.selectListByMarketingSpuId(marketingProductSpu.getId());
        Map<Long, List<MarketingProductSkuPropertyValue>> skuId2SkuPropertyValues = skuPropertyValues.stream().collect(Collectors.groupingBy(MarketingProductSkuPropertyValue::getMarketingProductSkuId));
        Map<Long, List<MarketingProductSkuRentalMethodProperty>> skuId2RentalProperties =
                skuRentalMethodPropertyMapper.selectListByMarketingSpuId(marketingProductSpu.getId()).stream()
                        .collect(Collectors.groupingBy(MarketingProductSkuRentalMethodProperty::getMarketingSkuId));

        List<MarketingProductSkuVO> skuVOS = Lists.newArrayList();
        for (MarketingProductSku sku : skus) {
            MarketingProductSkuVO skuVO = new MarketingProductSkuVO();
            skuVOS.add(skuVO);

            skuVO.setId(sku.getId());
            skuVO.setOfficialPrice(sku.getOfficialPrice());
            skuVO.setTotalPriceFactor(sku.getTotalPriceFactor());
            skuVO.setTotalRentFactor(sku.getTotalRentFactor());
            skuVO.setTotalPrice(sku.getTotalPrice());
            skuVO.setTotalRent(sku.getTotalRent());
            skuVO.setBuyoutAmount(sku.getBuyoutAmount());
            skuVO.setDailyRent(sku.getDailyRent());
            skuVO.setStock(sku.getStock());
            skuVO.setPremium(sku.getPremium());
            skuVO.setSuggestedRetailPrice(sku.getSuggestedRetailPrice());
            skuVO.setStrikethroughPrice(sku.getStrikethroughPrice());
            skuVO.setCashUsageRatio(sku.getCashUsageRatio());
            skuVO.setPointsUsageRatio(sku.getPointsUsageRatio());
            skuVO.setPointsCount(sku.getPointsCount());
            skuVO.setCashPrice(sku.getCashPrice());

            List<MarketingProductSkuPropertyValue> marketingProductSkuPropertyValues = skuId2SkuPropertyValues.get(sku.getId());
            List<SkuPropertyValueVO> skuPropertyValueVOS = Lists.newArrayList();
            for (MarketingProductSkuPropertyValue marketingProductSkuPropertyValue : marketingProductSkuPropertyValues) {
                SkuPropertyValueVO skuPropertyValueVO = new SkuPropertyValueVO();
                skuPropertyValueVOS.add(skuPropertyValueVO);

                MarketingProductSpuPropertyValue spuPropertyValue = id2SpuPropertyValueMap.get(marketingProductSkuPropertyValue.getMarketingSpuPropertyValueId());
                skuPropertyValueVO.setPropertyId(spuPropertyValue.getProductPropertyId());
                skuPropertyValueVO.setPropertyName(propertyIdNameMap.get(spuPropertyValue.getProductPropertyId()));
                skuPropertyValueVO.setPropertyValueId(spuPropertyValue.getProductPropertyValueId());
                if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                    skuPropertyValueVO.setPropertyValue(productPropertyId2ValueMap.get(spuPropertyValue.getProductPropertyValueId()));
                } else {
                    skuPropertyValueVO.setPropertyValue(spuPropertyValue.getPropertyValue());
                }

            }
            skuVO.setPropertyValues(skuPropertyValueVOS);
            skuVO.setRentalMethodProperties(convertSkuRentalMethodPropertyVOs(skuId2RentalProperties.get(sku.getId())));
        }
        detailRespVO.setSkus(skuVOS);

        return detailRespVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void del(List<Long> ids) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("del ids: {},operatorId:{}", ids, loginUser.getId());
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 批量逻辑删除
        Date now = new Date();
        marketingProductSpuMapper.logicDeleteByIds(ids, loginUser.getId(), now);
        spuPropertyMapper.logicDelByMarketingSpuIds(ids, loginUser.getId(), now);
        spuPropertyValueMapper.logicDelByMarketingSpuIds(ids, loginUser.getId(), now);
        skuMapper.logicDelByMarketingSpuIds(ids, loginUser.getId(), now);
        skuPropertyValueMapper.logicDelByMarketingSpuIds(ids, loginUser.getId(), now);
        rentalMethodPropertyMapper.logicDeleteByMarketingSpuIds(ids, loginUser.getId(), now);
        skuRentalMethodPropertyMapper.logicDeleteByMarketingSpuIds(ids, loginUser.getId(), now);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onOffShelves(OnOffShelvesReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("onOffShelves reqVO: {},operatorId:{}", reqVO, loginUser.getId());
        if (CollUtil.isEmpty(reqVO.getIds())) {
            return;
        }

        // 查询商品信息
        List<MarketingProductSpu> marketingProductSpus = marketingProductSpuMapper.selectListByIds(reqVO.getIds());
        if (CollUtil.isEmpty(marketingProductSpus)) {
            log.warn("onOffShelves marketingProductSpus is empty,ids:{}", reqVO.getIds());
            return;
        }

        Date now = new Date();
        if (ShelvesStatusEnum.OFF_SHELVES.getValue().equals(reqVO.getShelvesStatus())) {
            // 批量更新商品的上下架状态为下架
            marketingProductSpuMapper.updateShelvesStatusByIds(reqVO.getIds(), ShelvesStatusEnum.OFF_SHELVES.getValue(), loginUser.getId(), now);
        } else {
            // 状态是待上架的则更新为已上架，状态是已下架的则需要更新审批状态为待审批、上下架状态为待上架
            List<Long> needOnShelvesIds = Lists.newArrayList();
            List<Long> needUpdateApproveAndShelvesStatusIds = Lists.newArrayList();
            for (MarketingProductSpu marketingProductSpu : marketingProductSpus) {
                if (ShelvesStatusEnum.WAIT_SHELVES.getValue().equals(marketingProductSpu.getShelvesStatus())) {
                    needOnShelvesIds.add(marketingProductSpu.getId());
                } else if (ShelvesStatusEnum.OFF_SHELVES.getValue().equals(marketingProductSpu.getShelvesStatus())) {
                    needUpdateApproveAndShelvesStatusIds.add(marketingProductSpu.getId());
                }
            }

            if (CollUtil.isNotEmpty(needOnShelvesIds)) {
                // 批量更新商品状态为已上架
                marketingProductSpuMapper.updateShelvesStatusByIds(needOnShelvesIds, ShelvesStatusEnum.ON_SHELVES.getValue(), loginUser.getId(), now);
            }

            if (CollUtil.isNotEmpty(needUpdateApproveAndShelvesStatusIds)) {
                // 批量更新商品状态为待审批、上下架状态为待上架
                marketingProductSpuMapper.updateApproveAndShelvesStatusByIds(needUpdateApproveAndShelvesStatusIds,
                        ApproveStatusEnum.WAIT_APPROVE.getValue(), ShelvesStatusEnum.WAIT_SHELVES.getValue(), loginUser.getId(), now);
            }
        }
    }

    @Override
    public void approve(MarketingProductApproveReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("status change product approve dto: {},operatorId:{}", reqVO, loginUser.getId());
        MarketingProductSpu marketingProductSpu = marketingProductSpuMapper.selectById(reqVO.getId());
        if (marketingProductSpu == null) {
            throw exception(MARKETING_PRODUCT_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(marketingProductSpu.getIsDeleted())) {
            throw exception(MARKETING_PRODUCT_DELETED);
        }
        if (NumberUtils.INTEGER_ONE.equals(marketingProductSpu.getIsDraft())) {
            throw exception(MARKETING_PRODUCT_IS_DRAFT);
        }
        if (Objects.equals(marketingProductSpu.getApprovalStatus(), reqVO.getApproveStatus())) {
            log.info("商品状态未改变,productId:{}", marketingProductSpu.getId());
            return;
        }
        if (!Objects.equals(marketingProductSpu.getApprovalStatus(), ApproveStatusEnum.WAIT_APPROVE.getValue())) {
            throw exception(MARKETING_PRODUCT_STATUS_NOT_WAIT_APPROVE);
        }

        Date now = new Date();
        // 审批通过
        if (ApproveStatusEnum.APPROVE_PASS.getValue().equals(reqVO.getApproveStatus())) {
            if (ShelvingWayEnum.AUTO_SHELVES.getValue().equals(marketingProductSpu.getShelvingWay())) {
                // 如果是自动上架，那么审批通过后将上架状态改为已上架
                marketingProductSpu.setShelvesStatus(ShelvesStatusEnum.ON_SHELVES.getValue());
                marketingProductSpu.setShelvingTime(now);
            } else if (ShelvingWayEnum.APPOINT_SHELVES.getValue().equals(marketingProductSpu.getShelvingWay())) {
                // 如果是预约上架，预约时间在当前时间之前，将上架状态改为已上架
                if (marketingProductSpu.getShelvingTime().before(now)) {
                    marketingProductSpu.setShelvesStatus(ShelvesStatusEnum.ON_SHELVES.getValue());
                    marketingProductSpu.setShelvingTime(now);
                }
            }
        }


        marketingProductSpu.setApproverId(loginUser.getId());
        marketingProductSpu.setApprovalStatus(reqVO.getApproveStatus());
        marketingProductSpu.setApprovalRemark(reqVO.getApprovalRemark());
        marketingProductSpu.setUpdateTime(now);
        marketingProductSpu.setUpdateBy(loginUser.getId());
        marketingProductSpuMapper.updateById(marketingProductSpu);
    }

    @Override
    public StatusStatisticRespVO statusStatistic(MarketingProductListReqVO reqVO) {
        log.info("查询商品状态统计信息,reqVO:{}", reqVO);

        StatusStatisticRespVO statusStatisticRespVO = new StatusStatisticRespVO();
        MarketingProductQuery query = reqVO.toQuery();
        // 根据品牌、营销类目搜索
        if (reqVO.getBrandId() != null || reqVO.getMarketingCategoryId() != null) {
            List<Long> standardProductSpuIds = standardProductSpuMapper.selectIdsByBrandIdAndMarketingCategoryId(
                    reqVO.getBrandId(), reqVO.getMarketingCategoryId());
            query.setStandardProductSpuIds(standardProductSpuIds);
            if (CollectionUtil.isEmpty(standardProductSpuIds)) {
                return statusStatisticRespVO;
            }
        }

        // 根据颜色、规格搜索
        if (StringUtils.isNotBlank(reqVO.getColor()) || StringUtils.isNotBlank(reqVO.getSpecification())) {
            List<Long> propertyValueIds = Lists.newArrayList();
            if (StringUtils.isNotBlank(reqVO.getColor())) {
                Long colorPropertyValueId = productPropertyValueMapper.selectPropertyValueIdByPropertyNameAndValue("颜色", reqVO.getColor());
                if (Objects.isNull(colorPropertyValueId)) {
                    log.info("根据颜色:{}未查询到记录", reqVO.getColor());
                    return statusStatisticRespVO;
                }
                propertyValueIds.add(colorPropertyValueId);
            }
            if (StringUtils.isNotBlank(reqVO.getSpecification())) {
                Long colorPropertyValueId = productPropertyValueMapper.selectPropertyValueIdByPropertyNameAndValue("规格", reqVO.getSpecification());
                if (Objects.isNull(colorPropertyValueId)) {
                    log.info("根据规格:{}未查询到记录", reqVO.getColor());
                    return statusStatisticRespVO;
                }
                propertyValueIds.add(colorPropertyValueId);
            }
            List<Long> marketingProductSpuIds = spuPropertyValueMapper.selectMarketingSpuIdsByPropertyValueIds(propertyValueIds, propertyValueIds.size());
            if (CollUtil.isEmpty(marketingProductSpuIds)) {
                log.info("根据color:{},specification:{}未查询到记录", reqVO.getColor(), reqVO.getSpecification());
                return statusStatisticRespVO;
            }
            query.setIds(marketingProductSpuIds);
        }

        // 获取商品总数
        Integer totalCount = marketingProductSpuMapper.queryCount(query);

        // 获取草稿商品数
        query.setIsDraft(NumberUtils.INTEGER_ONE);
        Integer draftCount = marketingProductSpuMapper.queryCount(query);

        // 获取审核商品数
        query.setIsDraft(NumberUtils.INTEGER_ZERO);
        query.setShelvesStatus(ShelvesStatusEnum.WAIT_SHELVES.getValue());
        List<ApproveStatusStatisticCountDTO> approveStatusStatisticCountDTOS = marketingProductSpuMapper.approveStatusStatistic(query);

        // 获取上下架商品数
        query.setShelvesStatus(null);
        List<ShelvesStatisticCountDTO> shelvesStatisticCountDTOS = marketingProductSpuMapper.shelvesStatistic(query);

        // 构造返回结果
        statusStatisticRespVO.setTotalCount(totalCount);
        statusStatisticRespVO.setDraftCount(draftCount);
        for (ApproveStatusStatisticCountDTO approveStatusStatisticCountDTO : approveStatusStatisticCountDTOS) {
            if (approveStatusStatisticCountDTO.getApproveStatus() == null) {
                continue;
            }
            ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.get(approveStatusStatisticCountDTO.getApproveStatus());
            switch (approveStatusEnum) {
                case WAIT_APPROVE:
                    statusStatisticRespVO.setWaitApproveCount(approveStatusStatisticCountDTO.getCount());
                    break;
                case APPROVE_PASS:
                    statusStatisticRespVO.setApprovePassCount(approveStatusStatisticCountDTO.getCount());
                    break;
                case APPROVE_REJECT:
                    statusStatisticRespVO.setApproveRejectCount(approveStatusStatisticCountDTO.getCount());
            }
        }
        for (ShelvesStatisticCountDTO shelvesStatisticCountDTO : shelvesStatisticCountDTOS) {
            if (shelvesStatisticCountDTO.getShelvesStatus() == null) {
                continue;
            }
            ShelvesStatusEnum shelvesStatusEnum = ShelvesStatusEnum.get(shelvesStatisticCountDTO.getShelvesStatus());
            switch (shelvesStatusEnum) {
                case WAIT_SHELVES:
                    statusStatisticRespVO.setWaitShelvesCount(shelvesStatisticCountDTO.getCount());
                    break;
                case ON_SHELVES:
                    statusStatisticRespVO.setOnShelvesCount(shelvesStatisticCountDTO.getCount());
                    break;
                case OFF_SHELVES:
                    statusStatisticRespVO.setOffShelvesCount(shelvesStatisticCountDTO.getCount());
                    break;
                default:
                    break;
            }
        }
        return statusStatisticRespVO;
    }

    @Override
    public List<ProductDetailRespVO> batchGetProductDetail(MarketingProductReqVO reqVO) {
        log.info("batchGetProductDetail, reqVO: {}", reqVO);
        if (CollectionUtil.isEmpty(reqVO.getIds())) {
            return Collections.emptyList();
        }

        List<ProductDetailRespVO> detailRespVOS = Lists.newArrayList();
        if (ProductApiConstants.SKU.equals(reqVO.getType())) {
            List<MarketingProductSku> marketingProductSkus = skuMapper.selectListByIds(reqVO.getIds());
            if (CollectionUtil.isEmpty(marketingProductSkus)) {
                return Collections.emptyList();
            }
            List<Long> skuIds = marketingProductSkus.stream().map(MarketingProductSku::getId).toList();
            Set<Long> spuIds = marketingProductSkus.stream().map(MarketingProductSku::getMarketingSpuId).collect(Collectors.toSet());
            List<MarketingProductSpu> marketingProductSpus = marketingProductSpuMapper.selectListByIds(spuIds);
            Map<Long, MarketingProductSpu> spuId2SpuMap = marketingProductSpus.stream().collect(Collectors.toMap(MarketingProductSpu::getId, spu -> spu));
            Map<Long, List<MarketingProductRentalMethodDto>> spuId2RentalMethodDtoMap = buildRentalMethodDtoMap(
                    rentalMethodPropertyMapper.selectListByMarketingSpuIds(spuIds));

            List<MarketingProductSkuPropertyValue> skuPropertyValues = skuPropertyValueMapper.selectListBySkuIds(skuIds);
            if (CollectionUtil.isEmpty(skuPropertyValues)) {
                return Collections.emptyList();
            }
            Map<Long, List<Long>> skuId2SpuPropertyValuesIdsMap = skuPropertyValues.stream().collect(Collectors.groupingBy(
                    MarketingProductSkuPropertyValue::getMarketingProductSkuId, Collectors.mapping(
                            MarketingProductSkuPropertyValue::getMarketingSpuPropertyValueId, Collectors.toList())));
            List<Long> marketingSpuPropertyValuesIds = skuId2SpuPropertyValuesIdsMap.values().stream().flatMap(Collection::stream).toList();

            List<MarketingProductSpuPropertyValue> marketingSpuPropertyValues = spuPropertyValueMapper.selectListByIds(marketingSpuPropertyValuesIds);
            List<Long> productPropertyIds = Lists.newArrayList();
            List<Long> productPropertyValueIds = Lists.newArrayList();
            for (MarketingProductSpuPropertyValue spuPropertyValue : marketingSpuPropertyValues) {
                productPropertyIds.add(spuPropertyValue.getProductPropertyId());
                if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                    productPropertyValueIds.add(spuPropertyValue.getProductPropertyValueId());
                }
            }

            List<ProductProperty> productProperties = productPropertyMapper.selectListByIds(productPropertyIds);
            Map<Long, String> productPropertyId2NameMap = productProperties.stream().collect(Collectors.toMap(ProductProperty::getId, ProductProperty::getName));

            Map<Long, String> productPropertyValueId2ValueMap = Maps.newHashMap();
            if (CollectionUtil.isNotEmpty(productPropertyValueIds)) {
                List<ProductPropertyValue> productPropertyValues = productPropertyValueMapper.selectListByIds(productPropertyValueIds);
                productPropertyValueId2ValueMap = productPropertyValues.stream().collect(Collectors.toMap(ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue));
            }


            for (MarketingProductSku marketingProductSku : marketingProductSkus) {
                ProductDetailRespVO productDetailRespVO = new ProductDetailRespVO();
                detailRespVOS.add(productDetailRespVO);
                productDetailRespVO.setId(marketingProductSku.getId());
                MarketingProductSpu marketingProductSpu = spuId2SpuMap.get(marketingProductSku.getMarketingSpuId());
                productDetailRespVO.setName(marketingProductSpu.getName());
                productDetailRespVO.setProductType(marketingProductSpu.getType());
                productDetailRespVO.setRentalMethods(spuId2RentalMethodDtoMap.getOrDefault(marketingProductSpu.getId(), Collections.emptyList()));
                productDetailRespVO.setTotalPrice(marketingProductSku.getTotalPrice());
                productDetailRespVO.setDailyRent(marketingProductSku.getDailyRent());

                List<Long> spuPropertyValuesIds = skuId2SpuPropertyValuesIdsMap.get(marketingProductSku.getId());
                List<PropertyVO> properties = Lists.newArrayList();
                for (MarketingProductSpuPropertyValue spuPropertyValue : marketingSpuPropertyValues) {
                    if (spuPropertyValuesIds.contains(spuPropertyValue.getId())) {
                        PropertyVO propertyVO = new PropertyVO();
                        properties.add(propertyVO);

                        propertyVO.setPropertyId(spuPropertyValue.getProductPropertyId());
                        propertyVO.setPropertyName(productPropertyId2NameMap.get(spuPropertyValue.getProductPropertyId()));
                        List<PropertyValueVO> propertyValues = Lists.newArrayList();
                        propertyVO.setPropertyValues(propertyValues);
                        PropertyValueVO propertyValueVO = new PropertyValueVO();
                        propertyValues.add(propertyValueVO);
                        propertyValueVO.setPropertyValueId(spuPropertyValue.getProductPropertyValueId());
                        propertyValueVO.setPropertyValue(spuPropertyValue.getPropertyValue());
                        if (Objects.nonNull(spuPropertyValue.getProductPropertyId())) {
                            propertyValueVO.setPropertyValue(productPropertyValueId2ValueMap.get(spuPropertyValue.getProductPropertyValueId()));
                        }
                        propertyValueVO.setPropertyPics(Lists.newArrayList(spuPropertyValue.getPicUrl()));
                    }
                }
                productDetailRespVO.setProperties(properties);
            }
        } else {
            List<MarketingProductSpu> marketingProductSpus = marketingProductSpuMapper.selectListByIds(reqVO.getIds());
            if (CollectionUtil.isEmpty(marketingProductSpus)) {
                return Collections.emptyList();
            }
            // 获取spu下的sku的最低日租金
            List<IdAndPriceDTO> dailyRentPrices = skuMapper.selectSpuLowestDailyRentPricesBySpuIds(reqVO.getIds());
            Map<Long, Long> spuId2LowestDailyRentPricesMap = dailyRentPrices.stream()
                    .filter(dto -> dto.getId() != null && dto.getMinPrice() != null)  // 过滤 null 值
                    .collect(Collectors.toMap(IdAndPriceDTO::getId, IdAndPriceDTO::getMinPrice));

            // 属性
            List<Long> spuIds = marketingProductSpus.stream().map(MarketingProductSpu::getId).toList();
            Map<Long, List<MarketingProductRentalMethodDto>> spuId2RentalMethodDtoMap = buildRentalMethodDtoMap(
                    rentalMethodPropertyMapper.selectListByMarketingSpuIds(spuIds));
            List<MarketingProductSpuProperty> spuProperties = spuPropertyMapper.selectListBySpuIds(spuIds);
            Map<Long, List<MarketingProductSpuProperty>> spuId2SpuPropertiesMap = spuProperties.stream().collect(
                    Collectors.groupingBy(MarketingProductSpuProperty::getMarketingSpuId));
            List<Long> productPropertyIds = spuProperties.stream().map(MarketingProductSpuProperty::getProductPropertyId).toList();
            List<ProductProperty> productProperties = productPropertyMapper.selectListByIds(productPropertyIds);
            Map<Long, String> productPropertyId2NameMap = productProperties.stream().collect(Collectors.toMap(ProductProperty::getId, ProductProperty::getName));

            // 属性值
            List<MarketingProductSpuPropertyValue> spuPropertyValues = spuPropertyValueMapper.selectListBySpuIds(spuIds);
            Map<Long, Map<Long, List<MarketingProductSpuPropertyValue>>> spuId2SpuPropertyValuesMap = spuPropertyValues.stream().collect(
                    Collectors.groupingBy(MarketingProductSpuPropertyValue::getMarketingSpuId,
                            Collectors.groupingBy(MarketingProductSpuPropertyValue::getSpuPropertyId)));
            List<Long> productPropertyValueIds = spuPropertyValues.stream().map(MarketingProductSpuPropertyValue::getProductPropertyValueId).toList();
            List<ProductPropertyValue> productPropertyValues = productPropertyValueMapper.selectListByIds(productPropertyValueIds);
            Map<Long, String> productPropertyValueId2ValueMap = productPropertyValues.stream().collect(Collectors.toMap(
                    ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue));

            for (MarketingProductSpu marketingProductSpu : marketingProductSpus) {
                ProductDetailRespVO productDetailRespVO = new ProductDetailRespVO();
                detailRespVOS.add(productDetailRespVO);

                productDetailRespVO.setId(marketingProductSpu.getId());
                productDetailRespVO.setCode(marketingProductSpu.getCode());
                productDetailRespVO.setName(marketingProductSpu.getName());
                productDetailRespVO.setProductType(marketingProductSpu.getType());
                productDetailRespVO.setRentalMethods(spuId2RentalMethodDtoMap.getOrDefault(marketingProductSpu.getId(), Collections.emptyList()));
                productDetailRespVO.setDailyRent(spuId2LowestDailyRentPricesMap.get(marketingProductSpu.getId()));
                productDetailRespVO.setMainPics(List.of(StringUtils.split(marketingProductSpu.getMainPicUrls(), ",")));

                List<PropertyVO> properties = Lists.newArrayList();
                productDetailRespVO.setProperties(properties);
                if (MapUtil.isNotEmpty(spuId2SpuPropertiesMap)) {
                    for (MarketingProductSpuProperty spuProperty : spuId2SpuPropertiesMap.get(marketingProductSpu.getId())) {
                        PropertyVO propertyVO = new PropertyVO();
                        properties.add(propertyVO);
                        propertyVO.setPropertyId(spuProperty.getProductPropertyId());
                        propertyVO.setPropertyName(productPropertyId2NameMap.get(spuProperty.getProductPropertyId()));

                        List<PropertyValueVO> propertyValues = Lists.newArrayList();
                        propertyVO.setPropertyValues(propertyValues);
                        for (MarketingProductSpuPropertyValue spuPropertyValue : spuId2SpuPropertyValuesMap.get(marketingProductSpu.getId()).get(spuProperty.getId())) {
                            PropertyValueVO propertyValueVO = new PropertyValueVO();
                            propertyValues.add(propertyValueVO);

                            propertyValueVO.setPropertyValueId(spuPropertyValue.getProductPropertyValueId());
                            if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                                propertyValueVO.setPropertyValue(productPropertyValueId2ValueMap.get(spuPropertyValue.getProductPropertyValueId()));
                            } else {
                                propertyValueVO.setPropertyValue(spuPropertyValue.getPropertyValue());
                            }
                        }
                    }
                    productDetailRespVO.setProperties(properties);
                }
            }
        }

        return detailRespVOS;
    }

    @Override
    public PageResult<SpuRespVO> spuListForAddCoupon(ProductListReqVO reqVO) {
        log.info("list product spu dto: {}", reqVO);

        ProductQuery query = reqVO.convert2ProductQuery();
        if (CollectionUtil.isNotEmpty(reqVO.getPropertyValues())) {
            Set<Long> marketingSpuIds = spuPropertyValueMapper.selectMarketingSpuIdsByPropertyValuesIds(reqVO.getPropertyValues(), reqVO.getPropertyValues().size());
            if (CollectionUtil.isEmpty(marketingSpuIds)) {
                return PageResult.empty();
            }
            query.setMarketingSpuIds(marketingSpuIds);
        }
        List<MarketingProductSpu> marketingProductSpus = marketingProductSpuMapper.selectListByCondition(query);
        if (CollectionUtil.isEmpty(marketingProductSpus)) {
            return PageResult.empty();
        }
        long count = marketingProductSpuMapper.selectCountByCondition(query);

        List<Long> spuIds = marketingProductSpus.stream().map(MarketingProductSpu::getId).toList();
        // 获取spu下的sku的最低日租金
        List<IdAndPriceDTO> dailyRentPrices = skuMapper.selectSpuLowestDailyRentPricesBySpuIds(spuIds);
        Map<Long, Long> spuId2LowestDailyRentPricesMap = dailyRentPrices.stream()
                .filter(dto -> dto.getId() != null && dto.getMinPrice() != null)  // 过滤 null 值
                .collect(Collectors.toMap(IdAndPriceDTO::getId, IdAndPriceDTO::getMinPrice));

        // 属性
        List<MarketingProductSpuProperty> spuProperties = spuPropertyMapper.selectListBySpuIds(spuIds);
        Map<Long, List<MarketingProductSpuProperty>> spuId2SpuPropertiesMap = spuProperties.stream().collect(
                Collectors.groupingBy(MarketingProductSpuProperty::getMarketingSpuId));
        List<Long> productPropertyIds = spuProperties.stream().map(MarketingProductSpuProperty::getProductPropertyId).toList();
        List<ProductProperty> productProperties = productPropertyMapper.selectListByIds(productPropertyIds);
        Map<Long, String> productPropertyId2NameMap = productProperties.stream().collect(Collectors.toMap(ProductProperty::getId, ProductProperty::getName));

        // 属性值
        List<MarketingProductSpuPropertyValue> spuPropertyValues = spuPropertyValueMapper.selectListBySpuIds(spuIds);
        Map<Long, Map<Long, List<MarketingProductSpuPropertyValue>>> spuId2PropertyValueIdsMap = spuPropertyValues.stream().collect(
                Collectors.groupingBy(MarketingProductSpuPropertyValue::getMarketingSpuId,
                        Collectors.groupingBy(MarketingProductSpuPropertyValue::getSpuPropertyId)));
        List<Long> productPropertyValueIds = spuPropertyValues.stream().map(MarketingProductSpuPropertyValue::getProductPropertyValueId).toList();
        List<ProductPropertyValue> productPropertyValues = productPropertyValueMapper.selectListByIds(productPropertyValueIds);
        Map<Long, String> productPropertyValueId2ValueMap = productPropertyValues.stream().collect(Collectors.toMap(
                ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue));

        List<SpuRespVO> spuRespVOS = Lists.newArrayList();
        for (MarketingProductSpu marketingProductSpu : marketingProductSpus) {
            SpuRespVO spuRespVO = new SpuRespVO();
            spuRespVOS.add(spuRespVO);

            spuRespVO.setId(marketingProductSpu.getId());
            spuRespVO.setCode(marketingProductSpu.getCode());
            spuRespVO.setName(marketingProductSpu.getName());
            spuRespVO.setMainPics(List.of(StringUtils.split(marketingProductSpu.getMainPicUrls(), ",")));

            List<PropertyVO> properties = Lists.newArrayList();
            spuRespVO.setProperties(properties);
            for (MarketingProductSpuProperty spuProperty : spuId2SpuPropertiesMap.get(marketingProductSpu.getId())) {
                PropertyVO propertyVO = new PropertyVO();
                properties.add(propertyVO);

                propertyVO.setPropertyId(spuProperty.getProductPropertyId());
                propertyVO.setPropertyName(productPropertyId2NameMap.get(spuProperty.getProductPropertyId()));

                List<PropertyValueVO> propertyValues = Lists.newArrayList();
                propertyVO.setPropertyValues(propertyValues);
                for (MarketingProductSpuPropertyValue spuPropertyValue : spuId2PropertyValueIdsMap.get(marketingProductSpu.getId()).get(spuProperty.getId())) {
                    PropertyValueVO propertyValueVO = new PropertyValueVO();
                    propertyValues.add(propertyValueVO);

                    propertyValueVO.setPropertyValueId(spuPropertyValue.getProductPropertyValueId());
                    if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                        propertyValueVO.setPropertyValue(productPropertyValueId2ValueMap.get(spuPropertyValue.getProductPropertyValueId()));
                    } else {
                        propertyValueVO.setPropertyValue(spuPropertyValue.getPropertyValue());
                    }
                }
            }

            // spu下对应的sku的最低日租金
            spuRespVO.setDailyRent(spuId2LowestDailyRentPricesMap.get(marketingProductSpu.getId()));
        }
        return new PageResult<>(spuRespVOS, count);
    }

    @Override
    public PageResult<SkuRespVO> skuListForAddCoupon(ProductListReqVO reqVO) {
        log.info("list product sku dto: {}", reqVO);
        ProductQuery query = reqVO.convert2ProductQuery();
        if (CollectionUtil.isNotEmpty(reqVO.getPropertyValues())) {
            Set<Long> marketingSkuIds = spuPropertyValueMapper.selectMarketingSkuIdsByPropertyValuesIds(reqVO.getPropertyValues(), reqVO.getPropertyValues().size());
            if (CollectionUtil.isEmpty(marketingSkuIds)) {
                return PageResult.empty();
            }
            query.setMarketingSkuIds(marketingSkuIds);
        }

        List<MarketingProductSku> marketingProductSKus = skuMapper.selectListByCondition(query);
        if (CollectionUtil.isEmpty(marketingProductSKus)) {
            return PageResult.empty();
        }
        long count = skuMapper.selectCountByCondition(query);

        List<Long> skuIds = marketingProductSKus.stream().map(MarketingProductSku::getId).toList();
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

        List<SkuRespVO> skuRespVOS = Lists.newArrayList();
        for (MarketingProductSku sku : marketingProductSKus) {
            SkuRespVO skuRespVO = new SkuRespVO();
            skuRespVOS.add(skuRespVO);

            skuRespVO.setId(sku.getId());
            skuRespVO.setName(sku.getName());
            skuRespVO.setDailyRent(sku.getDailyRent());

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

        return new PageResult<>(skuRespVOS, count);
    }

    @Override
    public SkuRespDto getSkuInfoById(Long skuId) {
        if (skuId == null) {
            return null;
        }
        MarketingProductSku marketingProductSku = skuMapper.selectById(skuId);
        if (marketingProductSku == null) {
            return null;
        }

        // sku对应的spu信息
        MarketingProductSpu marketingProductSpu = marketingProductSpuMapper.selectById(marketingProductSku.getMarketingSpuId());

        // spu对应的标准商品
        StandardProductSpu standardProduct = standardProductSpuMapper.selectById(marketingProductSpu.getStandardProductSpuId());
        // 品牌名称
        String brandName = brandMapper.selectNameById(standardProduct.getProductBrandId());
        // 营销类目名称
        String marketingCategoryName = marketingCategoryMapper.selectNameById(standardProduct.getMarketingCategoryId());
        // 经营类目名称
        String businessCategoryName = businessCategoryMapper.selectNameById(standardProduct.getBusinessCategoryId());

        // 获取sku对应的属性id和属性值id
        List<Long> spuPropertyValueIds = skuPropertyValueMapper.selectSpuPropertyValueIdsBySkuIds(Lists.newArrayList(skuId));
        List<MarketingProductSpuPropertyValue> spuPropertyValues = spuPropertyValueMapper.selectListByIds(spuPropertyValueIds);
        List<Long> productPropertyIds = Lists.newArrayList();
        List<Long> productPropertyValueIds = Lists.newArrayList();
        for (MarketingProductSpuPropertyValue spuPropertyValue : spuPropertyValues) {
            productPropertyIds.add(spuPropertyValue.getProductPropertyId());
            if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                productPropertyValueIds.add(spuPropertyValue.getProductPropertyValueId());
            }
        }
        // 属性
        List<IdAndNameVO> productIdAndNameVOS = productPropertyMapper.selectIdAndNamesByIds(productPropertyIds);
        Map<Long, String> propertyId2NameMap = productIdAndNameVOS.stream().collect(Collectors.toMap(IdAndNameVO::getId, IdAndNameVO::getName));
        // 属性值
        List<ProductPropertyValue> productPropertyValues = productPropertyValueMapper.selectListByIds(productPropertyValueIds);
        Map<Long, String> propertyValueId2ValueMap = productPropertyValues.stream().collect(Collectors.toMap(
                ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue));

        // 基本信息
        SkuRespDto skuRespDto = new SkuRespDto();
        skuRespDto.setId(marketingProductSku.getId());
        skuRespDto.setMarketingSpuId(marketingProductSku.getMarketingSpuId());
        skuRespDto.setOfficialPrice(marketingProductSku.getOfficialPrice());
        skuRespDto.setTotalPriceFactor(marketingProductSku.getTotalPriceFactor());
        skuRespDto.setTotalRentFactor(marketingProductSku.getTotalRentFactor());
        skuRespDto.setTotalPrice(marketingProductSku.getTotalPrice());
        skuRespDto.setTotalRent(marketingProductSku.getTotalRent());
        skuRespDto.setBuyoutAmount(marketingProductSku.getBuyoutAmount());
        skuRespDto.setDailyRent(marketingProductSku.getDailyRent());
        skuRespDto.setPremium(marketingProductSku.getPremium());
        skuRespDto.setSuggestedRetailPrice(marketingProductSku.getSuggestedRetailPrice());
        skuRespDto.setStrikethroughPrice(marketingProductSku.getStrikethroughPrice());
        skuRespDto.setCashUsageRatio(marketingProductSku.getCashUsageRatio());
        skuRespDto.setPointsUsageRatio(marketingProductSku.getPointsUsageRatio());
        skuRespDto.setPointsCount(marketingProductSku.getPointsCount());
        skuRespDto.setCashPrice(marketingProductSku.getCashPrice());
        skuRespDto.setIsAllowOrder(marketingProductSku.getIsAllowOrder());
        skuRespDto.setPartnerId(marketingProductSku.getPartnerId());
        skuRespDto.setStock(marketingProductSku.getStock());

        // 属性信息
        List<PropertyVO> propertyVOS = Lists.newArrayList();
        skuRespDto.setProperties(propertyVOS);
        for (MarketingProductSpuPropertyValue spuPropertyValue : spuPropertyValues) {
            PropertyVO propertyVO = new PropertyVO();
            propertyVOS.add(propertyVO);
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

        // 对应的商品信息
        SpuRespDto spuRespDto = new SpuRespDto();
        skuRespDto.setSpuInfo(spuRespDto);
        spuRespDto.setId(marketingProductSpu.getStandardProductSpuId());
        spuRespDto.setName(marketingProductSpu.getName());
        spuRespDto.setCode(marketingProductSpu.getCode());
        spuRespDto.setProductCondition(marketingProductSpu.getProductCondition());
        spuRespDto.setProductConditionDesc(ProductConditionEnum.get(marketingProductSpu.getProductCondition()).getDesc());
        spuRespDto.setMonitorAttribute(marketingProductSpu.getMonitorAttribute());
        spuRespDto.setMonitorAttributeDesc(MonitorAtttributeEnum.get(marketingProductSpu.getMonitorAttribute()).getDesc());
        spuRespDto.setBrandName(brandName);
        spuRespDto.setBusinessCategoryName(businessCategoryName);
        spuRespDto.setMarketingCategoryName(marketingCategoryName);
        spuRespDto.setMainPicUrls(List.of(marketingProductSpu.getMainPicUrls().split(",")));
        spuRespDto.setShelvesStatus(marketingProductSpu.getShelvesStatus());
        spuRespDto.setApprovalStatus(marketingProductSpu.getApprovalStatus());
        return skuRespDto;
    }

    @Override
    public void updateProductShelvesStatus() {
        marketingProductSpuMapper.updateProductShelvesStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkuStock(List<UpdateSkuStockReqVO> reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("更新SKU库存,reqVO:{},operatorId:{}", reqVO, loginUser.getId());

        if (CollUtil.isEmpty(reqVO)) {
            return;
        }
        reqVO.forEach(this::validateUpdateSkuStockReq);

        Date now = new Date();
        Set<String> uniqueKeys = Sets.newHashSet();
        Set<Long> skuIds = reqVO.stream().map(UpdateSkuStockReqVO::getSkuId).collect(Collectors.toSet());
        Map<String, MarketingProductSkuRentalMethodProperty> rentalStockMap = skuRentalMethodPropertyMapper
                .selectListByMarketingSkuIds(skuIds)
                .stream()
                .collect(Collectors.toMap(
                        item -> buildSkuRentalStockKey(item.getMarketingSkuId(), item.getRentalMethod(), item.getRentalPeriodMonth()),
                        Function.identity()));
        List<UpdateSkuStockDTO> updateSkuStockDTOS = Lists.newArrayList();
        reqVO.forEach(updateSkuStockDTO -> {
            String uniqueKey = buildSkuRentalStockKey(updateSkuStockDTO.getSkuId(), updateSkuStockDTO.getRentalMethod(),
                    updateSkuStockDTO.getRentalPeriodMonth());
            if (!uniqueKeys.add(uniqueKey)) {
                throw exception(RENTAL_STOCK_DUPLICATE);
            }
            if (!rentalStockMap.containsKey(uniqueKey)) {
                throw exception(RENTAL_STOCK_NOT_EXIST);
            }
            UpdateSkuStockDTO updateSkuStock = new UpdateSkuStockDTO();
            updateSkuStock.setSkuId(updateSkuStockDTO.getSkuId());
            updateSkuStock.setRentalMethod(updateSkuStockDTO.getRentalMethod());
            updateSkuStock.setRentalPeriodMonth(updateSkuStockDTO.getRentalPeriodMonth());
            updateSkuStock.setStock(updateSkuStockDTO.getStock());
            updateSkuStock.setUpdateBy(loginUser.getId());
            updateSkuStock.setUpdateTime(now);
            updateSkuStockDTOS.add(updateSkuStock);
        });
        skuRentalMethodPropertyMapper.updateSkuStock(updateSkuStockDTOS);
    }

    private void validateUpdateSkuStockReq(UpdateSkuStockReqVO reqVO) {
        if (reqVO == null) {
            throw exception(RENTAL_STOCK_NOT_EXIST);
        }
        if (reqVO.getSkuId() == null) {
            throw exception(SKU_ID_IS_NULL);
        }
        if (reqVO.getRentalMethod() == null || reqVO.getRentalPeriodMonth() == null) {
            throw exception(RENTAL_METHOD_PERIOD_REQUIRED);
        }
        if (reqVO.getStock() == null || reqVO.getStock() < 0) {
            throw exception(RENTAL_STOCK_INVALID);
        }
    }

    private String buildSkuRentalStockKey(Long skuId, Integer rentalMethod, Integer rentalPeriodMonth) {
        return skuId + "_" + rentalMethod + "_" + rentalPeriodMonth;
    }

    @Override
    public SkuRentalPriceRespDto getSkuRentalPrice(Long skuId, Integer rentalMethod, Integer rentalPeriodMonth) {
        MarketingProductSkuRentalMethodProperty row =
                skuRentalMethodPropertyMapper.selectByMethodAndPeriod(skuId, rentalMethod, rentalPeriodMonth);
        if (row == null) {
            throw exception(RENTAL_PRICE_NOT_EXIST);
        }
        SkuRentalPriceRespDto dto = new SkuRentalPriceRespDto();
        dto.setId(row.getId());
        dto.setMarketingSpuId(row.getMarketingSpuId());
        dto.setMarketingSkuId(row.getMarketingSkuId());
        dto.setRentalMethod(row.getRentalMethod());
        dto.setRentalPeriodMonth(row.getRentalPeriodMonth());
        dto.setTotalRent(row.getTotalRent());
        dto.setMonthlyRent(row.getMonthlyRent());
        dto.setDailyRent(row.getDailyRent());
        dto.setBuyoutAmount(row.getBuyoutAmount());
        dto.setPremium(row.getPremium());
        dto.setStock(row.getStock());
        return dto;
    }

    @Override
    public boolean deductRentalStock(SkuRentalStockReqDto reqDto) {
        int affected = skuRentalMethodPropertyMapper.deductStock(
                reqDto.getSkuId(), reqDto.getRentalMethod(), reqDto.getRentalPeriodMonth(),
                reqDto.getQuantity(), new Date());
        log.info("扣减租期库存,reqDto:{},affected:{}", reqDto, affected);
        return affected > 0;
    }

    @Override
    public boolean restoreRentalStock(SkuRentalStockReqDto reqDto) {
        int affected = skuRentalMethodPropertyMapper.restoreStock(
                reqDto.getSkuId(), reqDto.getRentalMethod(), reqDto.getRentalPeriodMonth(),
                reqDto.getQuantity(), new Date());
        log.info("回补租期库存,reqDto:{},affected:{}", reqDto, affected);
        return affected > 0;
    }

    @Override
    public List<MarketingAvailablePropertyVO> availablePropertiesByStandardSpuId(Long standardProductSpuId) {
        List<MarketingAvailablePropertyVO> properties = queryAvailablePropertiesByStandardSpuId(standardProductSpuId);
        if (CollectionUtil.isEmpty(properties)) {
            throw exception(STANDARD_PRODUCT_PROPERTY_SCOPE_EMPTY);
        }
        return properties;
    }

    @Override
    public Long getResidualAmount(Long marketingSkuId, Integer globalMonth) {
        log.info("[getResidualAmount] start, marketingSkuId={}, globalMonth={}", marketingSkuId, globalMonth);
        if (marketingSkuId == null || globalMonth == null || globalMonth < 1) {
            return null;
        }
        Long standardProductSkuId = resolveStandardProductSkuId(marketingSkuId);
        if (standardProductSkuId == null) {
            log.info("[getResidualAmount] standard sku not resolved, marketingSkuId={}", marketingSkuId);
            return null;
        }
        MarketingProductSku marketingSku = skuMapper.selectById(marketingSkuId);
        if (marketingSku == null || marketingSku.getPartnerId() == null) {
            log.info("[getResidualAmount] marketingSku not found or partnerId null, marketingSkuId={}", marketingSkuId);
            return null;
        }
        Long partnerId = marketingSku.getPartnerId();
        AssetResidualConfig config = assetResidualConfigMapper.selectBySkuId(standardProductSkuId, partnerId);
        if (config == null) {
            log.info("[getResidualAmount] asset residual config not found, standardSkuId={}, partnerId={}",
                    standardProductSkuId, partnerId);
            return null;
        }
        List<AssetResidualMonthConfig> months = assetResidualMonthConfigMapper.selectByConfigId(
                config.getId(), partnerId);
        if (CollectionUtil.isEmpty(months)) {
            log.info("[getResidualAmount] month configs empty, configId={}, partnerId={}", config.getId(), partnerId);
            return null;
        }
        for (AssetResidualMonthConfig row : months) {
            if (Objects.equals(row.getGlobalMonth(), globalMonth)) {
                log.info("[getResidualAmount] found, marketingSkuId={}, standardSkuId={}, globalMonth={}, amount={}",
                        marketingSkuId, standardProductSkuId, globalMonth, row.getCurrentPurchaseAmount());
                return row.getCurrentPurchaseAmount();
            }
        }
        log.info("[getResidualAmount] globalMonth not in residual table, marketingSkuId={}, globalMonth={}, maxMonth={}",
                marketingSkuId, globalMonth, months.get(months.size() - 1).getGlobalMonth());
        return null;
    }

    @Override
    public List<SkuRentalPriceRespDto> listSkuRentalPrices(Long skuId, Integer rentalMethod) {
        log.info("[listSkuRentalPrices] skuId={}, rentalMethod={}", skuId, rentalMethod);
        if (skuId == null || rentalMethod == null) {
            return List.of();
        }
        List<MarketingProductSkuRentalMethodProperty> rows = skuRentalMethodPropertyMapper
                .selectListByMarketingSkuIds(List.of(skuId));
        if (CollectionUtil.isEmpty(rows)) {
            log.info("[listSkuRentalPrices] no rental rows found, skuId={}", skuId);
            return List.of();
        }
        List<SkuRentalPriceRespDto> result = new ArrayList<>();
        for (MarketingProductSkuRentalMethodProperty row : rows) {
            if (!Objects.equals(row.getRentalMethod(), rentalMethod)) {
                continue;
            }
            SkuRentalPriceRespDto dto = new SkuRentalPriceRespDto();
            dto.setId(row.getId());
            dto.setMarketingSpuId(row.getMarketingSpuId());
            dto.setMarketingSkuId(row.getMarketingSkuId());
            dto.setRentalMethod(row.getRentalMethod());
            dto.setRentalPeriodMonth(row.getRentalPeriodMonth());
            dto.setTotalRent(row.getTotalRent());
            dto.setMonthlyRent(row.getMonthlyRent());
            dto.setDailyRent(row.getDailyRent());
            dto.setBuyoutAmount(row.getBuyoutAmount());
            dto.setPremium(row.getPremium());
            dto.setStock(row.getStock());
            result.add(dto);
        }
        result.sort(Comparator.comparingInt(o -> o.getRentalPeriodMonth() == null ? 0 : o.getRentalPeriodMonth()));
        log.info("[listSkuRentalPrices] skuId={}, rentalMethod={}, resultSize={}", skuId, rentalMethod, result.size());
        return result;
    }

    /**
     * 营销SKU → 标品SKU 桥接：
     * 1. 通过营销SKU取所属营销SPU；2. 取标品SPU；3. 取该营销SKU的属性值列表 → product_property_value_id 集合；
     * 4. 在标品SPU属性值表中按 product_property_value_id 反查，得到标品SPU属性值ID集合；
     * 5. 在标品SKU属性值表中找同时拥有这些属性值的标品SKU。
     * 任一步缺失返回 null。
     */
    private Long resolveStandardProductSkuId(Long marketingSkuId) {
        MarketingProductSku marketingSku = skuMapper.selectById(marketingSkuId);
        if (marketingSku == null || marketingSku.getMarketingSpuId() == null) {
            log.info("[resolveStdSku] marketingSku not found, marketingSkuId={}", marketingSkuId);
            return null;
        }
        MarketingProductSpu marketingSpu = marketingProductSpuMapper.selectById(marketingSku.getMarketingSpuId());
        if (marketingSpu == null || marketingSpu.getStandardProductSpuId() == null) {
            log.info("[resolveStdSku] marketingSpu not found or no standardSpuId, marketingSpuId={}",
                    marketingSku.getMarketingSpuId());
            return null;
        }
        Long standardSpuId = marketingSpu.getStandardProductSpuId();

        List<MarketingProductSkuPropertyValue> mkSkuPVList = skuPropertyValueMapper
                .selectListBySkuIds(List.of(marketingSkuId));
        if (CollectionUtil.isEmpty(mkSkuPVList)) {
            log.info("[resolveStdSku] sku property values empty, marketingSkuId={}", marketingSkuId);
            return null;
        }
        Set<Long> mkSpuPropertyValueIds = mkSkuPVList.stream()
                .map(MarketingProductSkuPropertyValue::getMarketingSpuPropertyValueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (mkSpuPropertyValueIds.isEmpty()) {
            log.info("[resolveStdSku] mkSpuPropertyValueIds empty after filter, marketingSkuId={}", marketingSkuId);
            return null;
        }
        List<MarketingProductSpuPropertyValue> mkSpuPVList = spuPropertyValueMapper
                .selectListByIds(mkSpuPropertyValueIds);
        if (CollectionUtil.isEmpty(mkSpuPVList)) {
            log.info("[resolveStdSku] spu property values not found for ids={}", mkSpuPropertyValueIds);
            return null;
        }
        Set<Long> productPropertyValueIds = mkSpuPVList.stream()
                .map(MarketingProductSpuPropertyValue::getProductPropertyValueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (productPropertyValueIds.isEmpty()) {
            log.info("[resolveStdSku] productPropertyValueIds empty, marketingSkuId={}", marketingSkuId);
            return null;
        }
        List<StandardProductSpuPropertyValue> stdSpuPVList = standardProductSpuPropertyValueMapper
                .selectListByStandardSpuId(standardSpuId);
        if (CollectionUtil.isEmpty(stdSpuPVList)) {
            log.info("[resolveStdSku] standard spu property values empty, standardSpuId={}", standardSpuId);
            return null;
        }
        Set<Long> stdSpuPropertyValueIds = stdSpuPVList.stream()
                .filter(pv -> pv.getProductPropertyValueId() != null
                        && productPropertyValueIds.contains(pv.getProductPropertyValueId()))
                .map(StandardProductSpuPropertyValue::getId)
                .collect(Collectors.toSet());
        if (stdSpuPropertyValueIds.isEmpty()) {
            log.info("[resolveStdSku] no matching standard property values, standardSpuId={}, productPVIds={}",
                    standardSpuId, productPropertyValueIds);
            return null;
        }
        List<StandardProductSkuPropertyValue> stdSkuPVList = standardProductSkuPropertyValueMapper
                .selectListByStandardSpuId(standardSpuId);
        if (CollectionUtil.isEmpty(stdSkuPVList)) {
            log.info("[resolveStdSku] standard sku property values empty, standardSpuId={}", standardSpuId);
            return null;
        }
        Map<Long, Set<Long>> stdSkuToPVs = new HashMap<>();
        for (StandardProductSkuPropertyValue pv : stdSkuPVList) {
            if (pv.getStandardProductSkuId() == null || pv.getStandardSpuPropertyValueId() == null) {
                continue;
            }
            stdSkuToPVs.computeIfAbsent(pv.getStandardProductSkuId(), k -> new HashSet<>())
                    .add(pv.getStandardSpuPropertyValueId());
        }
        for (Map.Entry<Long, Set<Long>> entry : stdSkuToPVs.entrySet()) {
            if (entry.getValue().containsAll(stdSpuPropertyValueIds)) {
                log.info("[resolveStdSku] resolved, marketingSkuId={} → standardSkuId={}", marketingSkuId, entry.getKey());
                return entry.getKey();
            }
        }
        log.info("[resolveStdSku] no matching standard sku found, marketingSkuId={}, standardSpuId={}, requiredPVIds={}",
                marketingSkuId, standardSpuId, stdSpuPropertyValueIds);
        return null;
    }

    @Override
    public AssetConfigDto getAssetConfig(Long marketingSkuId) {
        log.info("[getAssetConfig] start, marketingSkuId={}", marketingSkuId);
        Long standardProductSkuId = resolveStandardProductSkuId(marketingSkuId);
        if (standardProductSkuId == null) {
            log.warn("[getAssetConfig] standard sku not resolved, marketingSkuId={}", marketingSkuId);
            return null;
        }
        MarketingProductSku marketingSku = skuMapper.selectById(marketingSkuId);
        if (marketingSku == null || marketingSku.getPartnerId() == null) {
            log.warn("[getAssetConfig] marketingSku not found or partnerId null, marketingSkuId={}", marketingSkuId);
            return null;
        }
        Long partnerId = marketingSku.getPartnerId();
        MarketingProductSpu marketingSpu = marketingProductSpuMapper.selectById(marketingSku.getMarketingSpuId());
        Long standardSpuId = marketingSpu != null ? marketingSpu.getStandardProductSpuId() : null;

        AssetResidualConfig residualConfig = assetResidualConfigMapper.selectBySkuId(standardProductSkuId, partnerId);
        if (residualConfig == null) {
            log.warn("[getAssetConfig] residual config not found, standardSkuId={}, partnerId={}", standardProductSkuId, partnerId);
            return null;
        }

        AssetConfigDto result = new AssetConfigDto();
        result.setStandardSpuId(standardSpuId);
        result.setStandardProductSkuId(standardProductSkuId);
        result.setPartnerId(partnerId);
        result.setOfficialPrice(residualConfig.getOfficialPrice());
        result.setDepreciationRuleType(residualConfig.getDepreciationRuleType());
        result.setDepreciationRuleSubType(residualConfig.getDepreciationRuleSubType());

        List<AssetResidualYearConfig> yearRows = assetResidualYearConfigMapper.selectByConfigId(
                residualConfig.getId(), partnerId);
        List<AssetConfigYearDto> yearDtos = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(yearRows)) {
            for (AssetResidualYearConfig row : yearRows) {
                AssetConfigYearDto dto = new AssetConfigYearDto();
                dto.setUseYear(row.getUseYear());
                dto.setTotalPriceUpperCoefficient(row.getTotalPriceUpperCoefficient());
                dto.setTotalPriceLowerCoefficient(row.getTotalPriceLowerCoefficient());
                dto.setYearBeginValue(row.getYearBeginValue());
                dto.setYearDepreciationAmount(row.getYearDepreciationAmount());
                dto.setYearEndResidualValue(row.getYearEndResidualValue());
                dto.setTotalPriceUpperLimit(row.getTotalPriceUpperLimit());
                dto.setTotalPriceLowerLimit(row.getTotalPriceLowerLimit());
                yearDtos.add(dto);
            }
        }
        result.setYearConfigs(yearDtos);

        List<AssetResidualMonthConfig> monthRows = assetResidualMonthConfigMapper.selectByConfigId(
                residualConfig.getId(), partnerId);
        List<AssetConfigMonthDto> monthDtos = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(monthRows)) {
            for (AssetResidualMonthConfig row : monthRows) {
                AssetConfigMonthDto dto = new AssetConfigMonthDto();
                dto.setUseYear(row.getUseYear());
                dto.setUseMonth(row.getUseMonth());
                dto.setGlobalMonth(row.getGlobalMonth());
                dto.setDepreciationRuleValue(row.getDepreciationRuleValue());
                dto.setBeginValue(row.getBeginValue());
                dto.setDepreciationAmount(row.getDepreciationAmount());
                dto.setResidualValue(row.getResidualValue());
                dto.setAccumulatedDepreciationAmount(row.getAccumulatedDepreciationAmount());
                dto.setCurrentPurchaseAmount(row.getCurrentPurchaseAmount());
                monthDtos.add(dto);
            }
        }
        result.setMonthConfigs(monthDtos);

        List<AssetPricingConfig> pricingRows = assetPricingConfigMapper.selectBySkuId(standardProductSkuId, partnerId);
        List<AssetConfigPricingDto> pricingDtos = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(pricingRows)) {
            for (AssetPricingConfig row : pricingRows) {
                AssetConfigPricingDto dto = new AssetConfigPricingDto();
                dto.setLeaseMode(row.getLeaseMode());
                dto.setUseYear(row.getUseYear());
                dto.setDeviceValue(row.getDeviceValue());
                dto.setDeviceTotalPriceCoefficient(row.getDeviceTotalPriceCoefficient());
                dto.setDeviceTotalPrice(row.getDeviceTotalPrice());
                dto.setTotalRentCoefficient(row.getTotalRentCoefficient());
                dto.setTotalRent(row.getTotalRent());
                dto.setMonthlyRent(row.getMonthlyRent());
                dto.setDailyRent(row.getDailyRent());
                dto.setAnnualDepreciationAmount(row.getAnnualDepreciationAmount());
                dto.setExpirationPurchaseAmount(row.getExpirationPurchaseAmount());
                pricingDtos.add(dto);
            }
        }
        result.setPricingConfigs(pricingDtos);

        log.info("[getAssetConfig] success, marketingSkuId={}, standardSkuId={}, yearSize={}, monthSize={}, pricingSize={}",
                marketingSkuId, standardProductSkuId, yearDtos.size(), monthDtos.size(), pricingDtos.size());
        return result;
    }

    private List<MarketingProductRespVO> buildListResult(List<MarketingProductSpu> marketingProductSpus, Integer productType) {
        List<Long> standardProductSpuIds = Lists.newArrayList();
        Set<Long> userIds = Sets.newHashSet();
        List<Long> channelIds = Lists.newArrayList();
        List<Long> marketingSpuIds = Lists.newArrayList();
        for (MarketingProductSpu marketingProductSpu : marketingProductSpus) {
            standardProductSpuIds.add(marketingProductSpu.getStandardProductSpuId());
            marketingSpuIds.add(marketingProductSpu.getId());
            userIds.add(marketingProductSpu.getCreateBy());
            userIds.add(marketingProductSpu.getUpdateBy());
            if (StringUtils.isNotBlank(marketingProductSpu.getShelvingChannelId())) {
                Arrays.stream(marketingProductSpu.getShelvingChannelId().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::valueOf)
                        .forEach(channelIds::add);
            }
        }

        // 获取标准商品
        List<StandardProductSpu> standardProductSpus = standardProductSpuMapper.selectListByIds(standardProductSpuIds);
        Map<Long, StandardProductSpu> standardProductSpuMap = standardProductSpus.stream()
                .collect(Collectors.toMap(StandardProductSpu::getId, standardProductSpu -> standardProductSpu));

        // 获取用户
        Map<Long, String> userId2NameMap = Maps.newHashMap();
        List<AdminUserRespDTO> userList = FeginMethodExecuteUtils.execute(() -> adminUserApi.getUserList(userIds), true);
        if (CollectionUtil.isNotEmpty(userList)) {
            userId2NameMap = userList.stream().collect(Collectors.toMap(AdminUserRespDTO::getId, AdminUserRespDTO::getName));
        }

        // 获取渠道
        Map<Long, MarketingChannelRespDTO> channelId2ChannelMap = buildChannelId2ChannelMap(channelIds);

        // 最低日租金（租赁商品才有）
        Map<Long, IdAndPriceDTO> minDailyRentPriceMap = Maps.newHashMap();
        if (ProductTypeEnum.RENTAL_PRODUCT.getValue().equals(productType)) {
            List<IdAndPriceDTO> minDailyRentPrices = skuMapper.queryMinDailyRentPriceByMarketingProductSpuIds(marketingSpuIds);
            minDailyRentPriceMap = minDailyRentPrices.stream().collect(Collectors.toMap(IdAndPriceDTO::getId, idAndPriceDTO -> idAndPriceDTO));
        }

        // 获取商品对应的SKU数量
        List<IdAndCountDTO> skuCountLists = skuMapper.querySkuCountByMarketingProductSpuIds(marketingSpuIds);
        Map<Long, Long> skuCountMap = skuCountLists.stream().collect(Collectors.toMap(IdAndCountDTO::getId, IdAndCountDTO::getCount));

        // 获取商品对应的库存
        List<IdAndCountDTO> stockLists = skuMapper.queryStockByMarketingProductSpuIds(marketingSpuIds);
        Map<Long, Long> stockMap = stockLists.stream().collect(Collectors.toMap(IdAndCountDTO::getId, IdAndCountDTO::getCount));

        // 采购价
        List<IdAndPriceDTO> officialPriceLists = skuMapper.queryMinAndMaxOfficialPriceByMarketingProductSpuIds(marketingSpuIds);
        Map<Long, IdAndPriceDTO> officialPriceMap = officialPriceLists.stream().collect(Collectors.toMap(IdAndPriceDTO::getId, idAndPriceDTO -> idAndPriceDTO));

        // 建议售价
        List<IdAndPriceDTO> suggestedRetailPriceLists = skuMapper.queryMinAndMaxSuggestedRetailPriceByMarketingProductSpuIds(marketingSpuIds);
        Map<Long, IdAndPriceDTO> suggestedRetailPriceMap = suggestedRetailPriceLists.stream().collect(Collectors.toMap(IdAndPriceDTO::getId, idAndPriceDTO -> idAndPriceDTO));

        // 获取商品属性
        List<MarketingProductSpuProperty> marketingProductSpuProperties = spuPropertyMapper.selectListByMarketingProductSpuIds(marketingSpuIds);
        Map<Long, List<Long>> spuId2PropertyIdsMap = marketingProductSpuProperties.stream().collect(Collectors.groupingBy(MarketingProductSpuProperty::getMarketingSpuId,
                Collectors.mapping(MarketingProductSpuProperty::getProductPropertyId, Collectors.toList())));
        List<Long> propertyIds = marketingProductSpuProperties.stream().map(MarketingProductSpuProperty::getProductPropertyId).toList();
        List<ProductProperty> productProperties = productPropertyMapper.selectListByIds(propertyIds);
        Map<Long, String> propertyId2NameMap = productProperties.stream().collect(Collectors.toMap(ProductProperty::getId, ProductProperty::getName));

        // 获取商品属性值
        List<MarketingProductSpuPropertyValue> marketingProductSpuPropertyValues = spuPropertyValueMapper.selectListByMarketingProductSpuIds(marketingSpuIds);
        Map<Long, Map<Long, List<MarketingProductSpuPropertyValue>>> spuId2PropertyValueIdsMap = marketingProductSpuPropertyValues.stream().collect(
                Collectors.groupingBy(MarketingProductSpuPropertyValue::getMarketingSpuId,
                Collectors.groupingBy(MarketingProductSpuPropertyValue::getProductPropertyId)));
        List<Long> propertyValueIds = marketingProductSpuPropertyValues.stream().map(MarketingProductSpuPropertyValue::getProductPropertyValueId).toList();
        List<ProductPropertyValue> productPropertyValues = productPropertyValueMapper.selectListByIds(propertyValueIds);
        Map<Long, String> propertyValueId2ValueMap = productPropertyValues.stream().collect(Collectors.toMap(ProductPropertyValue::getId, ProductPropertyValue::getPropertyValue));

        // 获取租赁方式配置，独立于SPU/SKU属性，不参与SKU生成
        Map<Long, List<MarketingProductRentalMethodVO>> spuId2RentalMethodMap = buildRentalMethodVOMap(
                rentalMethodPropertyMapper.selectListByMarketingSpuIds(marketingSpuIds));

        List<MarketingProductRespVO> marketingProductRespVOS = Lists.newArrayList();
        for (MarketingProductSpu marketingProductSpu : marketingProductSpus) {
            MarketingProductRespVO marketingProductRespVO = new MarketingProductRespVO();
            marketingProductRespVOS.add(marketingProductRespVO);

            StandardProductSpu standardProductSpu = standardProductSpuMap.get(marketingProductSpu.getStandardProductSpuId());
            marketingProductRespVO.setStandardProductId(standardProductSpu.getId());
            marketingProductRespVO.setStandardProductCode(standardProductSpu.getCode());
            marketingProductRespVO.setStandardProductName(standardProductSpu.getName());

            marketingProductRespVO.setMarketingProductId(marketingProductSpu.getId());
            marketingProductRespVO.setMarketingProductCode(marketingProductSpu.getCode());
            marketingProductRespVO.setMarketingProductName(marketingProductSpu.getName());
            marketingProductRespVO.setMainPicUrls(List.of(marketingProductSpu.getMainPicUrls().split(",")));
            marketingProductRespVO.setRentalMethods(spuId2RentalMethodMap.getOrDefault(marketingProductSpu.getId(), Collections.emptyList()));

            marketingProductRespVO.setSkuCount(skuCountMap.getOrDefault(marketingProductSpu.getId(), 0L));
            marketingProductRespVO.setStock(stockMap.getOrDefault(marketingProductSpu.getId(), 0L));

            // 处理商品属性
            List<MarketingProductPropertyVO> marketingProductPropertyVOS = Lists.newArrayList();
            for (Long propertyId : spuId2PropertyIdsMap.getOrDefault(marketingProductSpu.getId(), Lists.newArrayList())) {
                MarketingProductPropertyVO marketingProductPropertyVO = new MarketingProductPropertyVO();
                marketingProductPropertyVOS.add(marketingProductPropertyVO);

                marketingProductPropertyVO.setPropertyId(propertyId);
                marketingProductPropertyVO.setPropertyName(propertyId2NameMap.get(propertyId));

                List<MarketingProductPropertyValueVO> marketingProductPropertyValueVOS = Lists.newArrayList();
                marketingProductPropertyVO.setPropertyValues(marketingProductPropertyValueVOS);
                for (MarketingProductSpuPropertyValue spuPropertyValue : spuId2PropertyValueIdsMap.getOrDefault(marketingProductSpu.getId(), new HashMap<>()).get(propertyId)) {
                    MarketingProductPropertyValueVO marketingProductPropertyValueVO = new MarketingProductPropertyValueVO();
                    marketingProductPropertyValueVOS.add(marketingProductPropertyValueVO);

                    if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                        marketingProductPropertyValueVO.setProductPropertyValueId(spuPropertyValue.getProductPropertyValueId());
                        marketingProductPropertyValueVO.setValue(propertyValueId2ValueMap.get(spuPropertyValue.getProductPropertyValueId()));
                    } else {
                        marketingProductPropertyValueVO.setValue(spuPropertyValue.getPropertyValue());
                    }
                }
            }
            marketingProductRespVO.setProperties(marketingProductPropertyVOS);

            if (StringUtils.isNotBlank(marketingProductSpu.getShelvingChannelId())) {
                List<String> channelNames = Lists.newArrayList();
                long[] shelvingChannelIds = StrUtil.splitToLong(marketingProductSpu.getShelvingChannelId(), ",");
                for (long shelvingChannelId : shelvingChannelIds) {
                    MarketingChannelRespDTO marketingChannelRespDTO = channelId2ChannelMap.get(shelvingChannelId);
                    if (marketingChannelRespDTO != null) {
                        channelNames.add(marketingChannelRespDTO.getChannelName());
                    }
                }
                marketingProductRespVO.setChannelNames(channelNames);
            }

            // 最低日租金
            marketingProductRespVO.setMinDailyRentPrice(minDailyRentPriceMap.getOrDefault(marketingProductSpu.getId(), new IdAndPriceDTO()).getMinPrice());


            marketingProductRespVO.setApproveStatus(marketingProductSpu.getApprovalStatus());
            marketingProductRespVO.setShelvesStatus(marketingProductSpu.getShelvesStatus());
            marketingProductRespVO.setIsDraft(marketingProductSpu.getIsDraft());

            marketingProductRespVO.setMinBuybackPrice(marketingProductSpu.getMinBuybackPrice());
            marketingProductRespVO.setMaxBuybackPrice(marketingProductSpu.getMaxBuybackPrice());

            marketingProductRespVO.setMinOfficialPrice(officialPriceMap.getOrDefault(marketingProductSpu.getId(), new IdAndPriceDTO()).getMinPrice());
            marketingProductRespVO.setMaxOfficialPrice(officialPriceMap.getOrDefault(marketingProductSpu.getId(), new IdAndPriceDTO()).getMaxPrice());

            marketingProductRespVO.setMinSuggestedRetailPrice(suggestedRetailPriceMap.getOrDefault(marketingProductSpu.getId(), new IdAndPriceDTO()).getMinPrice());
            marketingProductRespVO.setMaxSuggestedRetailPrice(suggestedRetailPriceMap.getOrDefault(marketingProductSpu.getId(), new IdAndPriceDTO()).getMaxPrice());

            marketingProductRespVO.setCreatorName(userId2NameMap.get(marketingProductSpu.getCreateBy()));
            marketingProductRespVO.setCreateTime(marketingProductSpu.getCreateTime());
            marketingProductRespVO.setUpdaterName(userId2NameMap.get(marketingProductSpu.getUpdateBy()));
            marketingProductRespVO.setUpdateTime(marketingProductSpu.getUpdateTime());
        }

        return marketingProductRespVOS;
    }


    private void saveRentalMethods(Long marketingSpuId, Integer productType, List<MarketingProductRentalMethodVO> rentalMethods,
                                   LoginUser<?> loginUser, Date now) {
        List<MarketingProductSpuRentalMethodProperty> rentalMethodProperties = buildRentalMethodProperties(
                marketingSpuId, productType, rentalMethods, loginUser, now);
        if (CollectionUtil.isNotEmpty(rentalMethodProperties)) {
            rentalMethodPropertyMapper.insertBatch(rentalMethodProperties);
        }
    }

    private void saveSkuRentalMethodProperties(Long marketingSpuId, List<MarketingProductSkuVO> skuVOS,
                                               List<MarketingProductSku> savedSkus, LoginUser<?> loginUser, Date now) {
        List<MarketingProductSkuRentalMethodProperty> skuRentalMethodProperties = buildSkuRentalMethodProperties(
                marketingSpuId, skuVOS, savedSkus, loginUser, now);
        if (CollectionUtil.isNotEmpty(skuRentalMethodProperties)) {
            skuRentalMethodPropertyMapper.insertBatch(skuRentalMethodProperties);
        }
    }

    private List<MarketingProductSkuRentalMethodProperty> buildSkuRentalMethodProperties(Long marketingSpuId,
                                                                                         List<MarketingProductSkuVO> skuVOS,
                                                                                         List<MarketingProductSku> savedSkus,
                                                                                         LoginUser<?> loginUser,
                                                                                         Date now) {
        if (CollectionUtil.isEmpty(skuVOS) || CollectionUtil.isEmpty(savedSkus)) {
            return Collections.emptyList();
        }
        Map<String, Long> skuCode2IdMap = savedSkus.stream()
                .filter(sku -> StringUtils.isNotBlank(sku.getSkuCode()))
                .collect(Collectors.toMap(MarketingProductSku::getSkuCode, MarketingProductSku::getId, (first, duplicate) -> first));
        Set<Long> savedSkuIds = savedSkus.stream().map(MarketingProductSku::getId).collect(Collectors.toSet());

        List<MarketingProductSkuRentalMethodProperty> result = Lists.newArrayList();
        for (MarketingProductSkuVO skuVO : skuVOS) {
            if (skuVO == null || CollectionUtil.isEmpty(skuVO.getRentalMethodProperties())) {
                continue;
            }
            Long marketingSkuId = skuVO.getId();
            if (marketingSkuId == null && StringUtils.isNotBlank(skuVO.getSkuCode())) {
                marketingSkuId = skuCode2IdMap.get(skuVO.getSkuCode());
            }
            if (marketingSkuId == null || !savedSkuIds.contains(marketingSkuId)) {
                continue;
            }
            for (MarketingProductSkuRentalMethodPropertyVO propertyVO : skuVO.getRentalMethodProperties()) {
                if (propertyVO == null) {
                    continue;
                }
                MarketingProductSkuRentalMethodProperty property = new MarketingProductSkuRentalMethodProperty();
                result.add(property);
                property.setMarketingSpuId(marketingSpuId);
                property.setMarketingSkuId(marketingSkuId);
                property.setRentalMethod(propertyVO.getRentalMethod());
                property.setRentalPeriodMonth(propertyVO.getRentalPeriodMonth());
                property.setTotalRent(propertyVO.getTotalRent());
                property.setMonthlyRent(propertyVO.getMonthlyRent());
                property.setDailyRent(propertyVO.getDailyRent());
                property.setBuyoutAmount(propertyVO.getBuyoutAmount());
                property.setPremium(propertyVO.getPremium());
                property.setStock(propertyVO.getStock());
                property.setPartnerId(loginUser.getPartnerId());
                property.setCreateBy(loginUser.getId());
                property.setUpdateBy(loginUser.getId());
                property.setCreateTime(now);
                property.setUpdateTime(now);
                property.setIsDeleted(NumberUtils.INTEGER_ZERO);
            }
        }
        return result;
    }

    private List<MarketingProductSkuRentalMethodPropertyVO> convertSkuRentalMethodPropertyVOs(
            List<MarketingProductSkuRentalMethodProperty> properties) {
        if (CollectionUtil.isEmpty(properties)) {
            return Collections.emptyList();
        }
        return properties.stream().map(property -> {
            MarketingProductSkuRentalMethodPropertyVO vo = new MarketingProductSkuRentalMethodPropertyVO();
            vo.setRentalMethod(property.getRentalMethod());
            RentalMethodEnum rentalMethodEnum = RentalMethodEnum.get(property.getRentalMethod());
            vo.setRentalMethodName(rentalMethodEnum == null ? null : rentalMethodEnum.getDesc());
            vo.setRentalPeriodMonth(property.getRentalPeriodMonth());
            vo.setTotalRent(property.getTotalRent());
            vo.setMonthlyRent(property.getMonthlyRent());
            vo.setDailyRent(property.getDailyRent());
            vo.setBuyoutAmount(property.getBuyoutAmount());
            vo.setPremium(property.getPremium());
            vo.setStock(property.getStock());
            return vo;
        }).toList();
    }

    private List<MarketingProductSpuRentalMethodProperty> buildRentalMethodProperties(Long marketingSpuId, Integer productType,
                                                                                       List<MarketingProductRentalMethodVO> rentalMethods,
                                                                                       LoginUser<?> loginUser, Date now) {
        if (!ProductTypeEnum.RENTAL_PRODUCT.getValue().equals(productType) || CollectionUtil.isEmpty(rentalMethods)) {
            return Collections.emptyList();
        }

        Set<String> methodPeriodKeys = Sets.newHashSet();
        List<MarketingProductSpuRentalMethodProperty> rentalMethodProperties = Lists.newArrayList();
        for (MarketingProductRentalMethodVO rentalMethodVO : rentalMethods) {
            if (rentalMethodVO == null || RentalMethodEnum.get(rentalMethodVO.getRentalMethod()) == null) {
                throw exception(RENTAL_METHOD_INVALID);
            }
            if (CollectionUtil.isEmpty(rentalMethodVO.getRentalPeriods())) {
                throw exception(RENTAL_METHOD_PERIOD_REQUIRED);
            }
            for (Integer rentalPeriod : rentalMethodVO.getRentalPeriods()) {
                if (!SUPPORTED_RENTAL_PERIOD_MONTHS.contains(rentalPeriod)) {
                    throw exception(RENTAL_PERIOD_INVALID);
                }
                String methodPeriodKey = rentalMethodVO.getRentalMethod() + "_" + rentalPeriod;
                if (!methodPeriodKeys.add(methodPeriodKey)) {
                    continue;
                }

                MarketingProductSpuRentalMethodProperty rentalMethodProperty = new MarketingProductSpuRentalMethodProperty();
                rentalMethodProperties.add(rentalMethodProperty);
                rentalMethodProperty.setMarketingSpuId(marketingSpuId);
                rentalMethodProperty.setRentalMethod(rentalMethodVO.getRentalMethod());
                rentalMethodProperty.setRentalPeriodMonth(rentalPeriod);
                rentalMethodProperty.setPartnerId(loginUser.getPartnerId());
                rentalMethodProperty.setCreateBy(loginUser.getId());
                rentalMethodProperty.setUpdateBy(loginUser.getId());
                rentalMethodProperty.setCreateTime(now);
                rentalMethodProperty.setUpdateTime(now);
                rentalMethodProperty.setIsDeleted(NumberUtils.INTEGER_ZERO);
            }
        }
        return rentalMethodProperties;
    }

    private Map<Long, List<MarketingProductRentalMethodVO>> buildRentalMethodVOMap(List<MarketingProductSpuRentalMethodProperty> rentalMethodProperties) {
        if (CollectionUtil.isEmpty(rentalMethodProperties)) {
            return Collections.emptyMap();
        }
        Map<Long, List<MarketingProductSpuRentalMethodProperty>> spuId2RentalMethods = rentalMethodProperties.stream()
                .collect(Collectors.groupingBy(MarketingProductSpuRentalMethodProperty::getMarketingSpuId));
        Map<Long, List<MarketingProductRentalMethodVO>> result = Maps.newHashMap();
        for (Map.Entry<Long, List<MarketingProductSpuRentalMethodProperty>> entry : spuId2RentalMethods.entrySet()) {
            result.put(entry.getKey(), buildRentalMethodVOs(entry.getValue()));
        }
        return result;
    }

    private List<MarketingProductRentalMethodVO> buildRentalMethodVOs(List<MarketingProductSpuRentalMethodProperty> rentalMethodProperties) {
        if (CollectionUtil.isEmpty(rentalMethodProperties)) {
            return Collections.emptyList();
        }
        Map<Integer, List<MarketingProductSpuRentalMethodProperty>> method2Properties = rentalMethodProperties.stream()
                .collect(Collectors.groupingBy(MarketingProductSpuRentalMethodProperty::getRentalMethod));
        return method2Properties.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    MarketingProductRentalMethodVO rentalMethodVO = new MarketingProductRentalMethodVO();
                    rentalMethodVO.setRentalMethod(entry.getKey());
                    RentalMethodEnum rentalMethodEnum = RentalMethodEnum.get(entry.getKey());
                    rentalMethodVO.setRentalMethodName(rentalMethodEnum == null ? null : rentalMethodEnum.getDesc());
                    rentalMethodVO.setRentalPeriods(entry.getValue().stream()
                            .map(MarketingProductSpuRentalMethodProperty::getRentalPeriodMonth)
                            .filter(Objects::nonNull)
                            .distinct()
                            .sorted()
                            .toList());
                    return rentalMethodVO;
                }).toList();
    }

    private Map<Long, List<MarketingProductRentalMethodDto>> buildRentalMethodDtoMap(List<MarketingProductSpuRentalMethodProperty> rentalMethodProperties) {
        Map<Long, List<MarketingProductRentalMethodVO>> voMap = buildRentalMethodVOMap(rentalMethodProperties);
        if (MapUtil.isEmpty(voMap)) {
            return Collections.emptyMap();
        }
        Map<Long, List<MarketingProductRentalMethodDto>> result = Maps.newHashMap();
        for (Map.Entry<Long, List<MarketingProductRentalMethodVO>> entry : voMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().map(this::convertRentalMethodDto).toList());
        }
        return result;
    }

    private MarketingProductRentalMethodDto convertRentalMethodDto(MarketingProductRentalMethodVO rentalMethodVO) {
        MarketingProductRentalMethodDto rentalMethodDto = new MarketingProductRentalMethodDto();
        rentalMethodDto.setRentalMethod(rentalMethodVO.getRentalMethod());
        rentalMethodDto.setRentalMethodName(rentalMethodVO.getRentalMethodName());
        rentalMethodDto.setRentalPeriods(rentalMethodVO.getRentalPeriods());
        return rentalMethodDto;
    }

    private MarketingProductSpu buildMarketingProductSpu(MarketingProductAddReqVO reqVO, LoginUser<?> loginUser, Date now) {
        MarketingProductSpu marketingProductSpu = new MarketingProductSpu();
        marketingProductSpu.setStandardProductSpuId(reqVO.getStandardProductSpuId());
        marketingProductSpu.setType(reqVO.getType());
        marketingProductSpu.setName(reqVO.getName());
        marketingProductSpu.setProductCondition(reqVO.getProductCondition());
        marketingProductSpu.setMonitorAttribute(reqVO.getMonitorAttribute());
        marketingProductSpu.setMinBuybackPrice(reqVO.getMinBuybackPrice());
        marketingProductSpu.setMaxBuybackPrice(reqVO.getMaxBuybackPrice());

        // 根据商品类型，生成商品编码
        ProductTypeEnum productTypeEnum = ProductTypeEnum.get(reqVO.getType());
        String code = switch (productTypeEnum) {
            case RENTAL_PRODUCT ->
                    "ZSPU" + DateUtil.format(new Date(), "yyMMdd") + sequenceGenerator.getRentalProductSequence();
            case PRODUCT_FOR_SALE ->
                    "MSPU" + DateUtil.format(new Date(), "yyMMdd") + sequenceGenerator.getSaleProductSequence();
            case RECYCLED_PRODUCT ->
                    "HSPU" + DateUtil.format(new Date(), "yyMMdd") + sequenceGenerator.getRecycleProductSequence();
            case PHYSICAL_PRODUCT ->
                    "SSPU" + DateUtil.format(new Date(), "yyMMdd") + sequenceGenerator.getPhysicalProductSequence();
            case VIRTUAL_PRODUCT ->
                    "XSPU" + DateUtil.format(new Date(), "yyMMdd") + sequenceGenerator.getVirtualProductSequence();
        };
        marketingProductSpu.setCode(code);

        // 图文信息
        marketingProductSpu.setMainPicUrls(String.join(",", reqVO.getMainPicUrls()));
        marketingProductSpu.setCarouselPicUrls(String.join(",", reqVO.getCarouselPicUrls()));
        marketingProductSpu.setVideoUrls(String.join(",", reqVO.getVideoUrls()));
        marketingProductSpu.setDetailPicUrls(String.join(",", reqVO.getDetailPicUrls()));
        marketingProductSpu.setDetailTagIds(joinLongList(reqVO.getDetailTagIds()));
        marketingProductSpu.setSkuTagIds(joinLongList(reqVO.getSkuTagIds()));

        // 增值服务
        marketingProductSpu.setValueAddedIds(joinLongList(reqVO.getValueAddedIds()));
        marketingProductSpu.setShowPage(joinIntegerList(reqVO.getShowPages()));
        marketingProductSpu.setIsDefaultSelected(reqVO.getIsDefaultSelected());
        marketingProductSpu.setDefaultSelectedValueAddedId(reqVO.getDefaultSelectedValueAddedId());

        // 订单服务信息
        marketingProductSpu.setCompensationRuleId(reqVO.getCompensationRuleId());
        marketingProductSpu.setShippingWay(reqVO.getShippingWay());
        marketingProductSpu.setShippingTemplateId(reqVO.getShippingTemplateId());
        marketingProductSpu.setShippingAreaCodes(String.join(",", reqVO.getShippingAreaCodes()));

        // 商品上架信息
        marketingProductSpu.setShelvingWay(reqVO.getShelvingWay());
        if (ShelvingWayEnum.APPOINT_SHELVES.getValue().equals(reqVO.getShelvingWay())) {
            marketingProductSpu.setShelvingTime(reqVO.getShelvingTime());
        }
        marketingProductSpu.setShelvingChannelId(joinLongList(reqVO.getShelvingChannelIds()));

        // 通用字段
        marketingProductSpu.setPartnerId(loginUser.getPartnerId());
        marketingProductSpu.setCreateTime(now);
        marketingProductSpu.setUpdateTime(now);
        marketingProductSpu.setCreateBy(loginUser.getId());
        marketingProductSpu.setUpdateBy(loginUser.getId());
        marketingProductSpu.setIsDeleted(NumberUtils.INTEGER_ZERO);

        // 草稿状态
        if (OperateTypeEnum.SUBMIT.getType().equals(reqVO.getOperateType())) {
            marketingProductSpu.setIsDraft(NumberUtils.INTEGER_ZERO);
        } else {
            marketingProductSpu.setIsDraft(NumberUtils.INTEGER_ONE);
        }

        // 审批、上下架状态
        marketingProductSpu.setApprovalStatus(ApproveStatusEnum.WAIT_APPROVE.getValue());
        marketingProductSpu.setShelvesStatus(ShelvesStatusEnum.WAIT_SHELVES.getValue());

        // 回收商品的收货地址
        marketingProductSpu.setReceivingAddress(reqVO.getReceivingAddress());

        return marketingProductSpu;
    }

    private List<MarketingProductSpuProperty> buildMarketingProductSpuProperty(List<MarketingProductPropertyVO> spuProperties,
                                                                               LoginUser<?> loginUser, Long marketingSpuId, Date now) {
        List<MarketingProductSpuProperty> spuPropertyList = Lists.newArrayList();
        for (MarketingProductPropertyVO spuPropertyVO : spuProperties) {
            MarketingProductSpuProperty spuProperty = new MarketingProductSpuProperty();
            spuPropertyList.add(spuProperty);
            spuProperty.setMarketingSpuId(marketingSpuId);
            spuProperty.setProductPropertyId(spuPropertyVO.getPropertyId());
            spuProperty.setSort(spuPropertyVO.getSort());
            spuProperty.setIsAddPropertyPic(defaultSwitch(spuPropertyVO.getIsAddPropertyPic()));
            spuProperty.setIsAddMarketingCorner(defaultSwitch(spuPropertyVO.getIsAddMarketingCorner()));
            spuProperty.setIsSkuProperty(defaultSwitch(spuPropertyVO.getIsSkuProperty()));
            spuProperty.setPartnerId(loginUser.getPartnerId());
            spuProperty.setCreateBy(loginUser.getId());
            spuProperty.setUpdateBy(loginUser.getId());
            spuProperty.setCreateTime(now);
            spuProperty.setUpdateTime(now);
            spuProperty.setIsDeleted(NumberUtils.INTEGER_ZERO);
        }
        return spuPropertyList;
    }

    private List<MarketingProductSpuPropertyValue> buildMarketingProductSpuPropertyValues(List<MarketingProductPropertyVO> spuProperties,
                                                                                          LoginUser<?> loginUser, Map<Long, Long> spuPropertyIdMap,
                                                                                          Long marketingSpuId, Date now) {
        List<MarketingProductSpuPropertyValue> spuPropertyValueList = Lists.newArrayList();
        for (MarketingProductPropertyVO spuPropertyVO : spuProperties) {

            for (MarketingProductPropertyValueVO spuPropertyValueVO : spuPropertyVO.getPropertyValues()) {
                MarketingProductSpuPropertyValue spuPropertyValue = new MarketingProductSpuPropertyValue();
                spuPropertyValueList.add(spuPropertyValue);
                spuPropertyValue.setProductPropertyId(spuPropertyVO.getPropertyId());
                spuPropertyValue.setMarketingSpuId(marketingSpuId);
                spuPropertyValue.setSpuPropertyId(spuPropertyIdMap.get(spuPropertyVO.getPropertyId()));
                spuPropertyValue.setProductPropertyValueId(spuPropertyValueVO.getProductPropertyValueId());
                spuPropertyValue.setPropertyValue(spuPropertyValueVO.getValue());
                spuPropertyValue.setPicUrl(normalizePicUrl(spuPropertyValueVO.getPicUrl(), spuPropertyVO.getIsAddPropertyPic()));
                spuPropertyValue.setMarketingCornerText(normalizeMarketingCornerText(spuPropertyValueVO.getMarketingCornerText(),
                        spuPropertyVO.getIsAddMarketingCorner()));
                spuPropertyValue.setSort(spuPropertyValueVO.getSort());
                spuPropertyValue.setPartnerId(loginUser.getPartnerId());
                spuPropertyValue.setCreateBy(loginUser.getId());
                spuPropertyValue.setUpdateBy(loginUser.getId());
                spuPropertyValue.setCreateTime(now);
                spuPropertyValue.setUpdateTime(now);
                spuPropertyValue.setIsDeleted(NumberUtils.INTEGER_ZERO);
            }

        }
        return spuPropertyValueList;
    }

    private Integer defaultSwitch(Integer switchValue) {
        return NumberUtils.INTEGER_ONE.equals(switchValue) ? NumberUtils.INTEGER_ONE : NumberUtils.INTEGER_ZERO;
    }

    private String normalizePicUrl(String picUrl, Integer isAddPropertyPic) {
        if (!NumberUtils.INTEGER_ONE.equals(defaultSwitch(isAddPropertyPic))) {
            return StringUtils.EMPTY;
        }
        return picUrl;
    }

    private String normalizeMarketingCornerText(String marketingCornerText, Integer isAddMarketingCorner) {
        if (!NumberUtils.INTEGER_ONE.equals(defaultSwitch(isAddMarketingCorner))) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isBlank(marketingCornerText)) {
            throw exception(MARKETING_CORNER_TEXT_REQUIRED);
        }
        if (StringUtils.length(marketingCornerText) > MARKETING_CORNER_TEXT_MAX_LENGTH) {
            throw exception(MARKETING_CORNER_TEXT_TOO_LONG);
        }
        return marketingCornerText;
    }

    private List<MarketingProductSku> buildMarketingProductSkus(List<MarketingProductSkuVO> skus, LoginUser<?> loginUser,
                                                                Long marketingProductSpuId, Date now) {
        List<MarketingProductSku> skuList = Lists.newArrayList();
        for (MarketingProductSkuVO skuVO : skus) {
            MarketingProductSku marketingProductSku = new MarketingProductSku();
            skuList.add(marketingProductSku);
            String skuCode = "SKU" + DateUtil.format(new Date(), "yyMMdd") + sequenceGenerator.getSkuSequence();
            skuVO.setSkuCode(skuCode);
            marketingProductSku.setSkuCode(skuCode);
            marketingProductSku.setMarketingSpuId(marketingProductSpuId);
            marketingProductSku.setOfficialPrice(skuVO.getOfficialPrice());
            marketingProductSku.setTotalPriceFactor(skuVO.getTotalPriceFactor());
            marketingProductSku.setTotalRentFactor(skuVO.getTotalRentFactor());
            marketingProductSku.setTotalPrice(skuVO.getTotalPrice());
            marketingProductSku.setTotalRent(skuVO.getTotalRent());
            marketingProductSku.setBuyoutAmount(skuVO.getBuyoutAmount());
            marketingProductSku.setDailyRent(skuVO.getDailyRent());
            marketingProductSku.setStock(skuVO.getStock());
            marketingProductSku.setPremium(skuVO.getPremium());
            marketingProductSku.setSuggestedRetailPrice(skuVO.getSuggestedRetailPrice());
            marketingProductSku.setStrikethroughPrice(skuVO.getStrikethroughPrice());
            marketingProductSku.setCashUsageRatio(skuVO.getCashUsageRatio());
            marketingProductSku.setPointsUsageRatio(skuVO.getPointsUsageRatio());
            marketingProductSku.setPointsCount(skuVO.getPointsCount());
            marketingProductSku.setCashPrice(skuVO.getCashPrice());
            marketingProductSku.setIsAllowOrder(NumberUtils.INTEGER_ONE);
            marketingProductSku.setPartnerId(loginUser.getPartnerId());
            marketingProductSku.setCreateBy(loginUser.getId());
            marketingProductSku.setUpdateBy(loginUser.getId());
            marketingProductSku.setCreateTime(now);
            marketingProductSku.setUpdateTime(now);
            marketingProductSku.setIsDeleted(NumberUtils.INTEGER_ZERO);
        }
        return skuList;
    }

    private List<MarketingProductSkuPropertyValue> buildMarketingProductSkuPropertyValues(List<MarketingProductSkuVO> skus,
        Map<String, Long> skuCode2IdMap, List<MarketingProductSpuPropertyValue> spuPropertyValues, LoginUser<?> loginUser,
        Long marketingProductSpuId, Date now) {

        Map<String, Long> unqKey2IdMap = Maps.newHashMap();
        for (MarketingProductSpuPropertyValue spuPropertyValue : spuPropertyValues) {
            String unqKey = spuPropertyValue.getProductPropertyId() + "_" + spuPropertyValue.getProductPropertyValueId() + "_" + spuPropertyValue.getPropertyValue();
            unqKey2IdMap.put(unqKey, spuPropertyValue.getId());
        }

        List<MarketingProductSkuPropertyValue> skuPropertyValueList = Lists.newArrayList();
        for (MarketingProductSkuVO skuVO : skus) {
            long skuId = skuCode2IdMap.get(skuVO.getSkuCode());
            for (SkuPropertyValueVO skuPropertyValueVO : skuVO.getPropertyValues()) {
                MarketingProductSkuPropertyValue skuPropertyValue = new MarketingProductSkuPropertyValue();
                skuPropertyValueList.add(skuPropertyValue);

                skuPropertyValue.setMarketingSpuId(marketingProductSpuId);
                skuPropertyValue.setMarketingProductSkuId(skuId);
                String unqKey = skuPropertyValueVO.getPropertyId() + "_" + skuPropertyValueVO.getPropertyValueId() + "_" + skuPropertyValueVO.getPropertyValue();
                skuPropertyValue.setMarketingSpuPropertyValueId(unqKey2IdMap.get(unqKey));
                skuPropertyValue.setPartnerId(loginUser.getPartnerId());
                skuPropertyValue.setCreateBy(loginUser.getId());
                skuPropertyValue.setUpdateBy(loginUser.getId());
                skuPropertyValue.setCreateTime(now);
                skuPropertyValue.setUpdateTime(now);
                skuPropertyValue.setIsDeleted(NumberUtils.INTEGER_ZERO);
            }
        }

        return skuPropertyValueList;
    }

    private List<MarketingProductPropertyVO> filterSkuProperties(List<MarketingProductPropertyVO> spuProperties) {
        return Optional.ofNullable(spuProperties).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .filter(property -> NumberUtils.INTEGER_ONE.equals(defaultSwitch(property.getIsSkuProperty())))
                .toList();
    }

    private List<MarketingProductSkuVO> buildSkuCombinationsBySkuProperties(List<MarketingProductPropertyVO> skuProperties,
                                                                            List<MarketingProductSkuVO> incomingSkus) {
        if (CollUtil.isEmpty(skuProperties)) {
            throw exception(MARKETING_SKU_PROPERTY_REQUIRED);
        }
        List<List<SkuPropertyValueVO>> expectedCombinations = buildExpectedSkuCombinations(skuProperties);
        Set<Long> skuPropertyIds = Optional.ofNullable(skuProperties).orElse(Collections.emptyList())
                .stream().map(MarketingProductPropertyVO::getPropertyId).collect(Collectors.toSet());
        Map<String, MarketingProductSkuVO> normalizedIncomingSkuMap = Optional.ofNullable(incomingSkus).orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(skuVO -> buildSkuCombinationKeyFromSkuVO(skuVO, skuPropertyIds), Function.identity(), (first, duplicate) -> {
                    log.warn("duplicate incoming marketing sku combination detected, keep first. key:{}", buildSkuCombinationKeyFromSkuVO(first, skuPropertyIds));
                    return first;
                }));

        Set<String> expectedKeySet = expectedCombinations.stream().map(this::buildSkuCombinationKey).collect(Collectors.toSet());
        Set<String> actualKeySet = normalizedIncomingSkuMap.keySet();
        if (!expectedKeySet.containsAll(actualKeySet)) {
            log.warn("incoming marketing skus exceed generated sku combinations by isSkuProperty. expected:{}, actual:{}",
                    expectedKeySet, actualKeySet);
            throw new IllegalArgumentException("营销SKU与SKU属性组合不一致，请按SKU属性组合重新提交");
        }

        List<MarketingProductSkuVO> result = Lists.newArrayList();
        for (List<SkuPropertyValueVO> combination : expectedCombinations) {
            String key = buildSkuCombinationKey(combination);
            MarketingProductSkuVO targetSku = normalizedIncomingSkuMap.get(key);
            if (targetSku != null) {
                targetSku.setPropertyValues(combination);
                result.add(targetSku);
            }
        }
        return result;
    }

    private List<List<SkuPropertyValueVO>> buildExpectedSkuCombinations(List<MarketingProductPropertyVO> skuProperties) {
        List<List<SkuPropertyValueVO>> combinations = Lists.newArrayList();
        combinations.add(Lists.newArrayList());
        for (MarketingProductPropertyVO property : Optional.ofNullable(skuProperties).orElse(Collections.emptyList())) {
            List<SkuPropertyValueVO> values = Optional.ofNullable(property.getPropertyValues()).orElse(Collections.emptyList()).stream()
                    .filter(Objects::nonNull)
                    .map(propertyValue -> {
                        SkuPropertyValueVO skuPropertyValueVO = new SkuPropertyValueVO();
                        skuPropertyValueVO.setPropertyId(property.getPropertyId());
                        skuPropertyValueVO.setPropertyValueId(propertyValue.getProductPropertyValueId());
                        skuPropertyValueVO.setPropertyValue(propertyValue.getValue());
                        return skuPropertyValueVO;
                    }).toList();
            List<List<SkuPropertyValueVO>> newCombinations = Lists.newArrayList();
            for (List<SkuPropertyValueVO> combination : combinations) {
                for (SkuPropertyValueVO value : values) {
                    List<SkuPropertyValueVO> newCombination = Lists.newArrayList(combination);
                    newCombination.add(value);
                    newCombinations.add(newCombination);
                }
            }
            combinations = newCombinations;
        }
        return combinations;
    }

    private String buildSkuCombinationKeyFromSkuVO(MarketingProductSkuVO skuVO, Set<Long> skuPropertyIds) {
        List<SkuPropertyValueVO> filteredPropertyValues = Optional.ofNullable(skuVO.getPropertyValues()).orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(propertyValue -> skuPropertyIds.contains(propertyValue.getPropertyId()))
                .toList();
        return buildSkuCombinationKey(filteredPropertyValues);
    }

    private String buildSkuCombinationKey(List<SkuPropertyValueVO> propertyValues) {
        return propertyValues.stream()
                .sorted(Comparator.comparing(SkuPropertyValueVO::getPropertyId)
                        .thenComparing(SkuPropertyValueVO::getPropertyValueId, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(SkuPropertyValueVO::getPropertyValue, Comparator.nullsLast(String::compareTo)))
                .map(propertyValue -> propertyValue.getPropertyId() + "_" + propertyValue.getPropertyValueId() + "_" + propertyValue.getPropertyValue())
                .collect(Collectors.joining("|"));
    }

    private void validateSpuPropertiesInStandardScope(Long standardProductSpuId, List<MarketingProductPropertyVO> incomingSpuProperties) {
        if (CollectionUtil.isEmpty(incomingSpuProperties)) {
            return;
        }
        List<MarketingAvailablePropertyVO> availableProperties = queryAvailablePropertiesByStandardSpuId(standardProductSpuId);
        if (CollectionUtil.isEmpty(availableProperties)) {
            throw exception(STANDARD_PRODUCT_PROPERTY_SCOPE_EMPTY);
        }
        Map<Long, MarketingAvailablePropertyVO> propertyId2ScopeMap = availableProperties.stream()
                .collect(Collectors.toMap(MarketingAvailablePropertyVO::getPropertyId, Function.identity(), (a, b) -> a));
        for (MarketingProductPropertyVO incomingProperty : incomingSpuProperties.stream().filter(Objects::nonNull).toList()) {
            MarketingAvailablePropertyVO propertyScope = propertyId2ScopeMap.get(incomingProperty.getPropertyId());
            if (propertyScope == null) {
                throw exception(MARKETING_PROPERTY_OUT_OF_STANDARD_SCOPE);
            }
            Set<Long> valueIds = propertyScope.getPropertyValues().stream()
                    .map(MarketingAvailablePropertyValueVO::getPropertyValueId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            Set<String> values = propertyScope.getPropertyValues().stream()
                    .map(MarketingAvailablePropertyValueVO::getPropertyValue)
                    .filter(StrUtil::isNotBlank).collect(Collectors.toSet());
            List<MarketingProductPropertyValueVO> incomingValues = Optional.ofNullable(incomingProperty.getPropertyValues()).orElse(Collections.emptyList());
            for (MarketingProductPropertyValueVO incomingValue : incomingValues.stream().filter(Objects::nonNull).toList()) {
                if (incomingValue.getProductPropertyValueId() != null) {
                    if (!valueIds.contains(incomingValue.getProductPropertyValueId())) {
                        throw exception(MARKETING_PROPERTY_VALUE_OUT_OF_STANDARD_SCOPE);
                    }
                    continue;
                }
                if (StrUtil.isBlank(incomingValue.getValue()) || !values.contains(incomingValue.getValue())) {
                    throw exception(MARKETING_PROPERTY_VALUE_OUT_OF_STANDARD_SCOPE);
                }
            }
        }
    }

    private List<MarketingAvailablePropertyVO> queryAvailablePropertiesByStandardSpuId(Long standardProductSpuId) {
        List<StandardProductSpuProperty> scopeProperties = standardProductSpuPropertyMapper.selectListByStandardSpuId(standardProductSpuId);
        if (CollectionUtil.isEmpty(scopeProperties)) {
            return Collections.emptyList();
        }
        Map<Long, Long> propertyId2SpuPropertyIdMap = scopeProperties.stream().collect(Collectors.toMap(StandardProductSpuProperty::getProductPropertyId,
                StandardProductSpuProperty::getId, (a, b) -> a));
        List<StandardProductSpuPropertyValue> scopeValues = standardProductSpuPropertyValueMapper.selectListByStandardSpuId(standardProductSpuId);
        Map<Long, List<StandardProductSpuPropertyValue>> spuPropertyId2ValuesMap = scopeValues.stream()
                .collect(Collectors.groupingBy(StandardProductSpuPropertyValue::getSpuPropertyId));
        List<Long> propertyIds = scopeProperties.stream().map(StandardProductSpuProperty::getProductPropertyId).distinct().toList();
        Map<Long, String> propertyId2NameMap = productPropertyMapper.selectListByIds(propertyIds).stream()
                .collect(Collectors.toMap(ProductProperty::getId, ProductProperty::getName, (a, b) -> a));
        List<MarketingAvailablePropertyVO> result = Lists.newArrayList();
        for (StandardProductSpuProperty scopeProperty : scopeProperties) {
            MarketingAvailablePropertyVO propertyVO = new MarketingAvailablePropertyVO();
            propertyVO.setPropertyId(scopeProperty.getProductPropertyId());
            propertyVO.setPropertyName(propertyId2NameMap.get(scopeProperty.getProductPropertyId()));
            propertyVO.setIsSkuProperty(scopeProperty.getIsSkuProperty());
            propertyVO.setIsAddPropertyPic(scopeProperty.getIsAddPropertyPic());
            propertyVO.setIsAddMarketingCorner(scopeProperty.getIsAddMarketingCorner());
            List<MarketingAvailablePropertyValueVO> valueVOS = Lists.newArrayList();
            for (StandardProductSpuPropertyValue scopeValue : spuPropertyId2ValuesMap.getOrDefault(propertyId2SpuPropertyIdMap.get(scopeProperty.getProductPropertyId()), Collections.emptyList())) {
                MarketingAvailablePropertyValueVO valueVO = new MarketingAvailablePropertyValueVO();
                valueVO.setPropertyValueId(scopeValue.getProductPropertyValueId());
                valueVO.setPropertyValue(scopeValue.getPropertyValue());
                valueVO.setPicUrl(scopeValue.getPicUrl());
                valueVO.setMarketingCornerText(scopeValue.getMarketingCornerText());
                valueVOS.add(valueVO);
            }
            propertyVO.setPropertyValues(valueVOS.stream().collect(Collectors.collectingAndThen(
                    Collectors.toMap(v -> String.valueOf(v.getPropertyValueId()) + "_" + v.getPropertyValue(), Function.identity(), (a, b) -> a),
                    m -> new ArrayList<>(m.values()))));
            result.add(propertyVO);
        }
        return result;
    }

    private String joinLongList(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list.stream().map(String::valueOf).toArray(String[]::new));
    }

    private String joinIntegerList(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list.stream().map(String::valueOf).toArray(String[]::new));
    }

    // 辅助方法：解析ID
    private Set<Long> parseIds(String idsStr) {
        return Arrays.stream(idsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    // 辅助方法：构建id和名称列表
    private List<IdAndNameVO> buildIdAndNameList(Set<Long> ids, Map<Long, String> idNameMap) {
        List<IdAndNameVO> tagList = Lists.newArrayList();
        for (Long id : ids) {
            IdAndNameVO idAndNameVO = new IdAndNameVO();
            idAndNameVO.setId(id);
            idAndNameVO.setName(idNameMap.getOrDefault(id, null));
            tagList.add(idAndNameVO);
        }
        return tagList;
    }

    private Map<Long, MarketingChannelRespDTO> buildChannelId2ChannelMap(List<Long> channelIds) {
        Map<Long, MarketingChannelRespDTO> channelId2ChannelMap = Maps.newHashMap();
        if (CollUtil.isEmpty(channelIds)) {
            log.info("buildChannelId2ChannelMap,channelIds is empty");
            return channelId2ChannelMap;
        }
        MarketingChannelIdsReqDTO marketingChannelIdsReqDTO = new MarketingChannelIdsReqDTO();
        marketingChannelIdsReqDTO.setChannelIds(channelIds);
        List<MarketingChannelRespDTO> channels = FeginMethodExecuteUtils.execute(() -> marketingChannelApi.getChannelListByIdsApi(marketingChannelIdsReqDTO), true);
        if (CollectionUtil.isNotEmpty(channels)) {
            channelId2ChannelMap = channels.stream().collect(Collectors.toMap(MarketingChannelRespDTO::getChannelId
                    , marketingChannelRespDTO -> marketingChannelRespDTO));
        }
        return channelId2ChannelMap;
    }

    private void modSaveMarketingProductSpu(MarketingProductSpu marketingProductSpu, MarketingProductModReqVO reqVO,
                                            Long userId, Date now) {
        marketingProductSpu.setStandardProductSpuId(reqVO.getStandardProductSpuId());
        marketingProductSpu.setName(reqVO.getName());
        marketingProductSpu.setProductCondition(reqVO.getProductCondition());
        marketingProductSpu.setMonitorAttribute(reqVO.getMonitorAttribute());
        marketingProductSpu.setMinBuybackPrice(reqVO.getMinBuybackPrice());
        marketingProductSpu.setMaxBuybackPrice(reqVO.getMaxBuybackPrice());

        // 图文信息
        marketingProductSpu.setMainPicUrls(String.join(",", reqVO.getMainPicUrls()));
        marketingProductSpu.setCarouselPicUrls(String.join(",", reqVO.getCarouselPicUrls()));
        marketingProductSpu.setVideoUrls(String.join(",", reqVO.getVideoUrls()));
        marketingProductSpu.setDetailPicUrls(String.join(",", reqVO.getDetailPicUrls()));
        marketingProductSpu.setDetailTagIds(joinLongList(reqVO.getDetailTagIds()));
        marketingProductSpu.setSkuTagIds(joinLongList(reqVO.getSkuTagIds()));

        // 增值服务
        marketingProductSpu.setValueAddedIds(joinLongList(reqVO.getValueAddedIds()));
        marketingProductSpu.setShowPage(joinIntegerList(reqVO.getShowPages()));
        marketingProductSpu.setIsDefaultSelected(reqVO.getIsDefaultSelected());
        marketingProductSpu.setDefaultSelectedValueAddedId(reqVO.getDefaultSelectedValueAddedId());

        // 订单服务信息
        marketingProductSpu.setCompensationRuleId(reqVO.getCompensationRuleId());
        marketingProductSpu.setShippingWay(reqVO.getShippingWay());
        marketingProductSpu.setShippingTemplateId(reqVO.getShippingTemplateId());
        marketingProductSpu.setShippingAreaCodes(String.join(",", reqVO.getShippingAreaCodes()));

        // 商品上架信息
        marketingProductSpu.setShelvingWay(reqVO.getShelvingWay());
        if (ShelvingWayEnum.APPOINT_SHELVES.getValue().equals(reqVO.getShelvingWay())) {
            marketingProductSpu.setShelvingTime(reqVO.getShelvingTime());
        }
        marketingProductSpu.setShelvingChannelId(joinLongList(reqVO.getShelvingChannelIds()));

        // 通用字段
        marketingProductSpu.setUpdateTime(now);
        marketingProductSpu.setUpdateBy(userId);

        // 草稿状态
        if (OperateTypeEnum.SUBMIT.getType().equals(reqVO.getOperateType())) {
            marketingProductSpu.setIsDraft(NumberUtils.INTEGER_ZERO);
        } else {
            marketingProductSpu.setIsDraft(NumberUtils.INTEGER_ONE);
        }

        // 审批、上下架状态
        marketingProductSpu.setApprovalStatus(ApproveStatusEnum.WAIT_APPROVE.getValue());
        marketingProductSpu.setShelvesStatus(ShelvesStatusEnum.WAIT_SHELVES.getValue());

        // 回收商品的收货地址
        marketingProductSpu.setReceivingAddress(reqVO.getReceivingAddress());
        marketingProductSpuMapper.updateById(marketingProductSpu);
    }
}
