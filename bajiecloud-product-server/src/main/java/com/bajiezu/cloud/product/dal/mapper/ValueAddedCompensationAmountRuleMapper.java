package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.entity.ValueAddedCompensationAmountRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 增值服务金额赔付规则 Mapper 接口
 */
@Mapper
public interface ValueAddedCompensationAmountRuleMapper extends BaseMapper<ValueAddedCompensationAmountRule> {

    void batchInsert(@Param("rules") List<ValueAddedCompensationAmountRule> rules);

    List<ValueAddedCompensationAmountRule> selectListByValueAddedIds(@Param("valueAddedIds") List<Long> valueAddedIds);

    void logicDelByValueAddedId(@Param("valueAddedId") Long valueAddedId,
                                @Param("updateBy") Long updateBy,
                                @Param("updateTime") Date updateTime);
}
