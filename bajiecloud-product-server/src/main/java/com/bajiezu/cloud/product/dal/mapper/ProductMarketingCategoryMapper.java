package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMarketingCategoryMapper extends BaseMapper<ProductMarketingCategory> {

    List<ProductMarketingCategory> queryList(@Param("name") String name, @Param("status") Integer status,
                                             @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long queryCount(@Param("name") String name);

    List<ProductMarketingCategory> selectByStatus(@Param("status") Integer status);

}
