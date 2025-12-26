package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ProductBrand;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductBrandMapper extends BaseMapper<ProductBrand> {

    List<ProductBrand> queryList(@Param("name") String name, @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long queryCount(@Param("name") String name);

    List<ProductBrand> querySimpleList();
}
