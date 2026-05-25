package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.*;
import com.bajiezu.cloud.product.dal.entity.*;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.service.AssetPricingConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.*;

@Service
public class AssetPricingConfigServiceImpl implements AssetPricingConfigService {
    @Resource private StandardProductSkuMapper skuMapper;
    @Resource private AssetPricingConfigMapper pricingConfigMapper;
    @Resource private AssetResidualConfigMapper residualConfigMapper;
    @Resource private AssetResidualYearConfigMapper residualYearConfigMapper;

    @Override
    public AssetPricingConfigDetailRespVO detail(AssetPricingConfigQueryReqVO reqVO) {
        List<AssetPricingConfig> list = pricingConfigMapper.selectBySkuId(reqVO.getStandardProductSkuId(), reqVO.getPartnerId());
        AssetResidualConfig residualConfig = residualConfigMapper.selectBySkuId(reqVO.getStandardProductSkuId(), reqVO.getPartnerId());
        Map<Integer, AssetResidualYearConfig> yearMap = residualConfig == null ? Collections.emptyMap() : residualYearConfigMapper.selectByConfigId(residualConfig.getId(), reqVO.getPartnerId()).stream().collect(Collectors.toMap(AssetResidualYearConfig::getUseYear, y -> y));
        Map<String, AssetPricingConfig> confMap = list.stream().collect(Collectors.toMap(c -> c.getLeaseMode() + "_" + c.getUseYear(), c -> c));
        AssetPricingConfigDetailRespVO resp = new AssetPricingConfigDetailRespVO();
        resp.setPartnerId(reqVO.getPartnerId()); resp.setStandardSpuId(reqVO.getStandardSpuId()); resp.setStandardProductSkuId(reqVO.getStandardProductSkuId());
        resp.setLeaseModeConfigs(Arrays.asList(buildLeaseModeVO(1, confMap, residualConfig, yearMap), buildLeaseModeVO(2, confMap, residualConfig, yearMap)));
        return resp;
    }

