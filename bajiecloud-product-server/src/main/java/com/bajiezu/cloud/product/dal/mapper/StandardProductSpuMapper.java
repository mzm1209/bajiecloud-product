package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.dto.StandardProductQuery;
import com.bajiezu.cloud.product.dal.entity.StandardProductSpu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 标准商品SPU表 Mapper 接口
 */
@Mapper
public interface StandardProductSpuMapper extends BaseMapper<StandardProductSpu> {

    List<StandardProductSpu> selectListByQuery(StandardProductQuery query);

    Long selectCountByQuery(StandardProductQuery query);
}