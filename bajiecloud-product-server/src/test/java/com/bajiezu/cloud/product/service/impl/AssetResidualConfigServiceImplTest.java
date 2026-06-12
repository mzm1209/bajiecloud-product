package com.bajiezu.cloud.product.service.impl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssetResidualConfigServiceImplTest {

    @Test
    void toDisplayAmountPreservesDecimalAmount() throws Exception {
        AssetResidualConfigServiceImpl service = new AssetResidualConfigServiceImpl();
        Method method = AssetResidualConfigServiceImpl.class.getDeclaredMethod("toDisplayAmount", Long.class);
        method.setAccessible(true);

        assertEquals(new BigDecimal("2000.5000"), method.invoke(service, 20005000L));
        assertEquals(new BigDecimal("0.0000"), method.invoke(service, 0L));
    }
}
