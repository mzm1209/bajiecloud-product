package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.product.controller.vo.response.ProductTypeStatisticRespVO;
import com.bajiezu.cloud.product.dal.dto.ProductTypeStatisticCountDTO;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.enums.ProductTypeEnum;
import com.bajiezu.cloud.product.service.MarketingProductService;
import com.bajiezu.cloud.product.util.SequenceGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MarketingProductServiceImpl implements MarketingProductService {

    @Resource
    private SequenceGenerator sequenceGenerator;
    @Resource
    private MarketingProductSpuMapper spuMapper;
    @Resource
    private MarketingProductSpuPropertyMapper spuPropertyMapper;
    @Resource
    private MarketingProductSpuPropertyValueMapper spuPropertyValueMapper;
    @Resource
    private MarketingProductSkuMapper skuMapper;
    @Resource
    private MarketingProductSkuPropertyValueMapper skuPropertyValueMapper;

    @Override
    public ProductTypeStatisticRespVO productTypeStatistic() {

        List<ProductTypeStatisticCountDTO> productTypeStatisticCountDTOS = spuMapper.productTypeStatistic();
        ProductTypeStatisticRespVO productTypeStatisticRespVO = new ProductTypeStatisticRespVO();
        for (ProductTypeStatisticCountDTO productTypeStatisticCountDTO : productTypeStatisticCountDTOS) {
            if (productTypeStatisticCountDTO.getType() == null) {
                continue;
            }
            ProductTypeEnum productTypeEnum = ProductTypeEnum.get(productTypeStatisticCountDTO.getType());
            switch (productTypeEnum) {
                case RENTAL_PRODUCT:
                    productTypeStatisticRespVO.setRentalProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                case PRODUCT_FOR_SALE:
                    productTypeStatisticRespVO.setProductForSaleCount(productTypeStatisticCountDTO.getCount());
                    break;
                case RECYCLED_PRODUCT:
                    productTypeStatisticRespVO.setRecycledProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                case PHYSICAL_PRODUCT:
                    productTypeStatisticRespVO.setPhysicalProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                case VIRTUAL_PRODUCT:
                    productTypeStatisticRespVO.setVirtualProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                default:
                    break;
            }
        }
        return productTypeStatisticRespVO;
    }
}
