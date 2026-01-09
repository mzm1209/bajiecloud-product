package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ExpressTemplateShippingTo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Mapper
public interface ExpressTemplateShippingToMapper extends BaseMapper<ExpressTemplateShippingTo> {

    void insertBatch(@Param("shippingTos") List<ExpressTemplateShippingTo> shippingTos);

    List<ExpressTemplateShippingTo> selectByTemplateId(@Param("templateId") Long templateId);

    void logicDelByTemplateIdAndAreaCodes(@Param("templateId") Long templateId, @Param("areaCodes") Set<String> areaCodes,
                                          @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    void updateBatch(@Param("shippingTos") List<ExpressTemplateShippingTo> shippingTos);

    void logicDelByTemplateId(@Param("templateId") Long templateId, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    List<ExpressTemplateShippingTo> selectListByTemplateIds(@Param("templateIds") Set<Long> templateIds);
}