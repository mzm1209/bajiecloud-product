package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 快递模版关联的发货地区
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExpressTemplateShippingFrom extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 快递模版id
     */
    private Long expressTemplateId;
    
    /**
     * 发货地区编码
     */
    private String areaCode;
}