package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMarketingCategoryMapper extends BaseMapper<ProductMarketingCategory> {
    // 继承BaseMapper已包含基本的CRUD方法
    // 可根据需要添加自定义查询方法
    List<ProductMarketingCategory> queryList(@Param("name") String name, @Param("status") Integer status,@Param("offset") Integer offset, @Param("limit") Integer limit);

    Long queryCount(@Param("name") String name);

}
