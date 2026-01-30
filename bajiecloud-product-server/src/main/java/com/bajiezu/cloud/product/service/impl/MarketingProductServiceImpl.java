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

        // 构建并保存营销商品spu
        Date now = new Date();
        MarketingProductSpu marketingProductSpu = buildMarketingProductSpu(reqVO, loginUser, now);
        marketingProductSpuMapper.insert(marketingProductSpu);
        Long marketingProductSpuId = marketingProductSpu.getId();

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

        // 保存商品SKU
        List<MarketingProductSku> skus = buildMarketingProductSkus(reqVO.getSkus(), loginUser, marketingProductSpuId, now);
        skuMapper.insertBatch(skus);

        // 保存商品SKU属性值
        Map<String, Long> skuCode2IdMap = skus.stream().collect(Collectors.toMap(MarketingProductSku::getSkuCode, MarketingProductSku::getId));
        List<MarketingProductSkuPropertyValue> skuPropertyValues = buildMarketingProductSkuPropertyValues(reqVO.getSkus(),
                skuCode2IdMap, spuPropertyValues, loginUser, marketingProductSpuId, now);
        skuPropertyValueMapper.insertBatch(skuPropertyValues);
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

        Date now = new Date();
        // 编辑保存商品SPU
        modSaveMarketingProductSpu(marketingProductSpu, reqVO, loginUser.getId(), now);

        // 处理spu属性
        List<MarketingProductSpuProperty> existSpuPropertyList = spuPropertyMapper.selectListByMarketingSpuId(reqVO.getId());
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
                    spuPropertyValue.setPicUrl(propertyValue.getPicUrl());
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
                        if (!Objects.equals(existSpuPropertyValue.getPicUrl(), unqKey2NewPropertyValueMap.get(existSpuPropertyValue.getUnqKey()).getPicUrl())) {
                            existSpuPropertyValue.setPicUrl(unqKey2NewPropertyValueMap.get(existSpuPropertyValue.getUnqKey()).getPicUrl());
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
                        spuPropertyValue.setPicUrl(newPropertyValue.getPicUrl());
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
        List<MarketingProductSku> existingSkus = skuMapper.selectListByMarketingSpuId(reqVO.getId());
        Map<Long, MarketingProductSku> existingSkuMap = existingSkus.stream()
                .collect(Collectors.toMap(MarketingProductSku::getId, sku -> sku));
        List<MarketingProductSkuVO> incomingSkus = reqVO.getSkus();

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
            }
        }
        detailRespVO.setSpuProperties(spuPropertyVOS);


        // SKU信息
        List<MarketingProductSku> skus = skuMapper.selectListByMarketingSpuId(marketingProductSpu.getId());
        List<MarketingProductSkuPropertyValue> skuPropertyValues = skuPropertyValueMapper.selectListByMarketingSpuId(marketingProductSpu.getId());
        Map<Long, List<MarketingProductSkuPropertyValue>> skuId2SkuPropertyValues = skuPropertyValues.stream().collect(Collectors.groupingBy(MarketingProductSkuPropertyValue::getMarketingProductSkuId));

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
                skuPropertyValueVO.setPropertyValueId(spuPropertyValue.getProductPropertyValueId());
                if (Objects.nonNull(spuPropertyValue.getProductPropertyValueId())) {
                    skuPropertyValueVO.setPropertyValue(productPropertyId2ValueMap.get(spuPropertyValue.getProductPropertyValueId()));
                } else {
                    skuPropertyValueVO.setPropertyValue(spuPropertyValue.getPropertyValue());
                }

            }
            skuVO.setPropertyValues(skuPropertyValueVOS);
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

        // 审批通过
        if (ApproveStatusEnum.APPROVE_PASS.getValue().equals(reqVO.getApproveStatus())) {
            if (ShelvingWayEnum.AUTO_SHELVES.getValue().equals(marketingProductSpu.getShelvingWay())) {
                // 如果是自动上架，那么审批通过后将上架状态改为已上架
                marketingProductSpu.setShelvesStatus(ShelvesStatusEnum.ON_SHELVES.getValue());
            } else if (ShelvingWayEnum.APPOINT_SHELVES.getValue().equals(marketingProductSpu.getShelvingWay())) {
                // 如果是预约上架，预约时间在当前时间之前，将上架状态改为已上架
                if (marketingProductSpu.getShelvingTime().before(new Date())) {
                    marketingProductSpu.setShelvesStatus(ShelvesStatusEnum.ON_SHELVES.getValue());
                }
            }
        }


        marketingProductSpu.setApproverId(loginUser.getId());
        marketingProductSpu.setApprovalStatus(reqVO.getApproveStatus());
        marketingProductSpu.setApprovalRemark(reqVO.getApprovalRemark());
        marketingProductSpu.setUpdateTime(new Date());
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
        return skuRespDto;
    }

    @Override
    public void updateProductShelvesStatus() {
        marketingProductSpuMapper.updateProductShelvesStatus();
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
        marketingProductSpu.setShelvingTime(reqVO.getShelvingTime());
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
                spuPropertyValue.setPicUrl(spuPropertyValueVO.getPicUrl());
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
        marketingProductSpu.setShelvingTime(reqVO.getShelvingTime());
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
