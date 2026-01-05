package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.bajiezu.cloud.common.constants.OperateTypeEnum;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.MarketingProductPropertyValueVO;
import com.bajiezu.cloud.product.controller.vo.MarketingProductPropertyVO;
import com.bajiezu.cloud.product.controller.vo.MarketingProductSkuVO;
import com.bajiezu.cloud.product.controller.vo.SkuPropertyValueVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductApproveReqVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.OnOffShelvesReqVO;
import com.bajiezu.cloud.product.controller.vo.response.MarketingProductDetailRespVO;
import com.bajiezu.cloud.product.controller.vo.response.MarketingProductRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductTypeStatisticRespVO;
import com.bajiezu.cloud.product.controller.vo.response.StatusStatisticRespVO;
import com.bajiezu.cloud.product.dal.dto.ApproveStatusStatisticCountDTO;
import com.bajiezu.cloud.product.dal.dto.ProductTypeStatisticCountDTO;
import com.bajiezu.cloud.product.dal.dto.ShelvesStatisticCountDTO;
import com.bajiezu.cloud.product.dal.entity.*;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.enums.ApproveStatusEnum;
import com.bajiezu.cloud.product.enums.ProductTypeEnum;
import com.bajiezu.cloud.product.enums.ShelvesStatusEnum;
import com.bajiezu.cloud.product.service.MarketingProductService;
import com.bajiezu.cloud.product.util.SequenceGenerator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Override
    public PageResult<MarketingProductRespVO> page(MarketingProductListReqVO reqVO) {
        return null;
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
        return null;
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

        // 获取商品总数
        Integer totalCount = marketingProductSpuMapper.queryCount(reqVO);

        // 获取草稿商品数
        reqVO.setIsDraft(1);
        Integer draftCount = marketingProductSpuMapper.queryCount(reqVO);

        // 获取审核商品数
        reqVO.setIsDraft(null);
        List<ApproveStatusStatisticCountDTO> approveStatusStatisticCountDTOS = marketingProductSpuMapper.approveStatusStatistic(reqVO);

        // 获取上下架商品数
        List<ShelvesStatisticCountDTO> shelvesStatisticCountDTOS = marketingProductSpuMapper.shelvesStatistic(reqVO);

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
}
