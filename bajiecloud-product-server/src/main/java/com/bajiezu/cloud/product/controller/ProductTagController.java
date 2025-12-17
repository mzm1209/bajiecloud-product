package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.ProductTagReqVO;
import com.bajiezu.cloud.product.service.ProductTagService;
import com.bajiezu.cloud.product.dal.entity.ProductTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品标签管理控制器
 */
@Tag(name = "管理后台 - 商品标签管理")
@RestController
@RequestMapping("/product/tag")
@Validated
@Slf4j
public class ProductTagController {

    @Autowired
    private ProductTagService productTagService;

    @PostMapping("/add")
    @Operation(summary = "新增商品标签")
    @PreAuthorize("@ss.hasPermission('product:tag:add')")
    public CommonResult<Boolean> add(@Valid @RequestBody ProductTagReqVO reqVO) {
        productTagService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "编辑商品标签")
    @PreAuthorize("@ss.hasPermission('product:tag:mod')")
    public CommonResult<Boolean> mod(@Valid @RequestBody ProductTagReqVO reqVO) {
        productTagService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/del")
    @Operation(summary = "删除商品标签")
    @PreAuthorize("@ss.hasPermission('product:tag:del')")
    public CommonResult<Boolean> del(@Valid @RequestBody ProductTagReqVO reqVO) {
        productTagService.del(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/list")
    @Operation(summary = "商品标签列表")
    @PreAuthorize("@ss.hasPermission('product:tag:list')")
    public CommonResult<PageResult<ProductTag>> list(@Valid @RequestBody ProductTagReqVO reqVO) {
        return CommonResult.success(productTagService.list(reqVO));
    }


    @PostMapping("/status-change")
    @Operation(summary = "启用/禁用商品标签")
    @PreAuthorize("@ss.hasPefrmission('product:tag:status-change')")
    public CommonResult<Boolean> statusChange(@Valid @RequestBody ProductTagReqVO reqVO) {
        productTagService.statusChange(reqVO);
        return CommonResult.success(true);
    }
}
