package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bajiezu.cloud.common.constants.OperateTypeEnum;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.marketing.api.channel.MarketingChannelApi;
import com.bajiezu.cloud.marketing.dto.channel.req.MarketingChannelIdsReqDTO;
import com.bajiezu.cloud.marketing.dto.channel.resp.MarketingChannelRespDTO;
import com.bajiezu.cloud.product.controller.MarketingProductPropertyValueVO;
import com.bajiezu.cloud.product.controller.vo.*;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductApproveReqVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.OnOffShelvesReqVO;
import com.bajiezu.cloud.product.controller.vo.response.MarketingProductDetailRespVO;
import com.bajiezu.cloud.product.controller.vo.response.MarketingProductRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductTypeStatisticRespVO;
import com.bajiezu.cloud.product.controller.vo.response.StatusStatisticRespVO;
import com.bajiezu.cloud.product.dal.dto.*;
import com.bajiezu.cloud.product.dal.entity.*;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.enums.ApproveStatusEnum;
import com.bajiezu.cloud.product.enums.ProductTypeEnum;
import com.bajiezu.cloud.product.enums.ShelvesStatusEnum;
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
        // 根据品牌、营销类目搜索
        if (reqVO.getBrandId() != null || reqVO.getMarketingCategoryId() != null) {
            List<Long> standardProductSpuIds = standardProductSpuMapper.selectIdsByBrandIdAndMarketingCategoryId(
                    reqVO.getBrandId(), reqVO.getMarketingCategoryId());
            query.setStandardProductSpuIds(standardProductSpuIds);
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

    @Override
    public void mod(MarketingProductAddReqVO reqVO) {

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

        // 获取营销

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
        String businessCategoryName = businessCategoryMapper.selectNameById(standardProductSpu.getBusinessCategoryId());
        detailRespVO.setBusinessCategoryName(businessCategoryName);
        String marketingCategoryName = marketingCategoryMapper.selectNameById(standardProductSpu.getMarketingCategoryId());
        detailRespVO.setMarketingCategoryName(marketingCategoryName);
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
        detailRespVO.setShowPages(Arrays.stream(StringUtils.split(marketingProductSpu.getShowPage(), ","))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(Integer::valueOf)
                .collect(Collectors.toList()));
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
        if (CollUtil.isNotEmpty(ids)) {
            return;
        }
        // 批量逻辑删除
        marketingProductSpuMapper.logicDeleteByIds(ids, loginUser.getId(), new Date());
        spuPropertyMapper.logicDelByMarketingSpuIds(ids, loginUser.getId(), new Date());
        spuPropertyValueMapper.logicDelByMarketingSpuIds(ids, loginUser.getId(), new Date());
        skuMapper.logicDelByMarketingSpuIds(ids, loginUser.getId(), new Date());
        skuPropertyValueMapper.logicDelByMarketingSpuIds(ids, loginUser.getId(), new Date());

    }

    @Override
    public void onOffShelves(OnOffShelvesReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("onOffShelves reqVO: {},operatorId:{}", reqVO, loginUser.getId());
        if (CollUtil.isNotEmpty(reqVO.getIds())) {
            return;
        }

        // 批量更新商品的上下架状态
        marketingProductSpuMapper.updateShelvesStatusByIds(reqVO.getIds(), reqVO.getShelvesStatus(), loginUser.getId(), new Date());
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

        MarketingProductQuery query = reqVO.toQuery();
        // 根据品牌、营销类目搜索
        if (reqVO.getBrandId() != null || reqVO.getMarketingCategoryId() != null) {
            List<Long> standardProductSpuIds = standardProductSpuMapper.selectIdsByBrandIdAndMarketingCategoryId(
                    reqVO.getBrandId(), reqVO.getMarketingCategoryId());
            query.setStandardProductSpuIds(standardProductSpuIds);
        }
        // 获取商品总数
        Integer totalCount = marketingProductSpuMapper.queryCount(query);

        // 获取草稿商品数
        query.setIsDraft(1);
        Integer draftCount = marketingProductSpuMapper.queryCount(query);

        // 获取审核商品数
        query.setIsDraft(null);
        List<ApproveStatusStatisticCountDTO> approveStatusStatisticCountDTOS = marketingProductSpuMapper.approveStatusStatistic(query);

        // 获取上下架商品数
        List<ShelvesStatisticCountDTO> shelvesStatisticCountDTOS = marketingProductSpuMapper.shelvesStatistic(query);

        StatusStatisticRespVO statusStatisticRespVO = new StatusStatisticRespVO();
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
        CommonResult<List<AdminUserRespDTO>> userResult = adminUserApi.getUserList(userIds);
        Map<Long, String> userId2NameMap = Maps.newHashMap();
        if (userResult.isSuccess() && userResult.getData() != null) {
            userId2NameMap = userResult.getData().stream().collect(Collectors.toMap(AdminUserRespDTO::getId, AdminUserRespDTO::getName));
        }

        // 获取渠道
        Map<Long, MarketingChannelRespDTO> channelId2ChannelMap = buildChannelId2ChannelMap(channelIds);

        // 最低日租金（租赁商品才有）
        Map<Long, IdAndPriceDTO> minDailyRentPriceMap = Maps.newHashMap();
        if (ProductTypeEnum.RENTAL_PRODUCT.getValue().equals(productType)) {
            List<IdAndPriceDTO> minDailyRentPrices = skuMapper.queryMinDailyRentPriceByMarketingProductSpuIds(marketingSpuIds);
            minDailyRentPriceMap = minDailyRentPrices.stream().collect(Collectors.toMap(IdAndPriceDTO::getId, idAndPriceDTO -> idAndPriceDTO));
        }

        // 回收价 （回收商品才有）
        Map<Long, IdAndPriceDTO> buybackPriceMap = Maps.newHashMap();
        if (ProductTypeEnum.RECYCLED_PRODUCT.getValue().equals(productType)) {
            List<IdAndPriceDTO> buybackPrices = skuMapper.queryMinAndMaxBuybackPriceByMarketingProductSpuIds(marketingSpuIds);
            buybackPriceMap = buybackPrices.stream().collect(Collectors.toMap(IdAndPriceDTO::getId, idAndPriceDTO -> idAndPriceDTO));
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

            if (buybackPriceMap.containsKey(marketingProductSpu.getId())) {
                marketingProductRespVO.setMinBuybackPrice(buybackPriceMap.get(marketingProductSpu.getId()).getMinPrice());
                marketingProductRespVO.setMaxBuybackPrice(buybackPriceMap.get(marketingProductSpu.getId()).getMaxPrice());
            }

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
        CommonResult<List<MarketingChannelRespDTO>> channelResult = marketingChannelApi.getChannelListByIdsApi(marketingChannelIdsReqDTO);
        if (channelResult.isSuccess() && channelResult.getData() != null) {
            channelId2ChannelMap = channelResult.getData().stream().collect(Collectors.toMap(MarketingChannelRespDTO::getChannelId
                    , marketingChannelRespDTO -> marketingChannelRespDTO));
        }
        return channelId2ChannelMap;
    }
}
