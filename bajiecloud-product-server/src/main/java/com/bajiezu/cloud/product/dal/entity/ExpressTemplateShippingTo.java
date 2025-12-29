package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 快递模版关联的收货地区
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExpressTemplateShippingTo extends BaseDO {
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
     * 收货地区编码
     */
    private String areaCode;
    
    /**
     * 邮费
     */
    private Long shippingCost;
}