package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bajiezu.cloud.common.constants.CommonStatusEnum;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StatusChangeReqVo;
import com.bajiezu.cloud.product.controller.vo.response.StandardProductRespVO;
import com.bajiezu.cloud.product.dal.dto.StandardProductQuery;
import com.bajiezu.cloud.product.dal.entity.ProductBrand;
import com.bajiezu.cloud.product.dal.entity.ProductBusinessCategory;
import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import com.bajiezu.cloud.product.dal.entity.StandardProductSpu;
import com.bajiezu.cloud.product.dal.mapper.ProductBrandMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductBusinessCategoryMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductMarketingCategoryMapper;
import com.bajiezu.cloud.product.dal.mapper.StandardProductSpuMapper;
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
    public void add(StandardProductAddReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("add standard product reqVO:{},operatorId:{}", reqVO, loginUser.getId());

        // 保存spu
        Date now = new Date();
        StandardProductSpu spu = buildSpu(reqVO, loginUser, now);
        spuMapper.insert(spu);
    }

    @Override
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
        spuMapper.updateById(spu);
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

    private Map<Long, String> buildUserId2NameMap(Set<Long> userIds) {
        CommonResult<List<AdminUserRespDTO>> usersResult = adminUserApi.getUserList(userIds);
        Map<Long, String> userId2NameMap = Maps.newHashMap();
        if (usersResult.isSuccess() && usersResult.getData() != null) {
            usersResult.getData().forEach(user -> userId2NameMap.put(user.getId(), user.getName()));
        }
        return userId2NameMap;
    }
}