    private AssetPricingConfigDetailRespVO.LeaseModeConfigVO buildLeaseModeVO(Integer leaseMode, Map<String, AssetPricingConfig> confMap, AssetResidualConfig residualConfig, Map<Integer, AssetResidualYearConfig> yearMap) {
        AssetPricingConfigDetailRespVO.LeaseModeConfigVO vo = new AssetPricingConfigDetailRespVO.LeaseModeConfigVO(); vo.setLeaseMode(leaseMode);
        List<AssetPricingYearConfigVO> years = new ArrayList<>();
        for (int y = 1; y <= 3; y++) {
            AssetPricingConfig c = confMap.get(leaseMode + "_" + y); AssetPricingYearConfigVO v = new AssetPricingYearConfigVO(); v.setUseYear(y);
            if (c != null) {v.setDeviceValue(c.getDeviceValue());v.setAnnualDepreciationAmount(c.getAnnualDepreciationAmount());v.setDeviceTotalPriceCoefficient(c.getDeviceTotalPriceCoefficient());v.setDeviceTotalPrice(c.getDeviceTotalPrice());v.setTotalRentCoefficient(c.getTotalRentCoefficient());v.setTotalRent(c.getTotalRent());v.setMonthlyRent(c.getMonthlyRent());v.setDailyRent(c.getDailyRent());v.setExpirationPurchaseAmount(c.getExpirationPurchaseAmount());applyRisk(v, yearMap.get(y));}
            else {fillDefault(v, y, residualConfig, yearMap);}
            years.add(v);
        }
        vo.setYearConfigs(years); return vo;
    }
    private void fillDefault(AssetPricingYearConfigVO v, int y, AssetResidualConfig residualConfig, Map<Integer, AssetResidualYearConfig> yearMap){
        AssetResidualYearConfig yc=yearMap.get(y); if(y==1&&residualConfig!=null) v.setDeviceValue(residualConfig.getOfficialPrice()); else if(yc!=null) v.setDeviceValue(yc.getYearBeginValue());
        if(yc!=null) v.setAnnualDepreciationAmount(yc.getYearDepreciationAmount()); applyRisk(v,yc);
    }
    private void applyRisk(AssetPricingYearConfigVO v, AssetResidualYearConfig y){
        if(y==null||v.getMonthlyRent()==null||v.getExpirationPurchaseAmount()==null) return;
        boolean risk=v.getMonthlyRent().multiply(BigDecimal.valueOf(12)).add(v.getExpirationPurchaseAmount()).compareTo(y.getYearEndResidualValue())<0;
        v.setRiskWarning(risk); if(risk) v.setRiskWarningMsg("当前定价低于设备残值，存在亏损风险，请确认");
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void save(AssetPricingConfigSaveReqVO reqVO) {
        LoginUser<?> u = SecurityFrameworkUtils.getLoginUser();
        if (reqVO.getConfigs() == null || reqVO.getConfigs().isEmpty()) throw exception(ASSET_PRICING_PARAM_INVALID);
        if (skuMapper.selectListByStandardSpuId(reqVO.getStandardSpuId()).stream().noneMatch(s -> Objects.equals(s.getId(), reqVO.getStandardProductSkuId()))) throw exception(ASSET_PRICING_SPU_SKU_MISMATCH);
        AssetResidualConfig residualConfig = residualConfigMapper.selectBySkuId(reqVO.getStandardProductSkuId(), reqVO.getPartnerId());
        Map<Integer, AssetResidualYearConfig> yearMap = residualConfig == null ? Collections.emptyMap() : residualYearConfigMapper.selectByConfigId(residualConfig.getId(), reqVO.getPartnerId()).stream().collect(Collectors.toMap(AssetResidualYearConfig::getUseYear, y -> y));
        Date now = new Date();
        for (AssetPricingItemSaveReqVO item : reqVO.getConfigs()) {
            AssetPricingConfig po = buildAndValidate(reqVO, item, residualConfig, yearMap, u, now);
            pricingConfigMapper.logicDelBySkuAndKeys(reqVO.getStandardProductSkuId(), reqVO.getPartnerId(), item.getLeaseMode(), item.getUseYear(), u.getId(), now);
            pricingConfigMapper.insert(po);
        }
    }

    private AssetPricingConfig buildAndValidate(AssetPricingConfigSaveReqVO reqVO, AssetPricingItemSaveReqVO item, AssetResidualConfig residualConfig, Map<Integer, AssetResidualYearConfig> yearMap, LoginUser<?> u, Date now) {
        AssetResidualYearConfig y = yearMap.get(item.getUseYear());
        BigDecimal deviceValue = item.getUseYear() == 1 ? (residualConfig == null ? null : residualConfig.getOfficialPrice()) : (y == null ? null : y.getYearBeginValue());
        if (deviceValue == null || (item.getUseYear() > 1 && y == null)) throw exception(ASSET_PRICING_RESIDUAL_VALUE_REQUIRED);
        if (item.getDeviceTotalPriceCoefficient().compareTo(BigDecimal.ZERO) <= 0) throw exception(ASSET_PRICING_TOTAL_PRICE_COEFFICIENT_INVALID);
        BigDecimal deviceTotalPrice = deviceValue.multiply(item.getDeviceTotalPriceCoefficient()).setScale(2, RoundingMode.HALF_UP);
        if (deviceTotalPrice.compareTo(BigDecimal.ZERO) <= 0) throw exception(ASSET_PRICING_TOTAL_PRICE_INVALID);
        if (y != null && y.getTotalPriceLowerLimit() != null && deviceTotalPrice.compareTo(y.getTotalPriceLowerLimit()) < 0) throw exception(ASSET_PRICING_TOTAL_PRICE_TOO_LOW);
        if (y != null && y.getTotalPriceUpperLimit() != null && deviceTotalPrice.compareTo(y.getTotalPriceUpperLimit()) > 0) throw exception(ASSET_PRICING_TOTAL_PRICE_TOO_HIGH);
        if (item.getTotalRentCoefficient().compareTo(BigDecimal.ZERO) < 0) throw exception(ASSET_PRICING_TOTAL_RENT_COEFFICIENT_INVALID);
        BigDecimal totalRent = deviceTotalPrice.multiply(item.getTotalRentCoefficient()).setScale(2, RoundingMode.HALF_UP);
        if (totalRent.compareTo(BigDecimal.ZERO) < 0) throw exception(ASSET_PRICING_TOTAL_RENT_INVALID);
        BigDecimal monthlyRent = totalRent.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal dailyRent = totalRent.divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);
        if (monthlyRent.compareTo(BigDecimal.ZERO) < 0 || dailyRent.compareTo(BigDecimal.ZERO) < 0) throw exception(ASSET_PRICING_MONTHLY_OR_DAILY_RENT_INVALID);
        BigDecimal annualDep = y == null ? BigDecimal.ZERO : y.getYearDepreciationAmount();
        BigDecimal expiration = deviceTotalPrice.subtract(annualDep).setScale(2, RoundingMode.HALF_UP);
        if (expiration.compareTo(BigDecimal.ZERO) < 0) throw exception(ASSET_PRICING_EXPIRATION_PURCHASE_INVALID);
        AssetPricingConfig po = new AssetPricingConfig();
        po.setPartnerId(reqVO.getPartnerId()); po.setStandardSpuId(reqVO.getStandardSpuId()); po.setStandardProductSkuId(reqVO.getStandardProductSkuId()); po.setUseYear(item.getUseYear()); po.setLeaseMode(item.getLeaseMode()); po.setDeviceValue(deviceValue.setScale(2, RoundingMode.HALF_UP)); po.setDeviceTotalPriceCoefficient(item.getDeviceTotalPriceCoefficient().setScale(4, RoundingMode.HALF_UP)); po.setDeviceTotalPrice(deviceTotalPrice); po.setTotalRentCoefficient(item.getTotalRentCoefficient().setScale(4, RoundingMode.HALF_UP)); po.setTotalRent(totalRent); po.setMonthlyRent(monthlyRent); po.setDailyRent(dailyRent); po.setAnnualDepreciationAmount(annualDep.setScale(2, RoundingMode.HALF_UP)); po.setExpirationPurchaseAmount(expiration); po.setResidualValueConfigId(residualConfig == null ? null : residualConfig.getId()); po.setPricingSource(1); po.setStatus(1); po.setRemark(item.getRemark()); po.setCreateBy(u.getId()); po.setUpdateBy(u.getId()); po.setCreateTime(now); po.setUpdateTime(now); po.setIsDeleted(0);
        return po;
    }
}
