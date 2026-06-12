package com.bajiezu.cloud.product.service.impl;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AssetResidualCalculatorTest {
    @Test
    void fixedAmount() { assertEquals(new BigDecimal("100.00"), AssetResidualCalculator.depreciationAmount(1, new BigDecimal("1000"), new BigDecimal("100"))); }
    @Test
    void residualRatio() { assertEquals(new BigDecimal("1.00"), AssetResidualCalculator.depreciationAmount(2, new BigDecimal("1000"), new BigDecimal("0.1"))); }
    @Test
    void residualRatioPercentOne() { assertEquals(new BigDecimal("10.00"), AssetResidualCalculator.depreciationAmount(2, new BigDecimal("1000"), new BigDecimal("1"))); }
    @Test
    void residualRatioPercentGreaterThanOne() { assertEquals(new BigDecimal("20.00"), AssetResidualCalculator.depreciationAmount(2, new BigDecimal("1000"), new BigDecimal("2"))); }

    @Test
    void fixedAmountAllowsDecimalDepreciationEqualToBeginValue() {
        assertEquals(new BigDecimal("1650.50"), AssetResidualCalculator.depreciationAmount(1, new BigDecimal("1650.50"), new BigDecimal("1650.50")));
    }
    @Test
    void boundaryInvalid() { assertThrows(IllegalArgumentException.class, () -> AssetResidualCalculator.depreciationAmount(1, new BigDecimal("100"), new BigDecimal("101"))); }
    @Test
    void ruleTypeInvalid() { assertThrows(IllegalArgumentException.class, () -> AssetResidualCalculator.depreciationAmount(3, new BigDecimal("100"), new BigDecimal("1"))); }
}
