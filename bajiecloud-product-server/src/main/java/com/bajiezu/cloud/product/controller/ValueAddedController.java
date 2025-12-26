package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedStatusChangeReqVO;
import com.bajiezu.cloud.product.controller.vo.response.ValueAddedRespVO;
import com.bajiezu.cloud.product.service.ValueAddedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 增值服务管理")
@RestController
@RequestMapping("/product/valueAdded")
@Validated
public class ValueAddedController {

    @Resource
    private ValueAddedService valueAddedService;

    @PostMapping("/page")
    @Operation(summary = "分页查询增值服务")
    public CommonResult<PageResult<ValueAddedRespVO>> page(@Valid @RequestBody ValueAddedListReqVO reqVO) {
        return CommonResult.success(valueAddedService.page(reqVO));
    }

    @PostMapping("/add")
    @Operation(summary = "新增增值服务")
    public CommonResult<Boolean> add(@Valid @RequestBody ValueAddedAddReqVO reqVO) {
        valueAddedService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "修改增值服务")
    public CommonResult<Boolean> mod(@Valid @RequestBody ValueAddedModReqVO reqVO) {
        valueAddedService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/detail")
    @Operation(summary = "获取增值服务详情")
    public CommonResult<ValueAddedRespVO> detail(@RequestBody LongIdReqVO reqVO) {
        return CommonResult.success(valueAddedService.detail(reqVO.getId()));
    }

    @PostMapping("/del")
    @Operation(summary = "删除增值服务")
    public CommonResult<Boolean> del(@RequestBody LongIdReqVO reqVO) {
        valueAddedService.del(reqVO.getId());
        return CommonResult.success(true);
    }

    @PostMapping("/changeStatus")
    @Operation(summary = "改变增值服务状态")
    public CommonResult<Boolean> changeStatus(@RequestBody ValueAddedStatusChangeReqVO reqVO) {
        valueAddedService.changeStatus(reqVO);
        return CommonResult.success(true);
    }
}
