package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageParam;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.response.BusinessCategoryRespVO;
import com.bajiezu.cloud.product.dal.entity.ProductBusinessCategory;
import com.bajiezu.cloud.product.service.ProductBusinessCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "管理后台 - 经营类目管理")
@RestController
@RequestMapping("/product/bc")
@Validated
public class ProductBusinessCategoryController {

    @Resource
    private ProductBusinessCategoryService productBusinessCategoryService;

    @PostMapping("/page")
    @Operation(summary = "经营类目列表")
    //@PreAuthorize("@ss.hasPermission('product:business-category:list')")
    public CommonResult<PageResult<BusinessCategoryRespVO>> page(@RequestBody PageParam pageParam) {
        return CommonResult.success(productBusinessCategoryService.page(pageParam));
    }

    @PostMapping("/listAll")
    @Operation(summary = "经营类目树-提供给创建商品用")
    public CommonResult<List<BusinessCategoryRespVO>> listAll() {
        return CommonResult.success(productBusinessCategoryService.listAll());
    }
}
