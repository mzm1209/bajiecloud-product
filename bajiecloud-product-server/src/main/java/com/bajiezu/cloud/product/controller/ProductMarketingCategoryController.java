package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.ProductMarketingCategoryVO;
import com.bajiezu.cloud.product.controller.vo.request.PMCAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.PMCDelReqVO;
import com.bajiezu.cloud.product.controller.vo.request.PMCModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.PMCStatusChangeVO;
import com.bajiezu.cloud.product.service.ProductMarketingCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品营销类目管理控制器
 */
@Tag(name = "管理后台 - 营销类目管理")
@RestController
@RequestMapping("/product/marketing-category")
@Validated
@Slf4j
public class ProductMarketingCategoryController {

    @Resource
    private ProductMarketingCategoryService productMarketingCategoryService;

    @PostMapping("/add")
    @Operation(summary = "新增营销类目")
    @PreAuthorize("@ss.hasPermission('product:marketing-category:add')")
    public CommonResult<Boolean> add(@Valid @RequestBody PMCAddReqVO reqVO) {
        productMarketingCategoryService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "编辑营销类目")
    @PreAuthorize("@ss.hasPermission('product:marketing-category:mod')")
    public CommonResult<Boolean> mod(@Valid @RequestBody PMCModReqVO reqVO) {
        productMarketingCategoryService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/del")
    @Operation(summary = "删除营销类目")
    @PreAuthorize("@ss.hasPermission('product:marketing-category:del')")
    public CommonResult<Boolean> del(@Valid @RequestBody PMCDelReqVO reqVO) {
        productMarketingCategoryService.del(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/list")
    @Operation(summary = "营销类目列表")
    @PreAuthorize("@ss.hasPermission('product:marketing-category:list')")
    public CommonResult<PageResult<ProductMarketingCategoryVO>> list(@Valid @RequestBody ProductMarketingCategoryVO reqVO) {
        return CommonResult.success(productMarketingCategoryService.list(reqVO));
    }

    @PostMapping("/status-change")
    @Operation(summary = "启用/禁用营销类目")
    @PreAuthorize("@ss.hasPermission('product:marketing-category:status-change')")
    public CommonResult<Boolean> statusChange(@Valid @RequestBody PMCStatusChangeVO reqVO) {
        productMarketingCategoryService.statusChange(reqVO);
        return CommonResult.success(true);
    }

//    @GetMapping("/getById/{id}")
//    @Operation(summary = "根据ID获取营销类目详情")
//    @PreAuthorize("@ss.hasPermission('product:marketing-category:getById')")
//    public CommonResult<PMCRespVO> getById(@PathVariable Long id) {
//        return CommonResult.success(productMarketingCategoryService.getById(id));
//    }

//    @GetMapping("/children/{parentId}")
//    @Operation(summary = "根据父ID获取子类目列表")
//    @PreAuthorize("@ss.hasPermission('product:marketing-category:children')")
//    public CommonResult<PageResult<PMCRespVO>> getChildrenByParentId(@PathVariable Long parentId) {
//        return CommonResult.success(productMarketingCategoryService.getChildrenByParentId(parentId));
//    }

//    @GetMapping("/tree")
//    @Operation(summary = "获取树形结构的营销类目")
//    @PreAuthorize("@ss.hasPermission('product:marketing-category:tree')")
//    public CommonResult<PageResult<PMCRespVO>> getTreeStructure() {
//        return CommonResult.success(productMarketingCategoryService.getTreeStructure());
//    }
}
