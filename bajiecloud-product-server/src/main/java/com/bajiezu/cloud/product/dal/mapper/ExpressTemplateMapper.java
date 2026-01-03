package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.dto.ExpressTemplateQuery;
import com.bajiezu.cloud.product.dal.entity.ExpressTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExpressTemplateMapper extends BaseMapper<ExpressTemplate> {

    List<ExpressTemplate> selectListByQuery(ExpressTemplateQuery query);

    Long selectCountByQuery(ExpressTemplateQuery query);
}