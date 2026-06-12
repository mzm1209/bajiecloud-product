package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.product.controller.MarketingProductPropertyValueVO;
import com.bajiezu.cloud.product.controller.vo.MarketingProductPropertyVO;
import com.bajiezu.cloud.product.controller.vo.MarketingProductSkuVO;
import com.bajiezu.cloud.product.controller.vo.SkuPropertyValueVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarketingProductServiceImplTest {

    @Test
    void buildSkuCombinationsBySkuPropertiesAllowsSubsetOfCartesianProduct() throws Exception {
        MarketingProductServiceImpl service = new MarketingProductServiceImpl();
        List<MarketingProductPropertyVO> skuProperties = sampleSkuProperties();
        List<MarketingProductSkuVO> incomingSkus = List.of(
                sku(1L, value(1L, 1L, "橙色"), value(6L, 47L, "256G"), value(27L, 109L, "3天")),
                sku(2L, value(1L, 1L, "橙色"), value(6L, 48L, "128G"), value(27L, 110L, "2天")),
                sku(3L, value(1L, 1L, "橙色"), value(6L, 48L, "128G"), value(27L, 109L, "3天")),
                sku(4L, value(1L, 4L, "绿色"), value(6L, 47L, "256G"), value(27L, 110L, "2天")),
                sku(5L, value(1L, 4L, "绿色"), value(6L, 47L, "256G"), value(27L, 109L, "3天")),
                sku(6L, value(1L, 4L, "绿色"), value(6L, 48L, "128G"), value(27L, 110L, "2天")),
                sku(7L, value(1L, 4L, "绿色"), value(6L, 48L, "128G"), value(27L, 109L, "3天"))
        );

        List<MarketingProductSkuVO> normalizedSkus = invokeBuildSkuCombinations(service, skuProperties, incomingSkus);

        assertEquals(7, normalizedSkus.size());
    }

    @Test
    void buildSkuCombinationsBySkuPropertiesRejectsSkuOutsideCartesianProduct() {
        MarketingProductServiceImpl service = new MarketingProductServiceImpl();
        List<MarketingProductPropertyVO> skuProperties = sampleSkuProperties();
        List<MarketingProductSkuVO> incomingSkus = List.of(
                sku(1L, value(1L, 1L, "橙色"), value(6L, 47L, "256G"), value(27L, 999L, "10天"))
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> invokeBuildSkuCombinations(service, skuProperties, incomingSkus));
        assertEquals("营销SKU与SKU属性组合不一致，请按SKU属性组合重新提交", exception.getMessage());
    }


    @Test
    void normalizeMarketingCornerTextRejectsBlankTextWhenCornerEnabled() {
        MarketingProductServiceImpl service = new MarketingProductServiceImpl();

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> invokeNormalizeMarketingCornerText(service, " ", 1));

        assertEquals("开启营销角标时，营销角标文案不能为空", exception.getMessage());
    }

    @Test
    void normalizeMarketingCornerTextRejectsTooLongTextWhenCornerEnabled() {
        MarketingProductServiceImpl service = new MarketingProductServiceImpl();

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> invokeNormalizeMarketingCornerText(service, "文".repeat(65), 1));

        assertEquals("营销角标文案不能超过64个字符", exception.getMessage());
    }

    @Test
    void normalizeMarketingCornerTextClearsTextWhenCornerDisabled() throws Exception {
        MarketingProductServiceImpl service = new MarketingProductServiceImpl();

        String normalizedText = invokeNormalizeMarketingCornerText(service, null, 0);

        assertEquals("", normalizedText);
    }

    @SuppressWarnings("unchecked")
    private List<MarketingProductSkuVO> invokeBuildSkuCombinations(MarketingProductServiceImpl service,
                                                                    List<MarketingProductPropertyVO> skuProperties,
                                                                    List<MarketingProductSkuVO> incomingSkus) throws Exception {
        Method method = MarketingProductServiceImpl.class.getDeclaredMethod("buildSkuCombinationsBySkuProperties", List.class, List.class);
        method.setAccessible(true);
        try {
            return (List<MarketingProductSkuVO>) method.invoke(service, skuProperties, incomingSkus);
        } catch (InvocationTargetException exception) {
            Throwable targetException = exception.getTargetException();
            if (targetException instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw exception;
        }
    }

    private String invokeNormalizeMarketingCornerText(MarketingProductServiceImpl service,
                                                       String marketingCornerText,
                                                       Integer isAddMarketingCorner) throws Exception {
        Method method = MarketingProductServiceImpl.class.getDeclaredMethod("normalizeMarketingCornerText", String.class, Integer.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(service, marketingCornerText, isAddMarketingCorner);
        } catch (InvocationTargetException exception) {
            Throwable targetException = exception.getTargetException();
            if (targetException instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw exception;
        }
    }

    private List<MarketingProductPropertyVO> sampleSkuProperties() {
        return List.of(
                skuProperty(1L, "颜色", propertyValue(1L, "橙色"), propertyValue(4L, "绿色")),
                skuProperty(6L, "内存", propertyValue(47L, "256G"), propertyValue(48L, "128G")),
                skuProperty(27L, "续航", propertyValue(110L, "2天"), propertyValue(109L, "3天"))
        );
    }

    private MarketingProductPropertyVO skuProperty(Long propertyId, String propertyName, MarketingProductPropertyValueVO... values) {
        MarketingProductPropertyVO property = new MarketingProductPropertyVO();
        property.setPropertyId(propertyId);
        property.setPropertyName(propertyName);
        property.setIsSkuProperty(1);
        property.setPropertyValues(List.of(values));
        return property;
    }

    private MarketingProductPropertyValueVO propertyValue(Long propertyValueId, String value) {
        MarketingProductPropertyValueVO propertyValue = new MarketingProductPropertyValueVO();
        propertyValue.setProductPropertyValueId(propertyValueId);
        propertyValue.setValue(value);
        return propertyValue;
    }

    private MarketingProductSkuVO sku(Long id, SkuPropertyValueVO... propertyValues) {
        MarketingProductSkuVO sku = new MarketingProductSkuVO();
        sku.setId(id);
        sku.setPropertyValues(List.of(propertyValues));
        return sku;
    }

    private SkuPropertyValueVO value(Long propertyId, Long propertyValueId, String propertyValue) {
        SkuPropertyValueVO value = new SkuPropertyValueVO();
        value.setPropertyId(propertyId);
        value.setPropertyValueId(propertyValueId);
        value.setPropertyValue(propertyValue);
        return value;
    }
}
