package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ProductProperty;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductPropertyMapper extends BaseMapper<ProductProperty> {

    /**
     * 查询属性列表（支持分页和条件查询）
     */
    List<ProductProperty> queryList(@Param("name") String name,
                                    @Param("offset") Integer offset,
                                    @Param("limit") Integer limit);

    /**
     * 查询属性总数（支持条件查询）
     */
    Long queryCount(@Param("name") String name);

    /**
     * 查询属性列表（包含属性值）
     */
    List<ProductProperty> selectListWithValues(Long partnerId);

}
