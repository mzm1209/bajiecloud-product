package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 增值服务表实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("value_added")
public class ValueAdded extends BaseDO {
    /**
     * 服务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 编号
     */
    private String code;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 状态 0:禁用 1:启用
     */
    private Integer status;
    
    /**
     * 售价 (单位为毫，存储时需注意单位转换)
     */
    private Long salePrice;
    
    /**
     * 续租售价
     */
    private Long renewalPrice;
    
    /**
     * 划线价
     */
    private Long strikethroughPrice;
    
    /**
     * 服务概要
     */
    private String serviceOverview;
    
    /**
     * 服务内容
     */
    private String serviceContent;
    
    /**
     * 详情图URL
     */
    private String picUrl;
}