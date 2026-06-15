package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.MarketingAvailablePropertyVO;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.*;
import com.bajiezu.cloud.product.dto.AssetConfigDto;
import com.bajiezu.cloud.product.dto.MarketingProductReqVO;
import com.bajiezu.cloud.product.dto.ProductDetailRespVO;
import com.bajiezu.cloud.product.dto.SkuRentalPriceRespDto;
import com.bajiezu.cloud.product.dto.SkuRentalStockReqDto;
import com.bajiezu.cloud.product.dto.SkuRespDto;

import java.util.List;

public interface MarketingProductService {

    PageResult<MarketingProductRespVO> page(MarketingProductListReqVO reqVO);

    ProductTypeStatisticRespVO productTypeStatistic();

    void add(MarketingProductAddReqVO reqVO);

    void mod(MarketingProductModReqVO reqVO);

    MarketingProductDetailRespVO detail(Long id);

    void del(List<Long> ids);

    void onOffShelves(OnOffShelvesReqVO reqVO);

    void approve(MarketingProductApproveReqVO reqVO);

    StatusStatisticRespVO statusStatistic(MarketingProductListReqVO reqVO);

    List<ProductDetailRespVO> batchGetProductDetail(MarketingProductReqVO reqVO);

    PageResult<SpuRespVO> spuListForAddCoupon(ProductListReqVO reqVO);

    PageResult<SkuRespVO> skuListForAddCoupon(ProductListReqVO reqVO);

    SkuRespDto getSkuInfoById(Long skuId);

    /**
     * 定时任务更新商品上架状态
     */
    void updateProductShelvesStatus();

    void updateSkuStock(List<UpdateSkuStockReqVO> reqVO);

    /**
     * 按租赁方式+租期获取SKU价格与库存。
     */
    SkuRentalPriceRespDto getSkuRentalPrice(Long skuId, Integer rentalMethod, Integer rentalPeriodMonth);

    /**
     * 原子扣减租期维度库存，库存不足返回 false。
     */
    boolean deductRentalStock(SkuRentalStockReqDto reqDto);

    /**
     * 回补租期维度库存。
     */
    boolean restoreRentalStock(SkuRentalStockReqDto reqDto);

    List<MarketingAvailablePropertyVO> availablePropertiesByStandardSpuId(Long standardProductSpuId);

    /**
     * 按营销SKU+已用月数查询精确残值（当期购买金）。
     * 内部做营销SKU→标品SKU桥接，再查资产残值月配置表。找不到返回null。
     */
    Long getResidualAmount(Long marketingSkuId, Integer globalMonth);

    /**
     * 按营销SKU+租赁方式查询所有可用的租期价格列表。
     */
    List<SkuRentalPriceRespDto> listSkuRentalPrices(Long skuId, Integer rentalMethod);

    /**
     * 按营销SKU查询关联的资产配置（残值+定价）全量数据。
     */
    AssetConfigDto getAssetConfig(Long marketingSkuId);
}
