package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_property")
public class ProductProperty extends BaseDO implements Serializable {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 属性名称 */
    @TableField("name")
    private String name;

    /** 排序 */
    @TableField("sort")
    private Integer sort;

    /** 合作商ID */
    @TableField("partner_id")
    private Long partnerId;

    // 注意：BaseDO中已经包含了createBy, updateBy, createTime, updateTime, isDeleted字段
    // 因此不需要在子类中重复定义这些字段

    // 非数据库字段
    @TableField(exist = false)
    private String creatorName;

    private List<ProductPropertyValue> productPropertyValues;
}
