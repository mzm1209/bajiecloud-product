package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bajiezu.cloud.common.constants.CommonStatusEnum;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.FeginMethodExecuteUtils;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.*;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StatusChangeReqVo;
import com.bajiezu.cloud.product.controller.vo.response.StandardProductRespVO;
import com.bajiezu.cloud.product.dal.dto.StandardProductQuery;
import com.bajiezu.cloud.product.dal.entity.ProductBrand;
import com.bajiezu.cloud.product.dal.entity.ProductBusinessCategory;
import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import com.bajiezu.cloud.product.dal.entity.*;
import com.bajiezu.cloud.product.dal.mapper.ProductBrandMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductBusinessCategoryMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductMarketingCategoryMapper;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.service.StandardProductService;
import com.bajiezu.cloud.product.util.SequenceGenerator;
import com.bajiezu.cloud.system.api.user.AdminUserApi;
import com.bajiezu.cloud.system.dto.AdminUserRespDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.danielbechler.util.Strings;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.STANDARD_PRODUCT_DELETED;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.STANDARD_PRODUCT_NOT_EXIST;

@Slf4j
@Service
public class StandardProductServiceImpl implements StandardProductService {

    @Resource
    private StandardProductSpuMapper spuMapper;
    @Resource
    private SequenceGenerator sequenceGenerator;
    @Resource
    private ProductMarketingCategoryMapper marketingCategoryMapper;
    @Resource
    private ProductBusinessCategoryMapper businessCategoryMapper;
    @Resource
    private ProductBrandMapper productBrandMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private StandardProductSpuPropertyMapper spuPropertyMapper;
    @Resource
    private StandardProductSpuPropertyValueMapper spuPropertyValueMapper;
    @Resource
    private StandardProductSkuMapper skuMapper;
    @Resource
    private StandardProductSkuPropertyValueMapper skuPropertyValueMapper;

