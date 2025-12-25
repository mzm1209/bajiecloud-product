package com.bajiezu.cloud.product.api;

import com.bajiezu.cloud.common.web.pojo.CommonResult;
import com.bajiezu.cloud.product.dto.McSimpleInfoRespVO;
import com.bajiezu.cloud.product.service.ProductMarketingCategoryService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Primary
public class ProductMcApiImpl implements ProductMcApi {

    @Resource
    private ProductMarketingCategoryService marketingCategoryService;

    @Override
    public CommonResult<List<McSimpleInfoRespVO>> getByIds(List<Long> ids) {
        return CommonResult.success(marketingCategoryService.getByIds(ids));
    }
}