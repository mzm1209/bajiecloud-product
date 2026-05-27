package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.*;
import com.bajiezu.cloud.product.dal.entity.*;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.enums.AssetResidualConstants;
import com.bajiezu.cloud.product.service.AssetResidualConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.AssetResidualErrorCodeConstants.*;

@Service
public class AssetResidualConfigServiceImpl implements AssetResidualConfigService {
    @Resource private StandardProductSkuMapper skuMapper;
    @Resource private StandardProductSpuMapper spuMapper;
    @Resource private AssetResidualConfigMapper configMapper;
    @Resource private AssetResidualYearConfigMapper yearMapper;
    @Resource private AssetResidualMonthConfigMapper monthMapper;

    public AssetResidualConfigDetailRespVO detail(AssetResidualConfigQueryReqVO reqVO){
        LoginUser<?> u= SecurityFrameworkUtils.getLoginUser();
        List<StandardProductSku> skus=skuMapper.selectListByStandardSpuId(reqVO.getStandardSpuId());
        AssetResidualConfigDetailRespVO resp=new AssetResidualConfigDetailRespVO();
        resp.setStandardSpuId(reqVO.getStandardSpuId());
        StandardProductSpu spu=spuMapper.selectById(reqVO.getStandardSpuId()); if(spu!=null) resp.setSpuName(spu.getName());
        resp.setSkuList(skus.stream().map(s->{AssetResidualConfigDetailRespVO.SkuSimpleVO v=new AssetResidualConfigDetailRespVO.SkuSimpleVO();v.setSkuId(s.getId());v.setSkuCode(s.getSkuCode());return v;}).toList());
        if(skus.isEmpty()) return resp;
        Long skuId=reqVO.getStandardProductSkuId()==null?skus.get(0).getId():reqVO.getStandardProductSkuId();
        resp.setStandardProductSkuId(skuId);
        AssetResidualConfig c=configMapper.selectBySkuId(skuId,u.getPartnerId());
        if(c==null){ resp.setYearConfigs(emptyYears()); resp.setMonthConfigs(emptyMonths()); return resp; }
        resp.setOfficialPrice(c.getOfficialPrice()); resp.setDepreciationRuleType(c.getDepreciationRuleType()); resp.setDepreciationRuleSubType(c.getDepreciationRuleSubType()); resp.setRemark(c.getRemark()); resp.setStatus(c.getStatus());
        resp.setYearConfigs(yearMapper.selectByConfigId(c.getId(),u.getPartnerId()).stream().map(this::toYearVO).toList());
        resp.setMonthConfigs(monthMapper.selectByConfigId(c.getId(),u.getPartnerId()).stream().map(this::toMonthVO).toList());
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(AssetResidualConfigSaveReqVO reqVO){
        LoginUser<?> u=SecurityFrameworkUtils.getLoginUser();
        validate(reqVO);
        List<StandardProductSku> skus=skuMapper.selectListByStandardSpuId(reqVO.getStandardSpuId());
        if(skus.stream().noneMatch(s->Objects.equals(s.getId(),reqVO.getStandardProductSkuId()))) throw exception(ASSET_RESIDUAL_SPU_SKU_MISMATCH);
        Date now=new Date();
        AssetResidualConfig old=configMapper.selectBySkuId(reqVO.getStandardProductSkuId(),u.getPartnerId());
        if(old!=null){ monthMapper.logicDelByConfigId(old.getId(),u.getPartnerId(),u.getId(),now); yearMapper.logicDelByConfigId(old.getId(),u.getPartnerId(),u.getId(),now); configMapper.logicDelBySkuId(reqVO.getStandardProductSkuId(),u.getPartnerId(),u.getId(),now);}        
        AssetResidualConfig c=new AssetResidualConfig(); c.setPartnerId(u.getPartnerId()); c.setStandardSpuId(reqVO.getStandardSpuId()); c.setStandardProductSkuId(reqVO.getStandardProductSkuId()); c.setOfficialPrice(reqVO.getOfficialPrice()); c.setDepreciationRuleType(reqVO.getDepreciationRuleType()); c.setDepreciationRuleSubType(reqVO.getDepreciationRuleSubType()); c.setRemark(reqVO.getRemark()); c.setStatus(1); c.setCreateBy(u.getId()); c.setUpdateBy(u.getId()); c.setCreateTime(now); c.setUpdateTime(now); c.setIsDeleted(0); configMapper.insert(c);
        Map<Integer,AssetResidualYearConfigVO> years=reqVO.getYearConfigs().stream().collect(Collectors.toMap(AssetResidualYearConfigVO::getUseYear,y->y));
        Map<Integer,AssetResidualMonthConfigVO> months=reqVO.getMonthConfigs().stream().collect(Collectors.toMap(AssetResidualMonthConfigVO::getGlobalMonth,m->m));
        BigDecimal accum=BigDecimal.ZERO; BigDecimal lastYearEnd=toAmountDecimal(reqVO.getOfficialPrice());
        List<AssetResidualMonthConfig> ml=new ArrayList<>();
        for(int y=1;y<=3;y++){
            BigDecimal yearBegin=(y==1)?toAmountDecimal(reqVO.getOfficialPrice()):lastYearEnd; BigDecimal yearDep=BigDecimal.ZERO;
            for(int m=1;m<=12;m++){
                int g=(y-1)*12+m; AssetResidualMonthConfigVO in=months.get(g); BigDecimal begin=(m==1)?yearBegin:toAmountDecimal(ml.get(ml.size()-1).getResidualValue());
                BigDecimal dep=calcDep(reqVO.getDepreciationRuleType(),begin,in.getDepreciationRuleValue()); BigDecimal residual=begin.subtract(dep).setScale(2,RoundingMode.HALF_UP); if(residual.compareTo(BigDecimal.ZERO)<0) throw exception(ASSET_RESIDUAL_PARAM_INVALID);
                accum=accum.add(dep).setScale(2,RoundingMode.HALF_UP);
                AssetResidualMonthConfig mo=new AssetResidualMonthConfig(); mo.setAssetResidualConfigId(c.getId()); mo.setAssetResidualYearConfigId(null); mo.setPartnerId(u.getPartnerId()); mo.setUseYear(y); mo.setUseMonth(m); mo.setGlobalMonth(g); mo.setDepreciationRuleValue(in.getDepreciationRuleValue()); mo.setBeginValue(toAmountLong(begin)); mo.setDepreciationAmount(toAmountLong(dep)); mo.setResidualValue(toAmountLong(residual)); mo.setAccumulatedDepreciationAmount(toAmountLong(accum)); mo.setCurrentPurchaseAmount(toAmountLong(begin.subtract(accum))); mo.setCreateBy(u.getId()); mo.setUpdateBy(u.getId()); mo.setCreateTime(now); mo.setUpdateTime(now); mo.setIsDeleted(0); ml.add(mo); yearDep=yearDep.add(dep);
            }
            AssetResidualYearConfigVO yin=years.get(y); AssetResidualYearConfig yo=new AssetResidualYearConfig(); yo.setAssetResidualConfigId(c.getId()); yo.setPartnerId(u.getPartnerId()); yo.setUseYear(y); yo.setTotalPriceUpperCoefficient(yin.getTotalPriceUpperCoefficient()); yo.setTotalPriceLowerCoefficient(yin.getTotalPriceLowerCoefficient()); yo.setYearBeginValue(toAmountLong(yearBegin)); yo.setYearDepreciationAmount(toAmountLong(yearDep)); yo.setYearEndResidualValue(toAmountLong(yearBegin.subtract(yearDep))); yo.setTotalPriceUpperLimit(toAmountLong(yearBegin.multiply(yin.getTotalPriceUpperCoefficient()))); yo.setTotalPriceLowerLimit(toAmountLong(yearBegin.multiply(yin.getTotalPriceLowerCoefficient()))); yo.setCreateBy(u.getId()); yo.setUpdateBy(u.getId()); yo.setCreateTime(now); yo.setUpdateTime(now); yo.setIsDeleted(0); yearMapper.insert(yo); lastYearEnd=toAmountDecimal(yo.getYearEndResidualValue());
            for (AssetResidualMonthConfig mo : ml) {
                if (mo.getUseYear() == y && mo.getAssetResidualYearConfigId() == null) {
                    mo.setAssetResidualYearConfigId(yo.getId());
                }
            }
        }
        monthMapper.insertBatch(ml);
    }
    private static final BigDecimal AMOUNT_SCALE = BigDecimal.valueOf(10000);
    private BigDecimal toAmountDecimal(Long v){ return v==null?null:BigDecimal.valueOf(v).divide(AMOUNT_SCALE,4,RoundingMode.HALF_UP); }
    private Long toAmountLong(BigDecimal v){ return v==null?null:v.multiply(AMOUNT_SCALE).setScale(0,RoundingMode.HALF_UP).longValue(); }

    private void validate(AssetResidualConfigSaveReqVO r){ if(r.getYearConfigs().size()!=3||r.getMonthConfigs().size()!=36) throw exception(ASSET_RESIDUAL_PARAM_INVALID); }
    private BigDecimal calcDep(Integer t,BigDecimal begin,BigDecimal v){ return AssetResidualCalculator.depreciationAmount(t, begin, v); }
    private List<AssetResidualYearConfigVO> emptyYears(){ List<AssetResidualYearConfigVO> l=new ArrayList<>(); for(int i=1;i<=3;i++){AssetResidualYearConfigVO v=new AssetResidualYearConfigVO();v.setUseYear(i);l.add(v);} return l; }
    private List<AssetResidualMonthConfigVO> emptyMonths(){ List<AssetResidualMonthConfigVO> l=new ArrayList<>(); for(int g=1;g<=36;g++){AssetResidualMonthConfigVO v=new AssetResidualMonthConfigVO();v.setGlobalMonth(g);v.setUseYear((g-1)/12+1);v.setUseMonth((g-1)%12+1);l.add(v);} return l; }
    private AssetResidualYearConfigVO toYearVO(AssetResidualYearConfig y){ AssetResidualYearConfigVO v=new AssetResidualYearConfigVO(); v.setUseYear(y.getUseYear()); v.setTotalPriceUpperCoefficient(y.getTotalPriceUpperCoefficient()); v.setTotalPriceLowerCoefficient(y.getTotalPriceLowerCoefficient()); v.setYearBeginValue(y.getYearBeginValue()); v.setYearDepreciationAmount(y.getYearDepreciationAmount()); v.setYearEndResidualValue(y.getYearEndResidualValue()); v.setTotalPriceUpperLimit(y.getTotalPriceUpperLimit()); v.setTotalPriceLowerLimit(y.getTotalPriceLowerLimit()); return v; }
    private AssetResidualMonthConfigVO toMonthVO(AssetResidualMonthConfig m){ AssetResidualMonthConfigVO v=new AssetResidualMonthConfigVO(); v.setUseYear(m.getUseYear()); v.setUseMonth(m.getUseMonth()); v.setGlobalMonth(m.getGlobalMonth()); v.setDepreciationRuleValue(m.getDepreciationRuleValue()); v.setBeginValue(m.getBeginValue()); v.setDepreciationAmount(m.getDepreciationAmount()); v.setResidualValue(m.getResidualValue()); v.setAccumulatedDepreciationAmount(m.getAccumulatedDepreciationAmount()); v.setCurrentPurchaseAmount(m.getCurrentPurchaseAmount()); return v; }
}
