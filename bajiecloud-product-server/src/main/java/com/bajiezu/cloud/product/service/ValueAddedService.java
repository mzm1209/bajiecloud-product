package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedModReqVO;
import com.bajiezu.cloud.product.controller.vo.request.ValueAddedStatusChangeReqVO;
import com.bajiezu.cloud.product.controller.vo.response.ValueAddedRespVO;

public interface ValueAddedService {

    /**
     * 分页查询
     */
    PageResult<ValueAddedRespVO> page(ValueAddedListReqVO reqVO);

    /**
     * 新增
     */
    void add(ValueAddedAddReqVO reqVO);

    /**
     * 修改
     */
    void mod(ValueAddedModReqVO reqVO);

    /**
     * 详情
     */
    ValueAddedRespVO detail(Long id);

    /**
     * 删除
     */
    void del(Long id);

    /**
     * 启用/禁用
     */
    void changeStatus(ValueAddedStatusChangeReqVO reqVO);
}
