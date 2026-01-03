package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.AreaCodeAndNameVO;
import com.bajiezu.cloud.product.controller.vo.request.ExpressTemplateAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ExpressTemplateListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ExpressTemplateModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StatusChangeReqVo;
import com.bajiezu.cloud.product.controller.vo.response.ExpressTemplateRespVO;
import com.bajiezu.cloud.product.dal.dto.ExpressTemplateQuery;
import com.bajiezu.cloud.product.dal.entity.ExpressTemplate;
import com.bajiezu.cloud.product.dal.entity.ExpressTemplateShippingFrom;
import com.bajiezu.cloud.product.dal.entity.ExpressTemplateShippingTo;
import com.bajiezu.cloud.product.dal.mapper.ExpressTemplateMapper;
import com.bajiezu.cloud.product.dal.mapper.ExpressTemplateShippingFromMapper;
import com.bajiezu.cloud.product.dal.mapper.ExpressTemplateShippingToMapper;
import com.bajiezu.cloud.product.service.ExpressTemplateService;
import com.bajiezu.cloud.product.util.SequenceGenerator;
import com.bajiezu.cloud.system.api.area.AreaApi;
import com.bajiezu.cloud.system.api.partner.BusinessPartnerApi;
import com.bajiezu.cloud.system.api.user.AdminUserApi;
import com.bajiezu.cloud.system.dto.AdminUserRespDTO;
import com.bajiezu.cloud.system.dto.AreaCodeAndNameDTO;
import com.bajiezu.cloud.system.dto.PartnerSimpleInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.EXPRESS_TEMPLATE_DELETED;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.EXPRESS_TEMPLATE_NOT_EXIST;

@Slf4j
@Service
public class ExpressTemplateServiceImpl implements ExpressTemplateService {

    @Resource
    private ExpressTemplateMapper expressTemplateMapper;
    @Resource
    private ExpressTemplateShippingToMapper shippingToMapper;
    @Resource
    private ExpressTemplateShippingFromMapper shippingFromMapper;
    @Resource
    private SequenceGenerator sequenceGenerator;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BusinessPartnerApi businessPartnerApi;
    @Resource
    private AreaApi areaApi;

