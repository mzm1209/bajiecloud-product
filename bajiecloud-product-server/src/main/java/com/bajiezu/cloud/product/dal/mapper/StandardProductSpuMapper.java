package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.dto.StandardProductQuery;
import com.bajiezu.cloud.product.dal.entity.StandardProductSpu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 标准商品SPU表 Mapper 接口
 */
@Mapper
public interface StandardProductSpuMapper extends BaseMapper<StandardProductSpu> {

    List<StandardProductSpu> selectListByQuery(StandardProductQuery query);

    Long selectCountByQuery(StandardProductQuery query);

    void logicDelByIds(@Param("ids") List<Long> ids, @Param("updateBy") Long updateBy,
                       @Param("updateTime") Date updateTime);

    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") Integer status,
                           @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    List<Long> selectIdsByBrandIdAndMarketingCategoryId(@Param("brandId") Long brandId,
                                                        @Param("marketingCategoryId") Long marketingCategoryId);

    List<StandardProductSpu> selectListByIds(@Param("ids") List<Long> ids);
}