package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.PropertyAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.PropertyListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.PropertyModReqVO;
import com.bajiezu.cloud.product.controller.vo.response.PropertyRespVO;
import com.bajiezu.cloud.product.controller.vo.response.PropertySimpleInfoVO;

import java.util.List;

public interface ProductPropertyService {

    /**
     * 新增商品属性
     */
    void add(PropertyAddReqVO reqVO);

    /**
     * 编辑商品属性
     */
    void mod(PropertyModReqVO reqVO);

    /**
     * 删除商品属性
     */
    void del(LongIdReqVO reqVO);

    /**
     * 商品属性分页列表
     */
    PageResult<PropertyRespVO> page(PropertyListReqVO reqVO);

    /**
     * 商品属性简明信息列表
     */
    List<PropertySimpleInfoVO> simpleList();
}
