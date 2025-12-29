package com.bajiezu.cloud.product.controller;

import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.ExpressTemplateRespVO;
import com.bajiezu.cloud.product.controller.vo.response.StandardProductRespVO;
import com.bajiezu.cloud.product.service.ExpressTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 快递模块管理")
@RestController
@RequestMapping("/product/exp/tpl")
@Validated
public class ExpressTemplateController {

    @Resource
    private ExpressTemplateService expressTemplateService;

    @PostMapping("/page")
    @Operation(summary = "分页查询快递模版")
    public CommonResult<PageResult<ExpressTemplateRespVO>> page(@Valid @RequestBody ExpressTemplateListReqVO reqVO) {
        return CommonResult.success(expressTemplateService.page(reqVO));
    }

    @PostMapping("/add")
    @Operation(summary = "新增快递模版")
    public CommonResult<Boolean> add(@Valid @RequestBody ExpressTemplateAddReqVO reqVO) {
        expressTemplateService.add(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/mod")
    @Operation(summary = "修改快递模版")
    public CommonResult<Boolean> mod(@Valid @RequestBody ExpressTemplateModReqVO reqVO) {
        expressTemplateService.mod(reqVO);
        return CommonResult.success(true);
    }

    @PostMapping("/detail")
    @Operation(summary = "快递模版详情")
    public CommonResult<ExpressTemplateRespVO> detail(@Valid @RequestBody LongIdReqVO reqVO) {
        return CommonResult.success(expressTemplateService.detail(reqVO.getId()));
    }

    @PostMapping("/del")
    @Operation(summary = "删除快递模版")
    public CommonResult<Boolean> del(@Valid @RequestBody LongIdReqVO reqVO) {
        expressTemplateService.del(reqVO.getId());
        return CommonResult.success(true);
    }

    @PostMapping("/changeStatus")
    @Operation(summary = "改变状态")
    public CommonResult<Boolean> changeStatus(@Valid @RequestBody StatusChangeReqVo reqVO) {
        expressTemplateService.changeStatus(reqVO);
        return CommonResult.success(true);
    }
}