    @Override
    public PageResult<StandardProductRespVO> page(StandardProductListReqVO reqVO) {
        log.info("list standard product reqVO: {}", reqVO);

        // 获取标准商品
        StandardProductQuery query = reqVO.convert2StandardProductQuery();
        List<StandardProductSpu> productSpus = spuMapper.selectListByQuery(query);
        if (CollectionUtil.isEmpty(productSpus)) {
            return PageResult.empty();
        }
        long count = spuMapper.selectCountByQuery(query);

        // 获取用户、品牌、营销类目、经营类目
        Set<Long> userIds = Sets.newHashSet();
        Set<Long> brandIds = Sets.newHashSet();
        Set<Long> marketingCategoryIds = Sets.newHashSet();
        Set<Long> businessCategoryIds = Sets.newHashSet();
        for (StandardProductSpu spu : productSpus) {
            userIds.add(spu.getCreateBy());
            userIds.add(spu.getUpdateBy());
            brandIds.add(spu.getProductBrandId());
            marketingCategoryIds.add(spu.getMarketingCategoryId());
            businessCategoryIds.add(spu.getBusinessCategoryId());
        }
        Map<Long, String> userId2NameMap = buildUserId2NameMap(userIds);
        List<ProductBrand> brands = productBrandMapper.selectListByIds(brandIds);
        Map<Long, ProductBrand> brandId2BrandMap = brands.stream().collect(Collectors.toMap(ProductBrand::getId, brand -> brand));

        List<ProductMarketingCategory> marketingCategories = marketingCategoryMapper.selectListByIds(marketingCategoryIds);
        Set<Long> allMarketingCategoryIds = Sets.newHashSet();
        for (ProductMarketingCategory marketingCategory : marketingCategories) {
            allMarketingCategoryIds.add(marketingCategory.getId());
            if (StringUtils.isNotBlank(marketingCategory.getPath())) {
                long[] ids = StrUtil.splitToLong(marketingCategory.getPath(), ',');
                allMarketingCategoryIds.addAll(Arrays.stream(ids).boxed().toList());
            }
        }
        marketingCategories = marketingCategoryMapper.selectListByIds(allMarketingCategoryIds);
        Map<Long, ProductMarketingCategory> marketingCategoryId2MarketingCategoryMap = marketingCategories.stream().collect(Collectors.toMap(ProductMarketingCategory::getId, marketingCategory -> marketingCategory));

        List<ProductBusinessCategory> businessCategories = businessCategoryMapper.selectListByIds(businessCategoryIds);
        Set<Long> allBusinessCategoryIds = Sets.newHashSet();
        for (ProductBusinessCategory businessCategory : businessCategories) {
            allBusinessCategoryIds.add(businessCategory.getId());
            if (StringUtils.isNotBlank(businessCategory.getPath())) {
                long[] ids = StrUtil.splitToLong(businessCategory.getPath(), ',');
                allBusinessCategoryIds.addAll(Arrays.stream(ids).boxed().toList());
            }
        }
        businessCategories = businessCategoryMapper.selectListByIds(allBusinessCategoryIds);
        Map<Long, ProductBusinessCategory> businessCategoryId2BusinessCategoryMap = businessCategories.stream().collect(Collectors.toMap(ProductBusinessCategory::getId, businessCategory -> businessCategory));

        List<StandardProductRespVO> standardProductRespVOS = Lists.newArrayList();
        for (StandardProductSpu spu : productSpus) {
            StandardProductRespVO standardProductRespVO = new StandardProductRespVO();
            standardProductRespVOS.add(standardProductRespVO);
            standardProductRespVO.setId(spu.getId());
            standardProductRespVO.setCode(spu.getCode());
            standardProductRespVO.setName(spu.getName());
            standardProductRespVO.setBrandId(spu.getProductBrandId());
            standardProductRespVO.setBrandName(brandId2BrandMap.get(spu.getProductBrandId()).getName());
            standardProductRespVO.setMarketingCategoryId(spu.getMarketingCategoryId());
            long marketingCategoryId = spu.getMarketingCategoryId();
            standardProductRespVO.setMarketingCategoryId(marketingCategoryId);
            ProductMarketingCategory marketingCategory = marketingCategoryId2MarketingCategoryMap.get(marketingCategoryId);
            List<String> marketingCategoryNames = Lists.newArrayList();
            if (StringUtils.isBlank(marketingCategory.getPath())) {
                marketingCategoryNames.add(marketingCategory.getName());
            } else {
                marketingCategoryNames.addAll(StrUtil.split(marketingCategory.getPath(), ',').stream().map(
                        id -> marketingCategoryId2MarketingCategoryMap.get(Long.parseLong(id)).getName()).toList());
            }
            standardProductRespVO.setMarketingCategoryName(String.join(">", marketingCategoryNames));

            long businessCategoryId = spu.getBusinessCategoryId();
            standardProductRespVO.setBusinessCategoryId(marketingCategoryId);
            ProductBusinessCategory businessCategory = businessCategoryId2BusinessCategoryMap.get(businessCategoryId);
            List<String> businessCategoryNames = Lists.newArrayList();
            if (StringUtils.isBlank(businessCategory.getPath())) {
                businessCategoryNames.add(businessCategory.getName());
            } else {
                businessCategoryNames.addAll(StrUtil.split(businessCategory.getPath(), ',').stream().map(
                        id -> businessCategoryId2BusinessCategoryMap.get(Long.parseLong(id)).getName()).toList());
            }
            standardProductRespVO.setBusinessCategoryName(String.join(">", businessCategoryNames));
            standardProductRespVO.setStatus(spu.getStatus());
            standardProductRespVO.setProductConditions(Arrays.stream(StrUtil.splitToInt(spu.getProductCondition(), ','))
                    .boxed().collect(Collectors.toList()));
            standardProductRespVO.setMonitorAttributes(Arrays.stream(StrUtil.splitToInt(spu.getMonitorAttribute(), ','))
                    .boxed().collect(Collectors.toList()));
            standardProductRespVO.setIsDraft(spu.getIsDraft());
            standardProductRespVO.setCreatorName(userId2NameMap.get(spu.getCreateBy()));
            standardProductRespVO.setCreateTime(spu.getCreateTime());
            standardProductRespVO.setUpdaterName(userId2NameMap.get(spu.getUpdateBy()));
            standardProductRespVO.setUpdateTime(spu.getUpdateTime());
        }

        return new PageResult<>(standardProductRespVOS, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(StandardProductAddReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("add standard product reqVO:{},operatorId:{},partnerId:{}", reqVO, loginUser.getId(),loginUser.getPartnerId());

        // 保存spu
        Date now = new Date();
        StandardProductSpu spu = buildSpu(reqVO, loginUser, now);
        spuMapper.insert(spu);
        saveSpuPropertyAndSku(spu.getId(), reqVO, loginUser, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mod(StandardProductModReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("mod standard product reqVO:{},operatorId:{}", reqVO, loginUser.getId());
        StandardProductSpu spu = spuMapper.selectById(reqVO.getId());
        if (spu == null) {
            throw exception(STANDARD_PRODUCT_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(spu.getIsDeleted())) {
            throw exception(STANDARD_PRODUCT_DELETED);
        }

        if (NumberUtils.INTEGER_ONE.equals(reqVO.getOperationType())) {
            // 保存为草稿
            spu.setStatus(CommonStatusEnum.DISABLE.getStatus());
            spu.setIsDraft(NumberUtils.INTEGER_ONE);
        } else {
            // 提交
            spu.setStatus(CommonStatusEnum.ENABLE.getStatus());
            spu.setIsDraft(NumberUtils.INTEGER_ZERO);
        }
        spu.setProductBrandId(reqVO.getBrandId());
        spu.setName(reqVO.getName());
        spu.setBusinessCategoryId(reqVO.getBusinessCategoryId());
        spu.setMarketingCategoryId(reqVO.getMarketingCategoryId());
        spu.setProductCondition(Strings.join(",", reqVO.getProductConditions()));
        spu.setMonitorAttribute(Strings.join(",", reqVO.getMonitorAttribute()));
        spu.setUpdateTime(new Date());
        spu.setUpdateBy(loginUser.getId());
        Date now = new Date();
        spu.setUpdateTime(now);
        spuMapper.updateById(spu);
        spuPropertyMapper.logicDelByStandardSpuId(spu.getId(), loginUser.getId(), now);
        spuPropertyValueMapper.logicDelByStandardSpuId(spu.getId(), loginUser.getId(), now);
        skuPropertyValueMapper.logicDelByStandardSpuId(spu.getId(), loginUser.getId(), now);
        skuMapper.logicDelByStandardSpuId(spu.getId(), loginUser.getId(), now);
        saveSpuPropertyAndSku(spu.getId(), reqVO, loginUser, now);
    }

    @Override
    public StandardProductRespVO detail(Long id) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("detail standard product id:{},operatorId:{}", id, loginUser.getId());
        StandardProductSpu spu = spuMapper.selectById(id);
        if (spu == null) {
            throw exception(STANDARD_PRODUCT_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(spu.getIsDeleted())) {
            throw exception(STANDARD_PRODUCT_DELETED);
        }
        Set<Long> userIds = Sets.newHashSet();
        userIds.add(spu.getCreateBy());
        userIds.add(spu.getUpdateBy());
        Map<Long, String> userId2NameMap = buildUserId2NameMap(userIds);

        String brandName = productBrandMapper.selectNameById(spu.getProductBrandId());
        List<ProductMarketingCategory> marketingCategories = marketingCategoryMapper.selectSelfAndParentsById(spu.getMarketingCategoryId());
        List<ProductBusinessCategory> businessCategories = businessCategoryMapper.selectSelfAndParentsById(spu.getBusinessCategoryId());

        // 拼接营销类目名称
        String marketingCategoryPath = marketingCategories.stream()
                .map(ProductMarketingCategory::getName)
                .reduce((a, b) -> a + " > " + b)
                .orElse("");

        // 拼接经营类目名称
        String businessCategoryPath = businessCategories.stream()
                .map(ProductBusinessCategory::getName)
                .reduce((a, b) -> a + " > " + b)
                .orElse("");

        StandardProductRespVO respVO = new StandardProductRespVO();
        respVO.setId(spu.getId());
        respVO.setBrandId(spu.getProductBrandId());
        respVO.setBrandName(brandName);
        respVO.setName(spu.getName());
        respVO.setCode(spu.getCode());
        respVO.setMarketingCategoryId(spu.getMarketingCategoryId());
        respVO.setMarketingCategoryName(marketingCategoryPath);
        respVO.setBusinessCategoryId(spu.getBusinessCategoryId());
        respVO.setBusinessCategoryName(businessCategoryPath);
        respVO.setStatus(spu.getStatus());
        int[] productConditionArray = StrUtil.splitToInt(spu.getProductCondition(), ',');
        respVO.setProductConditions(Arrays.stream(productConditionArray).boxed().collect(Collectors.toList()));
        int[] monitorAttributeArray = StrUtil.splitToInt(spu.getMonitorAttribute(), ',');
        respVO.setMonitorAttributes(Arrays.stream(monitorAttributeArray).boxed().collect(Collectors.toList()));
        respVO.setIsDraft(spu.getIsDraft());
        respVO.setCreatorName(userId2NameMap.get(spu.getCreateBy()));
        respVO.setUpdaterName(userId2NameMap.get(spu.getUpdateBy()));
        respVO.setCreateTime(spu.getCreateTime());
        respVO.setUpdateTime(spu.getUpdateTime());
        fillPropertyAndSku(respVO, spu.getId());
        return respVO;
    }

    @Override
    public void del(List<Long> ids) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("del standard product ids:{},operatorId:{}", ids, loginUser.getId());
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        spuMapper.logicDelByIds(ids, loginUser.getId(), new Date());
    }

    @Override
    public void changeStatus(StatusChangeReqVo reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("changeStatus standard product reqVO:{},operatorId:{}", reqVO, loginUser.getId());
        if (CollUtil.isEmpty(reqVO.getIds())) {

        }
        spuMapper.updateStatusByIds(reqVO.getIds(), reqVO.getStatus(), loginUser.getId(), new Date());
    }

    private StandardProductSpu buildSpu(StandardProductAddReqVO reqVO, LoginUser<?> loginUser, Date now) {
        StandardProductSpu spu = new StandardProductSpu();
        spu.setCode(sequenceGenerator.getStandardProductSequence());
        spu.setProductBrandId(reqVO.getBrandId());
        spu.setName(reqVO.getName());
        spu.setBusinessCategoryId(reqVO.getBusinessCategoryId());
        spu.setMarketingCategoryId(reqVO.getMarketingCategoryId());

        if (NumberUtils.INTEGER_ONE.equals(reqVO.getOperationType())) {
            // 保存为草稿
            spu.setStatus(CommonStatusEnum.DISABLE.getStatus());
            spu.setIsDraft(NumberUtils.INTEGER_ONE);
        } else {
            // 提交
            spu.setStatus(CommonStatusEnum.ENABLE.getStatus());
            spu.setIsDraft(NumberUtils.INTEGER_ZERO);
        }
        spu.setProductCondition(Strings.join(",", reqVO.getProductConditions()));
        spu.setMonitorAttribute(Strings.join(",", reqVO.getMonitorAttribute()));
        spu.setPartnerId(loginUser.getPartnerId());
        spu.setCreateTime(now);
        spu.setUpdateTime(now);
        spu.setCreateBy(loginUser.getId());
        spu.setUpdateBy(loginUser.getId());
        spu.setIsDeleted(NumberUtils.INTEGER_ZERO);
        return spu;
    }



    private void saveSpuPropertyAndSku(Long spuId, StandardProductAddReqVO reqVO, LoginUser<?> loginUser, Date now) {
        if (CollectionUtil.isEmpty(reqVO.getSpuProperties())) { return; }
        List<StandardProductSpuProperty> properties = new ArrayList<>();
        Map<Long, StandardProductPropertyVO> propertyVOMap = new HashMap<>();
        for (StandardProductPropertyVO vo : reqVO.getSpuProperties()) {
            StandardProductSpuProperty p = new StandardProductSpuProperty();
            p.setStandardSpuId(spuId); p.setProductPropertyId(vo.getPropertyId()); p.setSort(vo.getSort()); p.setIsAddPropertyPic(vo.getIsAddPropertyPic()); p.setIsAddMarketingCorner(vo.getIsAddMarketingCorner()); p.setIsSkuProperty(vo.getIsSkuProperty()); p.setPartnerId(loginUser.getPartnerId()); p.setCreateBy(loginUser.getId()); p.setUpdateBy(loginUser.getId()); p.setCreateTime(now); p.setUpdateTime(now); p.setIsDeleted(0);
            properties.add(p); propertyVOMap.put(vo.getPropertyId(), vo);
        }
        spuPropertyMapper.insertBatch(properties);
        List<StandardProductSpuProperty> dbProps = spuPropertyMapper.selectListByStandardSpuId(spuId);
        Map<Long, Long> propertyId2SpuPropertyId = dbProps.stream().collect(Collectors.toMap(StandardProductSpuProperty::getProductPropertyId, StandardProductSpuProperty::getId));
        List<StandardProductSpuPropertyValue> values = new ArrayList<>();
        List<List<StandardProductSpuPropertyValue>> skuDims = new ArrayList<>();
        for (StandardProductPropertyVO pvo: reqVO.getSpuProperties()) {
            List<StandardProductSpuPropertyValue> dim = new ArrayList<>();
            if (CollectionUtil.isEmpty(pvo.getPropertyValues())) { continue; }
            for (StandardProductPropertyValueVO v: pvo.getPropertyValues()) {
                StandardProductSpuPropertyValue pv = new StandardProductSpuPropertyValue();
                pv.setStandardSpuId(spuId); pv.setSpuPropertyId(propertyId2SpuPropertyId.get(pvo.getPropertyId())); pv.setProductPropertyValueId(v.getProductPropertyValueId()); pv.setPropertyValue(v.getValue()); pv.setPicUrl(pvo.getIsAddPropertyPic()!=null&&pvo.getIsAddPropertyPic()==1?v.getPicUrl():null); pv.setMarketingCornerText(pvo.getIsAddMarketingCorner()!=null&&pvo.getIsAddMarketingCorner()==1?StringUtils.defaultString(v.getMarketingCornerText()):""); pv.setSort(v.getSort()); pv.setCreateBy(loginUser.getId()); pv.setUpdateBy(loginUser.getId()); pv.setCreateTime(now); pv.setUpdateTime(now); pv.setIsDeleted(0);
                values.add(pv);
                if (Integer.valueOf(1).equals(pvo.getIsSkuProperty())) dim.add(pv);
            }
            if (!dim.isEmpty()) skuDims.add(dim);
        }
        if (!values.isEmpty()) spuPropertyValueMapper.insertBatch(values);
        List<StandardProductSpuPropertyValue> dbVals = spuPropertyValueMapper.selectListByStandardSpuId(spuId);
        Map<String, StandardProductSpuPropertyValue> keyMap = dbVals.stream().collect(Collectors.toMap(v -> v.getSpuPropertyId()+"_"+v.getProductPropertyValueId()+"_"+v.getPropertyValue(), v->v,(a,b)->a));
        List<List<StandardProductSpuPropertyValue>> dbSkuDims = new ArrayList<>();
        for (List<StandardProductSpuPropertyValue> dim: skuDims){ List<StandardProductSpuPropertyValue> nd = new ArrayList<>(); for (StandardProductSpuPropertyValue v:dim){ String k=v.getSpuPropertyId()+"_"+v.getProductPropertyValueId()+"_"+v.getPropertyValue(); if(keyMap.get(k)!=null) nd.add(keyMap.get(k)); } if(!nd.isEmpty()) dbSkuDims.add(nd);}
        List<List<StandardProductSpuPropertyValue>> combos = cartesian(dbSkuDims);
        List<StandardProductSku> skus = new ArrayList<>();
        for (int i=0;i<combos.size();i++){ StandardProductSku sku=new StandardProductSku(); sku.setStandardSpuId(spuId); sku.setStock(0); sku.setCreateBy(loginUser.getId()); sku.setUpdateBy(loginUser.getId()); sku.setCreateTime(now); sku.setUpdateTime(now); sku.setIsDeleted(0); skus.add(sku);}
        if (!skus.isEmpty()) skuMapper.insertBatch(skus);
        List<StandardProductSku> dbSkus = skuMapper.selectListByStandardSpuId(spuId);
        List<StandardProductSkuPropertyValue> refs = new ArrayList<>();
        for(int i=0;i<Math.min(dbSkus.size(),combos.size());i++){ for(StandardProductSpuPropertyValue v:combos.get(i)){ StandardProductSkuPropertyValue r=new StandardProductSkuPropertyValue(); r.setStandardProductSkuId(dbSkus.get(i).getId()); r.setStandardSpuId(spuId); r.setStandardSpuPropertyValueId(v.getId()); r.setCreateBy(loginUser.getId()); r.setUpdateBy(loginUser.getId()); r.setCreateTime(now); r.setUpdateTime(now); r.setIsDeleted(0); refs.add(r);} }
        if(!refs.isEmpty()) skuPropertyValueMapper.insertBatch(refs);
    }

    private List<List<StandardProductSpuPropertyValue>> cartesian(List<List<StandardProductSpuPropertyValue>> dims){
        List<List<StandardProductSpuPropertyValue>> r = new ArrayList<>();
        r.add(new ArrayList<>());
        for (List<StandardProductSpuPropertyValue> dim: dims){ List<List<StandardProductSpuPropertyValue>> nr = new ArrayList<>(); for (List<StandardProductSpuPropertyValue> pre: r){ for(StandardProductSpuPropertyValue v: dim){ List<StandardProductSpuPropertyValue> c = new ArrayList<>(pre); c.add(v); nr.add(c);} } r=nr; }
        return dims.isEmpty()?new ArrayList<>():r;
    }

    private void fillPropertyAndSku(StandardProductRespVO respVO, Long spuId){
        List<StandardProductSpuProperty> props = spuPropertyMapper.selectListByStandardSpuId(spuId);
        List<StandardProductSpuPropertyValue> vals = spuPropertyValueMapper.selectListByStandardSpuId(spuId);
        Map<Long, List<StandardProductSpuPropertyValue>> valMap = vals.stream().collect(Collectors.groupingBy(StandardProductSpuPropertyValue::getSpuPropertyId));
        List<StandardProductPropertyVO> pvos = new ArrayList<>();
        for (StandardProductSpuProperty p: props){
            StandardProductPropertyVO vo = new StandardProductPropertyVO();
            vo.setPropertyId(p.getProductPropertyId()); vo.setSort(p.getSort()); vo.setIsAddPropertyPic(p.getIsAddPropertyPic()); vo.setIsAddMarketingCorner(p.getIsAddMarketingCorner()); vo.setIsSkuProperty(p.getIsSkuProperty());
            List<StandardProductPropertyValueVO> pv = new ArrayList<>();
            for (StandardProductSpuPropertyValue v: valMap.getOrDefault(p.getId(), Collections.emptyList())){ StandardProductPropertyValueVO vvo = new StandardProductPropertyValueVO(); vvo.setProductPropertyValueId(v.getProductPropertyValueId()); vvo.setValue(v.getPropertyValue()); vvo.setSort(v.getSort()); vvo.setPicUrl(v.getPicUrl()); vvo.setMarketingCornerText(v.getMarketingCornerText()); pv.add(vvo);} vo.setPropertyValues(pv); pvos.add(vo);
        }
        respVO.setSpuProperties(pvos);
        List<StandardProductSku> skus = skuMapper.selectListByStandardSpuId(spuId);
        List<StandardProductSkuPropertyValue> rels = skuPropertyValueMapper.selectListByStandardSpuId(spuId);
        Map<Long, StandardProductSpuPropertyValue> valById = vals.stream().collect(Collectors.toMap(StandardProductSpuPropertyValue::getId, a->a,(a,b)->a));
        Map<Long, List<StandardProductSkuPropertyValue>> relMap = rels.stream().collect(Collectors.groupingBy(StandardProductSkuPropertyValue::getStandardProductSkuId));
        List<StandardProductSkuVO> skuVOS = new ArrayList<>();
        for (StandardProductSku sku: skus){ StandardProductSkuVO svo = new StandardProductSkuVO(); svo.setId(sku.getId()); svo.setStock(sku.getStock()); List<SkuPropertyValueVO> pvs = new ArrayList<>(); for (StandardProductSkuPropertyValue rel: relMap.getOrDefault(sku.getId(), Collections.emptyList())){ StandardProductSpuPropertyValue v = valById.get(rel.getStandardSpuPropertyValueId()); if(v==null) continue; SkuPropertyValueVO t = new SkuPropertyValueVO(); t.setPropertyId(v.getSpuPropertyId()); t.setPropertyValueId(v.getProductPropertyValueId()); t.setPropertyValue(v.getPropertyValue()); pvs.add(t);} svo.setPropertyValues(pvs); skuVOS.add(svo);}        
        respVO.setSkus(skuVOS);
    }

    private Map<Long, String> buildUserId2NameMap(Set<Long> userIds) {
        Map<Long, String> userId2NameMap = Maps.newHashMap();
        try {
            List<AdminUserRespDTO> userRespDTOS = FeginMethodExecuteUtils.execute(() -> adminUserApi.getUserList(userIds), true);
            if (CollectionUtil.isNotEmpty(userRespDTOS)) {
                userRespDTOS.forEach(user -> userId2NameMap.put(user.getId(), user.getName()));
            }
        } catch (Exception e) {
            log.error("查询用户信息失败", e);
        }
        return userId2NameMap;
    }
}
