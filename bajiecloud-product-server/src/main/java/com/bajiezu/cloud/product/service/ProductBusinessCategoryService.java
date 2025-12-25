package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.response.BusinessCategoryRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductBusinessCategoryRespVO;
import com.bajiezu.cloud.product.dal.entity.ProductBusinessCategory;

import java.util.List;

public interface ProductBusinessCategoryService {

    PageResult<BusinessCategoryRespVO> page(PageParam pageParam);

    List<BusinessCategoryRespVO> listAll();
}
