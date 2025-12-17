package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ProductPropertyValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ProductPropertyValueMapper extends BaseMapper<ProductPropertyValue> {

    /**
     * 根据属性项ID查询属性值列表
     */
    List<ProductPropertyValue> selectByPropertyId(Long propertyId);
}
