package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.ProductTagRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductTagSimpleInfoRespVO;
import com.bajiezu.cloud.product.service.ProductTagService;
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


@Tag(name = "管理后台 - 商品标签管理")
@RestController
@RequestMapping("/product/tag")
@Validated
public class ProductTagController {

    @Resource
    private ProductTagService productTagService;

    @PostMapping("/add")
    @Operation(summary = "新增商品标签")
    //@PreAuthorize("@ss.hasPermission('product:tag:add')")
    public CommonResult<Boolean> add(@Valid @RequestBody ProductTagAddReqVO reqVO) {
        productTagService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "编辑商品标签")
    //@PreAuthorize("@ss.hasPermission('product:tag:mod')")
    public CommonResult<Boolean> mod(@Valid @RequestBody ProductTagModReqVO reqVO) {
        productTagService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/del")
    @Operation(summary = "删除商品标签")
    //@PreAuthorize("@ss.hasPermission('product:tag:del')")
    public CommonResult<Boolean> del(@Valid @RequestBody LongIdReqVO reqVO) {
        productTagService.del(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/page")
    @Operation(summary = "商品标签列表")
    //@PreAuthorize("@ss.hasPermission('product:tag:list')")
    public CommonResult<PageResult<ProductTagRespVO>> page(@Valid @RequestBody ProductTagListReqVO reqVO) {
        return CommonResult.success(productTagService.page(reqVO));
    }


    @PostMapping("/status-change")
    @Operation(summary = "启用/禁用商品标签")
    //@PreAuthorize("@ss.hasPermission('product:tag:status-change')")
    public CommonResult<Boolean> statusChange(@Valid @RequestBody ProductTagStatusChangeVO reqVO) {
        productTagService.statusChange(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/simpleList")
    @Operation(summary = "商品标签简明信息列表 创建商品用")
    public CommonResult<List<ProductTagSimpleInfoRespVO>> simpleList(@RequestBody ProductTagReqVO reqVO) {
        return CommonResult.success(productTagService.simpleList(reqVO));
    }
}
