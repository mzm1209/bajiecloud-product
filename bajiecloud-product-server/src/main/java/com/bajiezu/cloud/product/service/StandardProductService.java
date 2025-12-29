package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.StatusChangeReqVo;
import com.bajiezu.cloud.product.controller.vo.response.StandardProductRespVO;

public interface StandardProductService {

    /**
     * 标准商品分页列表
     */
    PageResult<StandardProductRespVO> page(StandardProductListReqVO reqVO);

    /**
     * 新增标准商品
     */
    void add(StandardProductAddReqVO reqVO);

    /**
     * 编辑标准商品
     */
    void mod(StandardProductModReqVO reqVO);


    /**
     * 标准商品详情
     */
    StandardProductRespVO detail(Long id);

    /**
     * 删除标准商品
     */
    void del(Long id);

    /**
     * 改变标准商品状态 启用/禁用
     */
    void changeStatus(StatusChangeReqVo reqVO);
}
