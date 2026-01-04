package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ExpressTemplateShippingFrom;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;


@Mapper
public interface ExpressTemplateShippingFromMapper extends BaseMapper<ExpressTemplateShippingFrom> {

    void insertBatch(@Param("shippingFroms") List<ExpressTemplateShippingFrom> shippingFroms);

    Set<String> selectAreaCodesByTemplateId(@Param("templateId") Long templateId);

    void logicDelByTemplateIdAndAreaCodes(@Param("templateId") Long templateId, @Param("areaCodes") Set<String> areaCodes,
                                          @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    void logicDelByTemplateId(@Param("templateId") Long templateId, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    List<ExpressTemplateShippingFrom> selectListByTemplateIds(@Param("templateIds") Set<Long> templateIds);
}
