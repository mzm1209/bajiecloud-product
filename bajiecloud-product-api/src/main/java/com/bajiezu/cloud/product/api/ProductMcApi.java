package com.bajiezu.cloud.product.api;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.dto.McSimpleInfoRespVO;
import com.bajiezu.cloud.product.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 商品营销类目")
public interface ProductMcApi {

    String PREFIX = ApiConstants.PREFIX + "/mc";

    @GetMapping(PREFIX + "/getByIds")
    @Operation(summary = "通过用户 ID 查询用户们")
    CommonResult<List<McSimpleInfoRespVO>> getByIds(@RequestParam("ids") List<Long> ids);


}