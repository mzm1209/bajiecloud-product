package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 增值服务金额赔付规则表实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("value_added_compensation_amount_rule")
public class ValueAddedCompensationAmountRule extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 增值服务ID
     */
    private Long valueAddedId;

    /**
     * 赔付金额上限，单位为毫
     */
    private Long compensationAmount;

    /**
     * 平台赔付比例，0~100
     */
    private Integer compensationAmountRatio;

    /**
     * 排序值，越小越靠前
     */
    private Integer sortOrder;
}
