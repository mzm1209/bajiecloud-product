package com.bajiezu.cloud.product.controller;


import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.dto.LongIdsReqVO;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.StandardProductRespVO;
import com.bajiezu.cloud.product.service.StandardProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 标准商品管理")
@RestController
@RequestMapping("/product/standard")
@Validated
public class StandardProductController {

    @Resource
    private StandardProductService standardProductService;

    @PostMapping("/page")
    @Operation(summary = "分页查询标准商品")
    public CommonResult<PageResult<StandardProductRespVO>> page(@Valid @RequestBody StandardProductListReqVO reqVO) {
        return CommonResult.success(standardProductService.page(reqVO));
    }

    @PostMapping("/add")
    @Operation(summary = "新增标准商品")
    public CommonResult<Boolean> add(@Valid @RequestBody StandardProductAddReqVO reqVO) {
        standardProductService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "修改标准商品")
    public CommonResult<Boolean> mod(@Valid @RequestBody StandardProductModReqVO reqVO) {
        standardProductService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/detail")
    @Operation(summary = "标准商品详情")
    public CommonResult<StandardProductRespVO> detail(@Valid @RequestBody LongIdReqVO reqVO) {
        return CommonResult.success(standardProductService.detail(reqVO.getId()));
    }

    @PostMapping("/del")
    @Operation(summary = "删除标准商品")
    public CommonResult<Boolean> del(@Valid @RequestBody LongIdsReqVO reqVO) {
        standardProductService.del(reqVO.getIds());
        return CommonResult.success(true);
    }

    @PostMapping("/changeStatus")
    @Operation(summary = "改变状态")
    public CommonResult<Boolean> changeStatus(@Valid @RequestBody StatusChangeReqVo reqVO) {
        standardProductService.changeStatus(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/simpleList")
    @Operation(summary = "标准商品简明信息-提供给创建营销商品使用")
    public CommonResult<PageResult<StandardProductRespVO>> simpleList(@Valid @RequestBody StandardProductListReqVO reqVO) {
        return CommonResult.success(standardProductService.page(reqVO));
    }
}
