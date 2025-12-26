package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.PropertyAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.PropertyListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.PropertyModReqVO;
import com.bajiezu.cloud.product.controller.vo.response.PropertyRespVO;
import com.bajiezu.cloud.product.controller.vo.response.PropertySimpleInfoVO;
import com.bajiezu.cloud.product.dal.entity.ProductProperty;
import com.bajiezu.cloud.product.service.ProductPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Tag(name = "管理后台 - 商品属性管理")
@RestController
@RequestMapping("/product/property")
@Validated
@Slf4j
public class ProductPropertyController {

    @Resource
    private ProductPropertyService productPropertyService;

    @PostMapping("/add")
    @Operation(summary = "新增商品属性")
    //@PreAuthorize("@ss.hasPermission('product:property:add')")
    public CommonResult<Boolean> addProperty(@Valid @RequestBody PropertyAddReqVO reqVO) {
        productPropertyService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "编辑商品属性")
    //@PreAuthorize("@ss.hasPermission('product:property:mod')")
    public CommonResult<Boolean> modProperty(@Valid @RequestBody PropertyModReqVO reqVO) {
        productPropertyService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/del")
    @Operation(summary = "删除商品属性")
    //@PreAuthorize("@ss.hasPermission('product:property:del')")
    public CommonResult<Boolean> delProperty(@Valid @RequestBody LongIdReqVO reqVO) {
        productPropertyService.del(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/page")
    @Operation(summary = "商品属性分页列表")
    //@PreAuthorize("@ss.hasPermission('product:property:list')")
    public CommonResult<PageResult<PropertyRespVO>> page(@Valid @RequestBody PropertyListReqVO reqVO) {
        return CommonResult.success(productPropertyService.page(reqVO));
    }

    @PostMapping("/simpleList")
    @Operation(summary = "商品属性简明信息列表-用户创建商品")
    public CommonResult<List<PropertySimpleInfoVO>> simpleList() {
        return CommonResult.success(productPropertyService.simpleList());
    }
}
