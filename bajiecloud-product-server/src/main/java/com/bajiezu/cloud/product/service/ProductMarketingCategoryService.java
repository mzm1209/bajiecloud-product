package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.ProductMarketingCategoryVO;
import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import com.bajiezu.cloud.product.controller.vo.request.*;

import java.util.List;

public interface ProductMarketingCategoryService {

    /**
     * 新增营销分类
     */
    void add(PMCAddReqVO reqVO);

    /**
     * 编辑营销分类
     */
    void mod(PMCModReqVO reqVO);

    /**
     * 删除营销分类
     */
    void del(PMCDelReqVO reqVO);

    /**
     * 启用/禁用营销分类
     */
    void statusChange(PMCStatusChangeVO reqVO);


    PageResult<ProductMarketingCategoryVO> list(ProductMarketingCategoryVO reqVO);


}
