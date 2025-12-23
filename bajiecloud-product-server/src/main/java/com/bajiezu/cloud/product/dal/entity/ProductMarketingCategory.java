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
@TableName("product_marketing_category")
public class ProductMarketingCategory extends BaseDO {

    /** 营销类目ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 营销类目名称 */
    @TableField("name")
    private String name;

    /** 上级营销类目ID */
    @TableField("parent_id")
    private Long parentId;

    /** 排序 */
    @TableField("sort")
    private Integer sort;

    /** 层级 1:一级 2:二级 3:三级 */
    @TableField("level")
    private Integer level;

    /** 状态 0:禁用 1:启用 */
    @TableField("status")
    private Integer status;

    /** 分组描述 */
    @TableField("remark")
    private String remark;

    /** 合作商ID */
    @TableField("partner_id")
    private Long partnerId;

    // 注意：BaseDO中已经包含了createBy, updateBy, createTime, updateTime, isDeleted字段
    // 因此不需要在子类中重复定义这些字段
}
