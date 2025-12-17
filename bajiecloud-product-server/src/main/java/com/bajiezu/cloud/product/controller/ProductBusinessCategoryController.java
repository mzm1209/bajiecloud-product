package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.dal.entity.ProductBusinessCategory;
import com.bajiezu.cloud.product.service.ProductBusinessCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品经营类目管理控制器
 */
@Tag(name = "管理后台 - 经营类目管理")
@RestController
@RequestMapping("/product/business-category")
@Validated
@Slf4j
public class ProductBusinessCategoryController {

    @Autowired
    private ProductBusinessCategoryService productBusinessCategoryService;
//
//    @PostMapping("/add")
//    @Operation(summary = "新增经营类目")
//    @PreAuthorize("@ss.hasPermission('product:business-category:add')")
//    public CommonResult<Boolean> add(@Valid @RequestBody PBCAddReqVO reqVO) {
//        productBusinessCategoryService.add(reqVO);
//        return CommonResult.success(true);
//    }

    @PostMapping("/list")
    @Operation(summary = "经营类目列表")
    @PreAuthorize("@ss.hasPermission('product:business-category:list')")
    public CommonResult<PageResult<ProductBusinessCategory>> list() {
        return CommonResult.success(productBusinessCategoryService.list());
    }
}
