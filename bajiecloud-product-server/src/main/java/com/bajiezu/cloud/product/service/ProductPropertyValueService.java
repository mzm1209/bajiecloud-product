package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyValueReqVO;
import com.bajiezu.cloud.product.dal.entity.ProductPropertyValue;

import java.util.List;

public interface ProductPropertyValueService {
    /**
     * 新增商品属性
     */
    void add(ProductPropertyValueReqVO reqVO);

    /**
     * 编辑商品属性
     */
    void mod(ProductPropertyValueReqVO reqVO);

    /**
     * 删除商品属性
     */
    void del(ProductPropertyValueReqVO reqVO);
     /**
      * 商品属性值列表查询
      */
    List<ProductPropertyValue> list(Long propertyId);
}
