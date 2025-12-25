package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.ProductMcRespVO;
import com.bajiezu.cloud.product.dto.McSimpleInfoRespVO;
import com.bajiezu.cloud.product.service.ProductMarketingCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品营销类目管理控制器
 */
@Tag(name = "管理后台 - 营销类目管理")
@RestController
@RequestMapping("/product/mc")
@Validated
public class ProductMarketingCategoryController {

    @Resource
    private ProductMarketingCategoryService productMarketingCategoryService;

    @PostMapping("/add")
    @Operation(summary = "新增营销类目")
    //@PreAuthorize("@ss.hasPermission('product:marketing-category:add')")
    public CommonResult<Boolean> add(@Valid @RequestBody PMCAddReqVO reqVO) {
        productMarketingCategoryService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "编辑营销类目")
    //@PreAuthorize("@ss.hasPermission('product:marketing-category:mod')")
    public CommonResult<Boolean> mod(@Valid @RequestBody PMCModReqVO reqVO) {
        productMarketingCategoryService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/del")
    @Operation(summary = "删除营销类目")
    //@PreAuthorize("@ss.hasPermission('product:marketing-category:del')")
    public CommonResult<Boolean> del(@Valid @RequestBody PMCDelReqVO reqVO) {
        productMarketingCategoryService.del(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/page")
    @Operation(summary = "营销类目列表")
    //@PreAuthorize("@ss.hasPermission('product:marketing-category:list')")
    public CommonResult<PageResult<ProductMcRespVO>> page(@Valid @RequestBody ProductMCListReq reqVO) {
        return CommonResult.success(productMarketingCategoryService.page(reqVO));
    }

    @PostMapping("/changeStatus")
    @Operation(summary = "启用/禁用营销类目")
    //@PreAuthorize("@ss.hasPermission('product:marketing-category:status-change')")
    public CommonResult<Boolean> changeStatus(@Valid @RequestBody PMCStatusChangeVO reqVO) {
        productMarketingCategoryService.changeStatus(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/tree")
    @Operation(summary = "营销类型简明信息列表")
    public CommonResult<List<ProductMcRespVO>> tree() {
        return CommonResult.success(productMarketingCategoryService.tree());
    }
}
