package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.dto.LongIdsReqVO;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.*;
import com.bajiezu.cloud.product.dto.MarketingProductReqVO;
import com.bajiezu.cloud.product.dto.ProductDetailRespVO;
import com.bajiezu.cloud.product.service.MarketingProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;


@Tag(name = "管理后台 - 营销商品管理")
@RestController
@RequestMapping("/product/mk/")
@Validated
public class MarketingProductController {

    @Resource
    private MarketingProductService marketingProductService;

    @PostMapping("/page")
    @Operation(summary = "列表")
    public CommonResult<PageResult<MarketingProductRespVO>> page(@Valid @RequestBody MarketingProductListReqVO reqVO) {
        return CommonResult.success(marketingProductService.page(reqVO));
    }

    @PostMapping("/productTypeStatistic")
    @Operation(summary = "营销商品-各种类型商品的统计数据")
    public CommonResult<ProductTypeStatisticRespVO> productTypeStatistic() {
        return CommonResult.success(marketingProductService.productTypeStatistic());
    }

    @PostMapping("/add")
    @Operation(summary = "新增")
    public CommonResult<Boolean> add(@Valid @RequestBody MarketingProductAddReqVO reqVO) {
        marketingProductService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "编辑")
    public CommonResult<Boolean> mod(@Valid @RequestBody MarketingProductModReqVO reqVO) {
        marketingProductService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<MarketingProductDetailRespVO> detail(@Valid @RequestBody LongIdReqVO reqVO) {
        return CommonResult.success(marketingProductService.detail(reqVO.getId()));
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public CommonResult<Boolean> del(@Valid @RequestBody LongIdsReqVO reqVO) {
        marketingProductService.del(reqVO.getIds());
        return CommonResult.success(true);
    }

    @PostMapping("/onOffShelves")
    @Operation(summary = "上下架")
    public CommonResult<Boolean> onOffShelves(@Valid @RequestBody OnOffShelvesReqVO reqVO) {
        marketingProductService.onOffShelves(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/approve")
    @Operation(summary = "审核")
    public CommonResult<Boolean> approve(@Valid @RequestBody MarketingProductApproveReqVO reqVO) {
        marketingProductService.approve(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/statusStatistic")
    @Operation(summary = "商品各种状态统计值")
    public CommonResult<StatusStatisticRespVO> statusStatistic(@RequestBody MarketingProductListReqVO reqVO) {
        return CommonResult.success(marketingProductService.statusStatistic(reqVO));
    }

    @PostMapping("/spuListForAddCoupon")
    @Operation(summary = "获取SPU列表-提供给创建优惠券使用")
    public CommonResult<PageResult<SpuRespVO>> spuListForAddCoupon(@Valid @RequestBody ProductListReqVO reqVO) {
        return CommonResult.success(marketingProductService.spuListForAddCoupon(reqVO));
    }

    @PostMapping("/skuListForAddCoupon")
    @Operation(summary = "获取SKU列表-提供给创建优惠券使用")
    public CommonResult<PageResult<SkuRespVO>> skuListForAddCoupon(@Valid @RequestBody ProductListReqVO reqVO) {
        return CommonResult.success(marketingProductService.skuListForAddCoupon(reqVO));
    }

    @PostMapping("/batchGetProductDetail")
    @Operation(summary = "批量获取商品详情")
    public CommonResult<List<ProductDetailRespVO>> batchGetProductDetail(@Valid @RequestBody MarketingProductReqVO reqVO) {
        return CommonResult.success(marketingProductService.batchGetProductDetail(reqVO));
    }
}
