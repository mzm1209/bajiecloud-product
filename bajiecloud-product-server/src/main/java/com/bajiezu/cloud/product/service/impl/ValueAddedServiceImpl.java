package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
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
import com.bajiezu.cloud.product.dal.dto.ValueAddedQuery;
import com.bajiezu.cloud.product.dal.entity.ValueAdded;
import com.bajiezu.cloud.product.dal.entity.ValueAddedProduct;
import com.bajiezu.cloud.product.dal.mapper.ValueAddedMapper;
import com.bajiezu.cloud.product.dal.mapper.ValueAddedProductMapper;
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        List<Long> marketingProductSkuIds = valueAddedProductMapper.queryMarketingProductSkuIdsByValueAddedId(id);

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

        // todo 根据营销商品skuIds获取对应的商品信息

        return valueAddedRespVO;
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

    private ValueAdded buildValueAdded(ValueAddedAddReqVO reqVO, LoginUser<?> loginUser, Date now) {
        ValueAdded valueAdded = new ValueAdded();
        String code = "ZZ" + DateUtil.format(new Date(), "yyMMdd") + sequenceGenerator.getValueAddedSequence();
        valueAdded.setCode(code);
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
