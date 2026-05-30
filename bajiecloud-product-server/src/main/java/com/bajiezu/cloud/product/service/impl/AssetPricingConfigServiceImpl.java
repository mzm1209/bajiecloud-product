package com.bajiezu.cloud.product.service.impl;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_EXPIRATION_PURCHASE_INVALID;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_MONTHLY_OR_DAILY_RENT_INVALID;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_PARAM_INVALID;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_RESIDUAL_VALUE_REQUIRED;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_SPU_SKU_MISMATCH;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_TOTAL_PRICE_COEFFICIENT_INVALID;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_TOTAL_PRICE_INVALID;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_TOTAL_PRICE_TOO_HIGH;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_TOTAL_PRICE_TOO_LOW;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_TOTAL_RENT_COEFFICIENT_INVALID;
import static com.bajiezu.cloud.product.enums.AssetPricingErrorCodeConstants.ASSET_PRICING_TOTAL_RENT_INVALID;

import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.AssetPricingConfigQueryReqVO;
import com.bajiezu.cloud.product.controller.vo.request.AssetPricingConfigSaveReqVO;
import com.bajiezu.cloud.product.controller.vo.request.AssetPricingItemSaveReqVO;
import com.bajiezu.cloud.product.controller.vo.response.AssetPricingConfigDetailRespVO;
import com.bajiezu.cloud.product.controller.vo.response.AssetPricingYearConfigVO;
import com.bajiezu.cloud.product.dal.entity.AssetPricingConfig;
import com.bajiezu.cloud.product.dal.entity.AssetResidualConfig;
import com.bajiezu.cloud.product.dal.entity.AssetResidualYearConfig;
import com.bajiezu.cloud.product.dal.mapper.AssetPricingConfigMapper;
import com.bajiezu.cloud.product.dal.mapper.AssetResidualConfigMapper;
import com.bajiezu.cloud.product.dal.mapper.AssetResidualYearConfigMapper;
import com.bajiezu.cloud.product.dal.mapper.StandardProductSkuMapper;
import com.bajiezu.cloud.product.service.AssetPricingConfigService;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AssetPricingConfigServiceImpl implements AssetPricingConfigService {

    private static final BigDecimal AMOUNT_SCALE = BigDecimal.valueOf(10000);

    @Resource private StandardProductSkuMapper skuMapper;
    @Resource private AssetPricingConfigMapper pricingConfigMapper;
    @Resource private AssetResidualConfigMapper residualConfigMapper;
    @Resource private AssetResidualYearConfigMapper residualYearConfigMapper;

    @Override
    public AssetPricingConfigDetailRespVO detail(AssetPricingConfigQueryReqVO reqVO) {
        List<AssetPricingConfig> list = pricingConfigMapper.selectBySkuId(reqVO.getStandardProductSkuId(), reqVO.getPartnerId());
        AssetResidualConfig residualConfig = residualConfigMapper.selectBySkuId(reqVO.getStandardProductSkuId(), reqVO.getPartnerId());
        Map<Integer, AssetResidualYearConfig> yearMap = residualConfig == null
                ? Collections.emptyMap()
                : residualYearConfigMapper.selectByConfigId(residualConfig.getId(), reqVO.getPartnerId())
                        .stream().collect(Collectors.toMap(AssetResidualYearConfig::getUseYear, y -> y));

        Map<String, AssetPricingConfig> confMap = list.stream()
                .collect(Collectors.toMap(c -> c.getLeaseMode() + "_" + c.getUseYear(), c -> c));

        AssetPricingConfigDetailRespVO resp = new AssetPricingConfigDetailRespVO();
        resp.setPartnerId(reqVO.getPartnerId());
        resp.setStandardSpuId(reqVO.getStandardSpuId());
        resp.setStandardProductSkuId(reqVO.getStandardProductSkuId());
        resp.setLeaseModeConfigs(Arrays.asList(
                buildLeaseModeVO(1, confMap, residualConfig, yearMap),
                buildLeaseModeVO(2, confMap, residualConfig, yearMap)));
        return resp;
    }

    private AssetPricingConfigDetailRespVO.LeaseModeConfigVO buildLeaseModeVO(
            Integer leaseMode,
            Map<String, AssetPricingConfig> confMap,
            AssetResidualConfig residualConfig,
            Map<Integer, AssetResidualYearConfig> yearMap) {
        AssetPricingConfigDetailRespVO.LeaseModeConfigVO vo = new AssetPricingConfigDetailRespVO.LeaseModeConfigVO();
        vo.setLeaseMode(leaseMode);
        List<AssetPricingYearConfigVO> years = new ArrayList<>();
        for (int y = 1; y <= 3; y++) {
            AssetPricingConfig c = confMap.get(leaseMode + "_" + y);
            AssetPricingYearConfigVO v = new AssetPricingYearConfigVO();
            v.setUseYear(y);
            if (c != null) {
                v.setDeviceValue(c.getDeviceValue());
                v.setAnnualDepreciationAmount(c.getAnnualDepreciationAmount());
                v.setDeviceTotalPriceCoefficient(c.getDeviceTotalPriceCoefficient());
                v.setDeviceTotalPrice(c.getDeviceTotalPrice());
                v.setTotalRentCoefficient(c.getTotalRentCoefficient());
                v.setTotalRent(c.getTotalRent());
                v.setMonthlyRent(c.getMonthlyRent());
                v.setDailyRent(c.getDailyRent());
                v.setExpirationPurchaseAmount(c.getExpirationPurchaseAmount());
                applyRisk(v, yearMap.get(y));
            } else {
                fillDefault(v, y, residualConfig, yearMap);
            }
            years.add(v);
        }
        vo.setYearConfigs(years);
        return vo;
    }

    private void fillDefault(AssetPricingYearConfigVO v, int y, AssetResidualConfig residualConfig, Map<Integer, AssetResidualYearConfig> yearMap) {
        AssetResidualYearConfig yc = yearMap.get(y);
        if (y == 1 && residualConfig != null) {
            v.setDeviceValue(residualConfig.getOfficialPrice());
        } else if (yc != null) {
            v.setDeviceValue(yc.getYearBeginValue());
        }
        if (yc != null) {
            v.setAnnualDepreciationAmount(yc.getYearDepreciationAmount());
        }
        applyRisk(v, yc);
    }

    private void applyRisk(AssetPricingYearConfigVO v, AssetResidualYearConfig y) {
        if (y == null || v.getMonthlyRent() == null || v.getExpirationPurchaseAmount() == null) {
            return;
        }
        boolean risk = v.getMonthlyRent() * 12 + v.getExpirationPurchaseAmount() < y.getYearEndResidualValue();
        v.setRiskWarning(risk);
        if (risk) {
            v.setRiskWarningMsg("当前定价低于设备残值，存在亏损风险，请确认");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(AssetPricingConfigSaveReqVO reqVO) {
        LoginUser<?> u = SecurityFrameworkUtils.getLoginUser();
        if (reqVO.getConfigs() == null || reqVO.getConfigs().isEmpty()) {
            throw exception(ASSET_PRICING_PARAM_INVALID);
        }
        if (skuMapper.selectListByStandardSpuId(reqVO.getStandardSpuId()).stream()
                .noneMatch(s -> Objects.equals(s.getId(), reqVO.getStandardProductSkuId()))) {
            throw exception(ASSET_PRICING_SPU_SKU_MISMATCH);
        }

        AssetResidualConfig residualConfig = residualConfigMapper.selectBySkuId(reqVO.getStandardProductSkuId(), reqVO.getPartnerId());
        Map<Integer, AssetResidualYearConfig> yearMap = residualConfig == null
                ? Collections.emptyMap()
                : residualYearConfigMapper.selectByConfigId(residualConfig.getId(), reqVO.getPartnerId())
                        .stream().collect(Collectors.toMap(AssetResidualYearConfig::getUseYear, y -> y));

        Date now = new Date();
        for (AssetPricingItemSaveReqVO item : reqVO.getConfigs()) {
            AssetPricingConfig po = buildAndValidate(reqVO, item, residualConfig, yearMap, u, now);
            pricingConfigMapper.logicDelBySkuAndKeys(
                    reqVO.getStandardProductSkuId(), reqVO.getPartnerId(), item.getLeaseMode(), item.getUseYear(), u.getId(), now);
            pricingConfigMapper.insert(po);
        }
    }

    private AssetPricingConfig buildAndValidate(
            AssetPricingConfigSaveReqVO reqVO,
            AssetPricingItemSaveReqVO item,
            AssetResidualConfig residualConfig,
            Map<Integer, AssetResidualYearConfig> yearMap,
            LoginUser<?> u,
            Date now) {
        AssetResidualYearConfig y = yearMap.get(item.getUseYear());
        Long deviceValue = item.getUseYear() == 1
                ? (residualConfig == null ? null : residualConfig.getOfficialPrice())
                : (y == null ? null : y.getYearBeginValue());
        BigDecimal deviceValueDecimal = toAmountDecimal(deviceValue);

        log.info("deviceValue: {},deviceValueDecimal:{}", deviceValue, deviceValueDecimal);
        if (deviceValue == null || deviceValueDecimal == null || (item.getUseYear() > 1 && y == null)) {
            throw exception(ASSET_PRICING_RESIDUAL_VALUE_REQUIRED);
        }

        log.info("DeviceTotalPriceCoefficient: {}", item.getDeviceTotalPriceCoefficient());
        if (item.getDeviceTotalPriceCoefficient().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ASSET_PRICING_TOTAL_PRICE_COEFFICIENT_INVALID);
        }

        BigDecimal deviceTotalPrice = deviceValueDecimal.multiply(item.getDeviceTotalPriceCoefficient()).setScale(4, RoundingMode.HALF_UP);
        Long deviceTotalPriceLong = toAmountLong(deviceTotalPrice);

        log.info("deviceTotalPrice: {},deviceTotalPriceLong:{}", deviceTotalPrice,deviceTotalPriceLong);
        if (deviceTotalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ASSET_PRICING_TOTAL_PRICE_INVALID);
        }

        log.info("TotalPriceLowerLimit: {}", y.getTotalPriceLowerLimit());
        if (y != null && y.getTotalPriceLowerLimit() != null && deviceTotalPriceLong < y.getTotalPriceLowerLimit()) {
            throw exception(ASSET_PRICING_TOTAL_PRICE_TOO_LOW);
        }

        log.info("TotalPriceUpperLimit: {}", y.getTotalPriceUpperLimit());
        if (y != null && y.getTotalPriceUpperLimit() != null && deviceTotalPriceLong > y.getTotalPriceUpperLimit()) {
            throw exception(ASSET_PRICING_TOTAL_PRICE_TOO_HIGH);
        }

        if (item.getTotalRentCoefficient().compareTo(BigDecimal.ZERO) < 0) {
            throw exception(ASSET_PRICING_TOTAL_RENT_COEFFICIENT_INVALID);
        }
        BigDecimal totalRent = deviceTotalPrice.multiply(item.getTotalRentCoefficient()).setScale(4, RoundingMode.HALF_UP);
        Long totalRentLong = toAmountLong(totalRent);
        if (totalRent.compareTo(BigDecimal.ZERO) < 0) {
            throw exception(ASSET_PRICING_TOTAL_RENT_INVALID);
        }

        Long monthlyRent = toAmountLong(totalRent.divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP));
        Long dailyRent = toAmountLong(totalRent.divide(BigDecimal.valueOf(365), 4, RoundingMode.HALF_UP));
        if (monthlyRent < 0 || dailyRent < 0) {
            throw exception(ASSET_PRICING_MONTHLY_OR_DAILY_RENT_INVALID);
        }

        Long annualDep = y == null ? 0L : y.getYearDepreciationAmount();
        Long expiration = deviceTotalPriceLong - annualDep;
        if (expiration < 0) {
            throw exception(ASSET_PRICING_EXPIRATION_PURCHASE_INVALID);
        }

        AssetPricingConfig po = new AssetPricingConfig();
        po.setPartnerId(reqVO.getPartnerId());
        po.setStandardSpuId(reqVO.getStandardSpuId());
        po.setStandardProductSkuId(reqVO.getStandardProductSkuId());
        po.setUseYear(item.getUseYear());
        po.setLeaseMode(item.getLeaseMode());
        po.setDeviceValue(deviceValue);
        po.setDeviceTotalPriceCoefficient(item.getDeviceTotalPriceCoefficient().setScale(4, RoundingMode.HALF_UP));
        po.setDeviceTotalPrice(deviceTotalPriceLong);
        po.setTotalRentCoefficient(item.getTotalRentCoefficient().setScale(4, RoundingMode.HALF_UP));
        po.setTotalRent(totalRentLong);
        po.setMonthlyRent(monthlyRent);
        po.setDailyRent(dailyRent);
        po.setAnnualDepreciationAmount(annualDep);
        po.setExpirationPurchaseAmount(expiration);
        po.setResidualValueConfigId(residualConfig == null ? null : residualConfig.getId());
        po.setPricingSource(1);
        po.setStatus(1);
        po.setRemark(item.getRemark());
        po.setCreateBy(u.getId());
        po.setUpdateBy(u.getId());
        po.setCreateTime(now);
        po.setUpdateTime(now);
        po.setIsDeleted(0);
        return po;
    }

    private BigDecimal toAmountDecimal(Long v) {
        return v == null ? null : BigDecimal.valueOf(v).divide(AMOUNT_SCALE, 4, RoundingMode.HALF_UP);
    }

    private Long toAmountLong(BigDecimal v) {
        return v == null ? null : v.multiply(AMOUNT_SCALE).setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
