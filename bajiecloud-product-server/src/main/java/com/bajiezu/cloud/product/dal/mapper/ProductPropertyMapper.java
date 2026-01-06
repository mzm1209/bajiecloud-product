package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.controller.vo.IdAndNameVO;
import com.bajiezu.cloud.product.dal.entity.ProductProperty;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
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

    ProductProperty selectByName(@Param("name") String name);

    void logicDelById(@Param("id") Long id, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    List<ProductProperty> queryAll();

    List<ProductProperty> selectListByIds(@Param("ids") Collection<Long> ids);

    List<IdAndNameVO> selectIdAndNamesByIds(@Param("ids") Collection<Long> ids);
}