    @Override
    public PageResult<ExpressTemplateRespVO> page(ExpressTemplateListReqVO reqVO) {
        log.info("list express template dto: {}", reqVO);
        ExpressTemplateQuery expressTemplateQuery = reqVO.convert2ExpressTemplateQuery();

        // 获取模版
        List<ExpressTemplate> expressTemplates = expressTemplateMapper.selectListByQuery(expressTemplateQuery);
        if (CollectionUtil.isEmpty(expressTemplates)) {
            return PageResult.empty();
        }
        long count = expressTemplateMapper.selectCountByQuery(expressTemplateQuery);

        // 获取用户ids、合作商ids、模板ids
        Set<Long> templateIds = Sets.newHashSet();
        Set<Long> userIds = Sets.newHashSet();
        Set<Long> partnerIds = Sets.newHashSet();
        for (ExpressTemplate expressTemplate : expressTemplates) {
            templateIds.add(expressTemplate.getId());
            userIds.add(expressTemplate.getCreateBy());
            userIds.add(expressTemplate.getUpdateBy());
            partnerIds.add(expressTemplate.getPartnerId());
        }

        // 获取模板对应的发货地区
        List<ExpressTemplateShippingTo> shippingTos = shippingToMapper.selectListByTemplateIds(templateIds);
        Map<Long, Set<String>> templateId2AreaCodes = shippingTos.stream().collect(Collectors.groupingBy(ExpressTemplateShippingTo::getExpressTemplateId,
                Collectors.mapping(ExpressTemplateShippingTo::getAreaCode, Collectors.toSet())));
        Set<String> areaCodes = templateId2AreaCodes.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

        CommonResult<List<AdminUserRespDTO>> users = adminUserApi.getUserList(userIds);
        Map<Long, String> userId2NameMap = Maps.newHashMap();
        if (users.isSuccess() && CollectionUtil.isNotEmpty(users.getData())) {
            userId2NameMap = users.getData().stream().collect(Collectors.toMap(AdminUserRespDTO::getId, AdminUserRespDTO::getName));
        }

        CommonResult<List<PartnerSimpleInfo>> partners = businessPartnerApi.getByIds(partnerIds);
        Map<Long, PartnerSimpleInfo> partnerId2PartnerMap = Maps.newHashMap();
        if (partners.isSuccess() && CollectionUtil.isNotEmpty(partners.getData())) {
            partnerId2PartnerMap = partners.getData().stream().collect(Collectors.toMap(PartnerSimpleInfo::getPartnerId, Function.identity()));
        }

        CommonResult<List<AreaCodeAndNameDTO>> areas = areaApi.getByAreaCodes(areaCodes);
        Map<String, String> areaCode2NameMap = Maps.newHashMap();
        if (areas.isSuccess() && CollectionUtil.isNotEmpty(areas.getData())) {
            areaCode2NameMap = areas.getData().stream().collect(Collectors.toMap(AreaCodeAndNameDTO::getCode, AreaCodeAndNameDTO::getName));
        }

        // 构造返回结果
        List<ExpressTemplateRespVO> expressTemplateRespVOS = Lists.newArrayList();
        for (ExpressTemplate expressTemplate : expressTemplates) {
            ExpressTemplateRespVO expressTemplateRespVO = new ExpressTemplateRespVO();
            expressTemplateRespVOS.add(expressTemplateRespVO);

            expressTemplateRespVO.setId(expressTemplate.getId());
            expressTemplateRespVO.setCode(expressTemplate.getCode());
            expressTemplateRespVO.setName(expressTemplate.getTemplateName());
            expressTemplateRespVO.setRemark(expressTemplate.getRemark());
            if (templateId2AreaCodes.containsKey(expressTemplate.getId())) {
                Set<String> shippingFromAreaCodes = templateId2AreaCodes.get(expressTemplate.getId());
                Set<String> shippingFromAreaNames = shippingFromAreaCodes.stream().map(areaCode2NameMap::get).collect(Collectors.toSet());

                List<AreaCodeAndNameVO> shippingFroms = Lists.newArrayList();
                for (String shippingFromAreaCode : shippingFromAreaCodes) {
                    AreaCodeAndNameVO shippingFrom = new AreaCodeAndNameVO();
                    shippingFroms.add(shippingFrom);
                    shippingFrom.setAreaCode(shippingFromAreaCode);
                    shippingFrom.setAreaName(areaCode2NameMap.get(shippingFromAreaCode));
                }
                expressTemplateRespVO.setShippingFroms(shippingFroms);
            }
            expressTemplateRespVO.setExpressServiceType(expressTemplate.getExpressServiceType());
            expressTemplateRespVO.setPostageType(expressTemplate.getPostageType());
            expressTemplateRespVO.setDefaultShippingCost(expressTemplate.getDefaultShippingCost());
            expressTemplateRespVO.setStatus(expressTemplate.getStatus());
            expressTemplateRespVO.setPartnerId(expressTemplate.getPartnerId());
            expressTemplateRespVO.setPartnerName(partnerId2PartnerMap.get(expressTemplate.getPartnerId()).getPartnerName());
            expressTemplateRespVO.setCreatorName(userId2NameMap.get(expressTemplate.getCreateBy()));
            expressTemplateRespVO.setCreateTime(expressTemplate.getCreateTime());
            expressTemplateRespVO.setUpdaterName(userId2NameMap.get(expressTemplate.getUpdateBy()));
            expressTemplateRespVO.setUpdateTime(expressTemplate.getUpdateTime());

        }
        return new PageResult<>(expressTemplateRespVOS, count);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ExpressTemplateAddReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("add dto: {},operatorId:{}", reqVO, loginUser.getId());

        // 构建并保存快递模版
        Date now = new Date();
        ExpressTemplate expressTemplate = buildExpressTemplate(reqVO, loginUser, now);
        expressTemplateMapper.insert(expressTemplate);

        // 构建并保存发货地区
        if (CollectionUtil.isNotEmpty(reqVO.getShippingFroms())) {
            List<ExpressTemplateShippingFrom> shippingFroms = buildShippingFroms(expressTemplate.getId(),
                    reqVO.getShippingFroms(), now, loginUser);
            shippingFromMapper.insertBatch(shippingFroms);
        }

        // 构建并保存收货地区
        if (CollectionUtil.isNotEmpty(reqVO.getShippingTos())) {
            List<ExpressTemplateShippingTo> shippingTos = buildShippingTos(expressTemplate.getId(), reqVO.getShippingTos(), now, loginUser);
            shippingToMapper.insertBatch(shippingTos);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mod(ExpressTemplateModReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("mod dto: {},operatorId:{}", reqVO, loginUser.getId());
        ExpressTemplate expressTemplate = expressTemplateMapper.selectById(reqVO.getId());
        if (expressTemplate == null) {
            throw exception(EXPRESS_TEMPLATE_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(expressTemplate.getIsDeleted())) {
            throw exception(EXPRESS_TEMPLATE_DELETED);
        }

        // 1、更新模版信息
        Date now = new Date();
        expressTemplate.setTemplateName(reqVO.getName());
        expressTemplate.setRemark(reqVO.getRemark());
        expressTemplate.setExpressServiceType(reqVO.getExpressServiceType());
        expressTemplate.setPostageType(reqVO.getPostageType());
        expressTemplate.setDefaultShippingCost(reqVO.getDefaultShippingCost());
        expressTemplate.setStatus(reqVO.getStatus());
        expressTemplate.setUpdateTime(now);
        expressTemplateMapper.updateById(expressTemplate);

        // 2、处理发货地区
        Set<String> existShippingFromAreaCodes = shippingFromMapper.selectAreaCodesByTemplateId(expressTemplate.getId());
        // 需要新增的属性值
        Set<String> addShippingAreaCodes = reqVO.getShippingFroms().stream().filter(value -> !existShippingFromAreaCodes.contains(value))
                .collect(Collectors.toSet());
        if (CollectionUtil.isNotEmpty(addShippingAreaCodes)) {
            List<ExpressTemplateShippingFrom> shippingFroms = buildShippingFroms(expressTemplate.getId(),
                    reqVO.getShippingFroms(), now, loginUser);
            shippingFromMapper.insertBatch(shippingFroms);
        }
        // 需要删除的发货地区
        Set<String> delShippingAreaCodes = existShippingFromAreaCodes.stream().filter(value -> !reqVO.getShippingFroms().contains(value))
                .collect(Collectors.toSet());
        if (CollectionUtil.isNotEmpty(delShippingAreaCodes)) {
            shippingFromMapper.logicDelByTemplateIdAndAreaCodes(expressTemplate.getId(), delShippingAreaCodes, loginUser.getId(), now);
        }

        // 3、处理收货地区
        List<ExpressTemplateShippingTo> existShippingTos = shippingToMapper.selectByTemplateId(expressTemplate.getId());
        Map<String, ExpressTemplateShippingTo> existShippingToMap = existShippingTos.stream().collect(Collectors.toMap(
                ExpressTemplateShippingTo::getAreaCode, value -> value));
        Map<String, AreaCodeAndNameVO> areaCodeToShippingToMap = reqVO.getShippingTos().stream().collect(Collectors.toMap(
                AreaCodeAndNameVO::getAreaCode, value -> value));

        Set<String> deleteShippingToAreaCodes = Sets.newHashSet();
        List<AreaCodeAndNameVO> addShippingTos = Lists.newArrayList();
        List<ExpressTemplateShippingTo> updateShippingTos = Lists.newArrayList();
        for (AreaCodeAndNameVO areaCodeAndNameVO : reqVO.getShippingTos()) {
            ExpressTemplateShippingTo shippingTo = existShippingToMap.get(areaCodeAndNameVO.getAreaCode());
            if (shippingTo == null) {
                addShippingTos.add(areaCodeAndNameVO);
            } else {
                if (areaCodeAndNameVO.getShippingCost().equals(shippingTo.getShippingCost())) {
                    shippingTo.setShippingCost(areaCodeAndNameVO.getShippingCost());
                    shippingTo.setUpdateBy(loginUser.getId());
                    shippingTo.setUpdateTime(now);
                    updateShippingTos.add(shippingTo);
                }
            }
        }
        for (ExpressTemplateShippingTo shippingTo : existShippingTos) {
            if (!areaCodeToShippingToMap.containsKey(shippingTo.getAreaCode())) {
                deleteShippingToAreaCodes.add(shippingTo.getAreaCode());
            }
        }

        if (CollectionUtil.isNotEmpty(deleteShippingToAreaCodes)) {
            shippingToMapper.logicDelByTemplateIdAndAreaCodes(expressTemplate.getId(), deleteShippingToAreaCodes, loginUser.getId(), now);
        }

        if (CollectionUtil.isNotEmpty(addShippingTos)) {
            List<ExpressTemplateShippingTo> shippingTos = buildShippingTos(expressTemplate.getId(), addShippingTos, now, loginUser);
            shippingToMapper.insertBatch(shippingTos);
        }

        if (CollectionUtil.isNotEmpty(updateShippingTos)) {
            shippingToMapper.updateBatch(updateShippingTos);
        }
    }

    @Override
    public ExpressTemplateRespVO detail(Long id) {
        // 获取模板
        ExpressTemplate expressTemplate = expressTemplateMapper.selectById(id);
        if (expressTemplate == null) {
            throw exception(EXPRESS_TEMPLATE_NOT_EXIST);
        }
        // 获取发货地区
        Set<String> shippingFromAreaCodes = shippingFromMapper.selectAreaCodesByTemplateId(expressTemplate.getId());

        // 获取收货地区
        List<ExpressTemplateShippingTo> shippingTos = shippingToMapper.selectByTemplateId(expressTemplate.getId());

        // 获取所有的areaCodes
        Set<String> areaCodes = Sets.newHashSet();
        if (CollectionUtil.isNotEmpty(shippingFromAreaCodes)) {
            areaCodes.addAll(shippingFromAreaCodes);
        }
        if (CollectionUtil.isNotEmpty(shippingTos)) {
            areaCodes.addAll(shippingTos.stream().map(ExpressTemplateShippingTo::getAreaCode).collect(Collectors.toSet()));
        }

        CommonResult<List<AreaCodeAndNameDTO>> areas = areaApi.getByAreaCodes(areaCodes);
        Map<String, String> areaCode2NameMap = Maps.newHashMap();
        if (areas.isSuccess() && CollectionUtil.isNotEmpty(areas.getData())) {
            areaCode2NameMap = areas.getData().stream().collect(Collectors.toMap(AreaCodeAndNameDTO::getCode, AreaCodeAndNameDTO::getName));
        }

        ExpressTemplateRespVO expressTemplateRespVO = new ExpressTemplateRespVO();
        expressTemplateRespVO.setId(expressTemplate.getId());
        expressTemplateRespVO.setCode(expressTemplate.getCode());
        expressTemplateRespVO.setName(expressTemplate.getTemplateName());
        expressTemplateRespVO.setRemark(expressTemplate.getRemark());
        if (CollectionUtil.isNotEmpty(shippingFromAreaCodes)) {
            List<AreaCodeAndNameVO> shippingFroms = Lists.newArrayList();
            for (String areaCode : shippingFromAreaCodes) {
                AreaCodeAndNameVO vo = new AreaCodeAndNameVO();
                vo.setAreaCode(areaCode);
                vo.setAreaName(areaCode2NameMap.get(areaCode));
                shippingFroms.add(vo);
            }
            expressTemplateRespVO.setShippingFroms(shippingFroms);
        }
        expressTemplateRespVO.setExpressServiceType(expressTemplate.getExpressServiceType());
        expressTemplateRespVO.setPostageType(expressTemplate.getPostageType());
        expressTemplateRespVO.setDefaultShippingCost(expressTemplate.getDefaultShippingCost());
        expressTemplateRespVO.setStatus(expressTemplate.getStatus());
        if (CollectionUtil.isNotEmpty(shippingTos)) {
            List<AreaCodeAndNameVO> areaCodeAndNameVOS = Lists.newArrayList();
            for (ExpressTemplateShippingTo shippingTo : shippingTos) {
                AreaCodeAndNameVO vo = new AreaCodeAndNameVO();
                areaCodeAndNameVOS.add(vo);
                vo.setAreaCode(shippingTo.getAreaCode());
                vo.setAreaName(areaCode2NameMap.get(shippingTo.getAreaCode()));
                vo.setShippingCost(shippingTo.getShippingCost());
            }
            expressTemplateRespVO.setShippingTos(areaCodeAndNameVOS);
        }
        return expressTemplateRespVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void del(Long id) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("del id: {},operatorId:{}", id, loginUser.getId());
        ExpressTemplate expressTemplate = expressTemplateMapper.selectById(id);
        if (expressTemplate == null) {
            throw exception(EXPRESS_TEMPLATE_NOT_EXIST);
        }

        Date now = new Date();
        expressTemplate.setUpdateBy(loginUser.getId());
        expressTemplate.setUpdateTime(now);
        expressTemplate.setIsDeleted(NumberUtils.INTEGER_ONE);
        expressTemplateMapper.updateById(expressTemplate);
        shippingFromMapper.logicDelByTemplateId(id, loginUser.getId(), now);
        shippingToMapper.logicDelByTemplateId(id, loginUser.getId(), now);
    }

    @Override
    public void changeStatus(StatusChangeReqVo reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("changeStatus: {},operatorId:{}", reqVO, loginUser.getId());
        ExpressTemplate expressTemplate = expressTemplateMapper.selectById(reqVO.getId());
        if (expressTemplate == null) {
            throw exception(EXPRESS_TEMPLATE_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(expressTemplate.getIsDeleted())) {
            throw exception(EXPRESS_TEMPLATE_DELETED);
        }

        if (Objects.equals(expressTemplate.getStatus(), reqVO.getStatus())) {
            return;
        }

        expressTemplate.setStatus(reqVO.getStatus());
        expressTemplate.setUpdateTime(new Date());
        expressTemplate.setUpdateBy(loginUser.getId());
        expressTemplateMapper.updateById(expressTemplate);
    }

    private ExpressTemplate buildExpressTemplate(ExpressTemplateAddReqVO reqVO, LoginUser<?> loginUser, Date now) {
        ExpressTemplate expressTemplate = new ExpressTemplate();
        String code = sequenceGenerator.getExpressTemplateSequence();
        expressTemplate.setCode(code);
        expressTemplate.setTemplateName(reqVO.getName());
        expressTemplate.setExpressServiceType(reqVO.getExpressServiceType());
        expressTemplate.setPostageType(reqVO.getPostageType());
        expressTemplate.setDefaultShippingCost(reqVO.getDefaultShippingCost());
        expressTemplate.setRemark(reqVO.getRemark());
        expressTemplate.setPartnerId(loginUser.getPartnerId());
        expressTemplate.setCreateTime(now);
        expressTemplate.setUpdateTime(now);
        expressTemplate.setCreateBy(loginUser.getId());
        expressTemplate.setUpdateBy(loginUser.getId());
        expressTemplate.setIsDeleted(0);
        return expressTemplate;
    }

    private List<ExpressTemplateShippingFrom> buildShippingFroms(Long expressTemplateId, Set<String> areaCodes,
                                                                 Date now, LoginUser<?> loginUser) {
        List<ExpressTemplateShippingFrom> shippingFroms = Lists.newArrayList();
        for (String areaCode : areaCodes) {
            ExpressTemplateShippingFrom shippingFrom = new ExpressTemplateShippingFrom();
            shippingFroms.add(shippingFrom);
            shippingFrom.setExpressTemplateId(expressTemplateId);
            shippingFrom.setAreaCode(areaCode);
            shippingFrom.setPartnerId(loginUser.getPartnerId());
            shippingFrom.setCreateTime(now);
            shippingFrom.setUpdateTime(now);
            shippingFrom.setCreateBy(loginUser.getId());
            shippingFrom.setUpdateBy(loginUser.getId());
            shippingFrom.setIsDeleted(0);
        }
        return shippingFroms;
    }

    private List<ExpressTemplateShippingTo> buildShippingTos(Long expressTemplateId, List<AreaCodeAndNameVO> areaCodeAndNameVOS,
                                                             Date now, LoginUser<?> loginUser) {
        List<ExpressTemplateShippingTo> shippingTos = Lists.newArrayList();
        for (AreaCodeAndNameVO areaCodeAndNameVO : areaCodeAndNameVOS) {
            ExpressTemplateShippingTo shippingTo = new ExpressTemplateShippingTo();
            shippingTos.add(shippingTo);
            shippingTo.setExpressTemplateId(expressTemplateId);
            shippingTo.setAreaCode(areaCodeAndNameVO.getAreaCode());
            shippingTo.setShippingCost(areaCodeAndNameVO.getShippingCost());
            shippingTo.setPartnerId(loginUser.getPartnerId());
            shippingTo.setCreateTime(now);
            shippingTo.setUpdateTime(now);
            shippingTo.setCreateBy(loginUser.getId());
            shippingTo.setUpdateBy(loginUser.getId());
            shippingTo.setIsDeleted(0);
        }
        return shippingTos;
    }
}
