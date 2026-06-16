package com.bajiezu.cloud.product.controller.app;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.app.request.*;
import com.bajiezu.cloud.product.controller.vo.app.response.*;
import com.bajiezu.cloud.product.service.app.AppProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "App端-营销商品查询")
@RestController
@RequestMapping("/app/product")
@Validated
public class AppProductController {
    @Resource
    private AppProductQueryService appProductQueryService;

    @PostMapping("/spu/page") @Operation(summary = "App端SPU分页")
    @PermitAll
    public CommonResult<PageResult<AppProductSpuPageRespVO>> spuPage(@Valid @RequestBody AppProductSpuPageReqVO reqVO){return CommonResult.success(appProductQueryService.spuPage(reqVO));}
    @PostMapping("/spu/detail") @Operation(summary = "App端SPU详情")
    @PermitAll
    public CommonResult<AppProductSpuDetailRespVO> spuDetail(@Valid @RequestBody AppProductSpuDetailReqVO reqVO){return CommonResult.success(appProductQueryService.spuDetail(reqVO));}
    @PostMapping("/sku/list") @Operation(summary = "App端SKU列表")
    @PermitAll
    public CommonResult<List<AppProductSkuRespVO>> skuList(@Valid @RequestBody AppProductSkuListReqVO reqVO){return CommonResult.success(appProductQueryService.skuList(reqVO));}
    @PostMapping("/sku/detail") @Operation(summary = "App端SKU详情")
    @PermitAll
    public CommonResult<AppProductSkuRespVO> skuDetail(@Valid @RequestBody AppProductSkuDetailReqVO reqVO){return CommonResult.success(appProductQueryService.skuDetail(reqVO));}
}
