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
@TableName("product_tag")
public class ProductTag extends BaseDO {

    /** 商品标签ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 标签名称 */
    @TableField("name")
    private String name;

    /** 标签图地址 */
    @TableField("pic_url")
    private String picUrl;

    /** 展示页面 1:SKU页 2:商品详情页 */
    @TableField("show_page")
    private String showPage;

    /** 状态 0:已停用 1:使用中 */
    @TableField("status")
    private Integer status;

    /** 排序 */
    @TableField("sort")
    private Integer sort;

    /** 是否高亮 0:否 1:是 */
    @TableField("is_highlight")
    private Integer isHighlight;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
