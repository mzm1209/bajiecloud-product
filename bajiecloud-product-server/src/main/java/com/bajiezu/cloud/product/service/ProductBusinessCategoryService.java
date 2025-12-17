package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.response.ProductBusinessCategoryRespVO;
import com.bajiezu.cloud.product.dal.entity.ProductBusinessCategory;

public interface ProductBusinessCategoryService {

    PageResult<ProductBusinessCategory> list();
}
