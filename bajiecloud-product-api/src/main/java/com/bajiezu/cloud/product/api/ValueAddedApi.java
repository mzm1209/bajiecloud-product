package com.bajiezu.cloud.product.api;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.dto.ValueAddedRespDto;
import com.bajiezu.cloud.product.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 增值服务")
public interface ValueAddedApi {

    String PREFIX = ApiConstants.PREFIX + "/valueAdded";

    @GetMapping(PREFIX + "/getByIds")
    @Operation(summary = "批量获取增值服务信息")
    CommonResult<List<ValueAddedRespDto>> getByIds(@RequestParam("ids") Collection<Long> ids);


}