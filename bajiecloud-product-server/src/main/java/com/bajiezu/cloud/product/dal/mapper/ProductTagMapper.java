package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ProductTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductTagMapper extends BaseMapper<ProductTag> {

    /**
     * 查询标签列表（支持分页和条件查询）
     */
    List<ProductTag> queryList(@Param("name") String name,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);

    /**
     * 查询标签总数（支持条件查询）
     */
    Long queryCount(@Param("name") String name);

    List<ProductTag> querySimpleList(@Param("showPage") Integer showPage);
}
