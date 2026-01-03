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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.danielbechler.util.Strings;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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

        return null;
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
    public void del(Long id) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("del standard product id:{},operatorId:{}", id, loginUser.getId());
        StandardProductSpu spu = spuMapper.selectById(id);
        if (spu == null) {
            throw exception(STANDARD_PRODUCT_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(spu.getIsDeleted())) {
            return;
        }
        spu.setIsDeleted(NumberUtils.INTEGER_ONE);
        spu.setUpdateTime(new Date());
        spu.setUpdateBy(loginUser.getId());
        spuMapper.updateById(spu);
    }

    @Override
    public void changeStatus(StatusChangeReqVo reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("changeStatus standard product reqVO:{},operatorId:{}", reqVO, loginUser.getId());
        StandardProductSpu spu = spuMapper.selectById(reqVO.getId());
        if (spu == null) {
            throw exception(STANDARD_PRODUCT_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(spu.getIsDeleted())) {
            throw exception(STANDARD_PRODUCT_DELETED);
        }

        spu.setStatus(reqVO.getStatus());
        spu.setIsDeleted(NumberUtils.INTEGER_ONE);
        spu.setUpdateTime(new Date());
        spu.setUpdateBy(loginUser.getId());
        spuMapper.updateById(spu);
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
