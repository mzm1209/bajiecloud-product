package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ProductPropertyValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;


@Mapper
public interface ProductPropertyValueMapper extends BaseMapper<ProductPropertyValue> {

    /**
     * 根据属性项ID查询属性值列表
     */
    List<ProductPropertyValue> selectByPropertyId(Long propertyId);

    void batchInsert(@Param("propertyValues") List<ProductPropertyValue> propertyValues);

    Set<String> selectValuesByPropertyId(@Param("propertyId") Long propertyId);

    void logicDelByPropertyIdAndValues(@Param("propertyId") Long propertyId, @Param("values") Set<String> values,
                                       @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    void logicDelByPropertyId(@Param("propertyId") Long propertyId, @Param("updateBy") Long updateBy,
                              @Param("updateTime") Date updateTime);

    List<ProductPropertyValue> selectListByPropertyIds(@Param("propertyIds") List<Long> propertyIds);

    List<ProductPropertyValue> queryAll();
}
