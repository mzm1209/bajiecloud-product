package com.bajiezu.cloud.product.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bajiezu.cloud.product.dal.entity.ProductBusinessCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductBusinessCategoryMapper extends BaseMapper<ProductBusinessCategory> {

    List<ProductBusinessCategory> selectByLevel(@Param("level") int level, @Param("offset") int offset, @Param("pageSize") int pageSize);

    Long selectCountByLevel(@Param("level") int level);

    List<ProductBusinessCategory> batchSelectByPathPrefix(@Param("categoryIds") List<Long> categoryIds);

    List<ProductBusinessCategory> queryAll();

    List<ProductBusinessCategory> selectSelfAndParentsById(@Param("id") Long id);

    List<ProductBusinessCategory> selectListByIds(@Param("ids") Collection<Long> ids);

    String selectNameById(@Param("id") Long id);

    List<String> selectSelfAndParentNamesById(@Param("id") Long id);
}
