package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyValueReqVO;
import com.bajiezu.cloud.product.dal.entity.ProductProperty;
import com.bajiezu.cloud.product.service.ProductPropertyService;
import com.bajiezu.cloud.product.service.ProductPropertyValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品属性管理控制器
 */
@Tag(name = "管理后台 - 商品属性管理")
@RestController
@RequestMapping("/product/property")
@Validated
@Slf4j
public class ProductPropertyController {

    @Autowired
    private ProductPropertyService productPropertyService;

    @Autowired
    private ProductPropertyValueService productPropertyValueService;

    // ==================== 商品属性相关接口 ====================

    @PostMapping("/add")
    @Operation(summary = "新增商品属性")
    @PreAuthorize("@ss.hasPermission('product:property:add')")
    public CommonResult<Boolean> addProperty(@Valid @RequestBody ProductPropertyReqVO reqVO) {
        productPropertyService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "编辑商品属性")
    @PreAuthorize("@ss.hasPermission('product:property:mod')")
    public CommonResult<Boolean> modProperty(@Valid @RequestBody ProductPropertyReqVO reqVO) {
        productPropertyService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/del")
    @Operation(summary = "删除商品属性")
    @PreAuthorize("@ss.hasPermission('product:property:del')")
    public CommonResult<Boolean> delProperty(@Valid @RequestBody ProductPropertyReqVO reqVO) {
        productPropertyService.del(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/list")
    @Operation(summary = "商品属性列表")
    @PreAuthorize("@ss.hasPermission('product:property:list')")
    public CommonResult<PageResult<ProductProperty>> listProperty(@Valid @RequestBody ProductPropertyReqVO reqVO) {
        return CommonResult.success(productPropertyService.list(reqVO));
    }

}
