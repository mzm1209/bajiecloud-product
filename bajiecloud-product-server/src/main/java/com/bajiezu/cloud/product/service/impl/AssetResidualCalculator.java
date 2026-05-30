package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.product.enums.AssetResidualConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class AssetResidualCalculator {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public static BigDecimal depreciationAmount(Integer ruleType, BigDecimal beginValue, BigDecimal ruleValue) {
        BigDecimal dep;
        if (Objects.equals(ruleType, AssetResidualConstants.RULE_TYPE_FIXED_AMOUNT)) {
            dep = ruleValue;
        } else if (Objects.equals(ruleType, AssetResidualConstants.RULE_TYPE_RESIDUAL_RATIO)) {
            BigDecimal ratio = ruleValue.divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP);
            dep = beginValue.multiply(ratio);
        } else {
            throw new IllegalArgumentException("invalid depreciation rule type");
        }
        dep = dep.setScale(2, RoundingMode.HALF_UP);
        if (dep.compareTo(BigDecimal.ZERO) < 0 || dep.compareTo(beginValue) > 0) throw new IllegalArgumentException("invalid depreciation amount");
        return dep;
    }
}
