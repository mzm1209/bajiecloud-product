package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductTypeStatisticRespVO;
import com.bajiezu.cloud.product.controller.vo.response.StatusStatisticRespVO;
import com.bajiezu.cloud.product.service.MarketingProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 营销商品管理")
@RestController
@RequestMapping("/product/mk/")
@Validated
public class MarketingProductController {

    @Resource
    private MarketingProductService marketingProductService;

    @PostMapping("/page")
    @Operation(summary = "列表")
    public CommonResult<Boolean> page() {
        return CommonResult.success(true);
    }

    @PostMapping("/productTypeStatistic")
    @Operation(summary = "营销商品-各种类型商品的统计数据")
    public CommonResult<ProductTypeStatisticRespVO> productTypeStatistic() {
        return CommonResult.success(marketingProductService.productTypeStatistic());
    }

    @PostMapping("/add")
    @Operation(summary = "新增")
    public CommonResult<Boolean> add() {
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "编辑")
    public CommonResult<Boolean> mod() {
        return CommonResult.success(true);
    }

    @PostMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<Boolean> detail() {
        return CommonResult.success(true);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public CommonResult<Boolean> del() {
        return CommonResult.success(true);
    }

    @PostMapping("/onOffShelves")
    @Operation(summary = "上下架")
    public CommonResult<Boolean> onOffShelves() {
        return CommonResult.success(true);
    }

    @PostMapping("/approve")
    @Operation(summary = "审核")
    public CommonResult<Boolean> approve() {
        return CommonResult.success(true);
    }

    @PostMapping("/statusStatistic")
    @Operation(summary = "商品各种状态统计值")
    public CommonResult<StatusStatisticRespVO> statusStatistic(@RequestBody MarketingProductListReqVO reqVO) {
        return CommonResult.success(marketingProductService.statusStatistic(reqVO));
    }
}
