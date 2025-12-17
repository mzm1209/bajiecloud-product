package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.PBStatusChangeVO;
import com.bajiezu.cloud.product.controller.vo.request.ProductTagReqVO;
import com.bajiezu.cloud.product.dal.entity.ProductTag;

/**
 * 商品标签服务接口
 */
public interface ProductTagService {

    /**
     * 新增商品标签
     */
    void add(ProductTagReqVO reqVO);

    /**
     * 编辑商品标签
     */
    void mod(ProductTagReqVO reqVO);

    /**
     * 删除商品标签
     */
    void del(ProductTagReqVO reqVO);

    /**
     * 商品标签列表查询
     */
    PageResult<ProductTag> list(ProductTagReqVO reqVO);

    void statusChange(ProductTagReqVO reqVO);
}
