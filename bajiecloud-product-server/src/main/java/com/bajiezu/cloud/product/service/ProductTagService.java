package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.dto.LongIdReqVO;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.ProductTagRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductTagSimpleInfoRespVO;

import java.util.List;

/**
 * 商品标签服务接口
 */
public interface ProductTagService {

    /**
     * 新增商品标签
     */
    void add(ProductTagAddReqVO reqVO);

    /**
     * 编辑商品标签
     */
    void mod(ProductTagModReqVO reqVO);

    /**
     * 删除商品标签
     */
    void del(LongIdReqVO reqVO);

    /**
     * 商品标签列表查询
     */
    PageResult<ProductTagRespVO> page(ProductTagListReqVO reqVO);

    /**
     * 商品标签状态变更
     */
    void statusChange(ProductTagStatusChangeVO reqVO);

    /**
     * 商品标签列表查询
     */
    List<ProductTagSimpleInfoRespVO> simpleList(ProductTagReqVO reqVO);
}
