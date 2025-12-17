package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_property_value")
public class ProductPropertyValue extends BaseDO implements Serializable {

    /** 编号 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 属性项的编号 */
    @TableField("property_id")
    private Long propertyId;

    /** 属性对应的值 */
    @TableField("property_value")
    private String propertyValue;

    /** 合作商ID */
    @TableField("partner_id")
    private Long partnerId;
    // 注意：BaseDO中已经包含了createBy, updateBy, createTime, updateTime, isDeleted字段
    // 因此不需要在子类中重复定义这些字段
}
