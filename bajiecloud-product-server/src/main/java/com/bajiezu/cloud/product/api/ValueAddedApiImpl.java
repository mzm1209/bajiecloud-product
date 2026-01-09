package com.bajiezu.cloud.product.api;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.dto.ValueAddedRespDto;
import com.bajiezu.cloud.product.service.ValueAddedService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@Validated
@Primary
@Slf4j
public class ValueAddedApiImpl implements ValueAddedApi {

    @Resource
    private ValueAddedService valueAddedService;

    @Override
    public CommonResult<List<ValueAddedRespDto>> getByIds(Collection<Long> ids) {
        log.info("valueAdded getByIds ids: {}", ids);
        if (CollectionUtil.isEmpty(ids)) {
            return CommonResult.success(Collections.emptyList());
        }

        List<ValueAddedRespDto> valueAddedRespVOS = valueAddedService.getByIds(ids);
        return CommonResult.success(valueAddedRespVOS);
    }
}