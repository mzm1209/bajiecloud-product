package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.PBRespVO;
import com.bajiezu.cloud.product.service.ProductBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 品牌管理")
@RestController
@RequestMapping("/product/brand")
@Validated
@Slf4j
public class ProductBrandController {

    @Resource
    private ProductBrandService productBrandService;

    @PostMapping("/add")
    @Operation(summary = "新增")
    @PreAuthorize("@ss.hasPermission('product:brand:add')")
    public CommonResult<Boolean> add(@Valid @RequestBody PBAddReqVO reqVO) {
        productBrandService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "编辑")
    @PreAuthorize("@ss.hasPermission('product:brand:mod')")
    public CommonResult<Boolean> mod(@Valid @RequestBody PBModReqVO reqVO) {
        productBrandService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@ss.hasPermission('product:brand:del')")
    public CommonResult<Boolean> del(@Valid @RequestBody PBDelReqVO reqVO) {
        productBrandService.del(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/statusChange")
    @Operation(summary = "启用禁用")
    @PreAuthorize("@ss.hasPermission('product:brand:statusChange')")
    public CommonResult<Boolean> statusChange(@Valid @RequestBody PBStatusChangeVO reqVO) {
        productBrandService.statusChange(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/list")
    @Operation(summary = "启用禁用")
    @PreAuthorize("@ss.hasPermission('product:brand:list')")
    public CommonResult<PageResult<PBRespVO>> list(@Valid @RequestBody PBListReqVO reqVO) {
        return CommonResult.success(productBrandService.list(reqVO));
    }
}
