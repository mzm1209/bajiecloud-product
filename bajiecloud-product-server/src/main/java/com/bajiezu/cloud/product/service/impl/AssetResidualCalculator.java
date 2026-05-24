package com.bajiezu.cloud.product.service.impl;
import java.math.*;
public class AssetResidualCalculator {
    public static BigDecimal depreciationAmount(Integer ruleType, BigDecimal beginValue, BigDecimal ruleValue) {
        BigDecimal dep = ruleType == 1 ? ruleValue : beginValue.multiply(ruleValue.compareTo(BigDecimal.ONE) > 0 ? ruleValue.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP) : ruleValue);
        dep = dep.setScale(2, RoundingMode.HALF_UP);
        if (dep.compareTo(BigDecimal.ZERO) < 0 || dep.compareTo(beginValue) > 0) throw new IllegalArgumentException("invalid depreciation amount");
        return dep;
    }
}
