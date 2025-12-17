package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyValueReqVO;
import com.bajiezu.cloud.product.dal.entity.ProductProperty;

public interface ProductPropertyService {

    /**
     * 新增商品属性
     */
    void add(ProductPropertyReqVO reqVO);

    /**
     * 编辑商品属性
     */
    void mod(ProductPropertyReqVO reqVO);

    /**
     * 删除商品属性
     */
    void del(ProductPropertyReqVO reqVO);

    /**
     * 商品属性列表查询
     */
    PageResult<ProductProperty> list(ProductPropertyReqVO reqVO);

}
