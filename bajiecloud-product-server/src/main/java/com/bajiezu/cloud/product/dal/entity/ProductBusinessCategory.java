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
@TableName("product_business_category")
public class ProductBusinessCategory extends BaseDO implements Serializable {

    /** 经营类目ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 经营类目名称 */
    @TableField("name")
    private String name;

    /** 上级经营类目ID */
    @TableField("parent_id")
    private Long parentId;

    /** 合作商ID */
    @TableField("partner_id")
    private Long partnerId;

}
