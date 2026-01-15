package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface ProductMarketingCategoryMapper extends BaseMapper<ProductMarketingCategory> {

    Long queryCount(@Param("name") String name);

    List<ProductMarketingCategory> selectByStatus(@Param("status") Integer status);

    List<ProductMarketingCategory> queryByParentIdAndName(@Param("parentId") Long parentId, @Param("name") String name);

    void logicDelById(@Param("id") Long id, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    void logicDelByPathLike(@Param("path") String path, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    void updateStatusById(@Param("id") Long id, @Param("status") Integer status, @Param("updateBy") Long updateBy,
                          @Param("updateTime") Date updateTime);

    void updateStatusByPathLike(@Param("path") String path, @Param("status") Integer status,
                                @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    List<ProductMarketingCategory> queryByName(@Param("name") String name);

    List<ProductMarketingCategory> selectByLevel(@Param("ids") Collection<Long> ids, @Param("level") Integer level,
                                                 @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    Long selectCountByLevel(@Param("ids") Collection<Long> ids, @Param("level") Integer level);

    List<ProductMarketingCategory> batchSelectByPathPrefix(@Param("ids") Collection<Long> ids);

    void updatePathById(@Param("id") Long id, @Param("path") String path);

    List<ProductMarketingCategory> queryAll();

    List<ProductMarketingCategory> selectSelfAndParentsById(@Param("id") Long id);

    List<ProductMarketingCategory> selectListByIds(@Param("ids") Collection<Long> ids);

    String selectNameById(@Param("id") Long id);

    List<String> selectSelfAndParentNamesById(@Param("id") Long id);
}
