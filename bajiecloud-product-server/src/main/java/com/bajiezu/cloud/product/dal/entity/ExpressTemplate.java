package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 快递模版表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExpressTemplate extends BaseDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 快递模版编码
     */
    private String code;
    
    /**
     * 快递模版名称
     */
    private String templateName;
    
    /**
     * 快递服务类型 1:普通快递 2:面签 3:当面激活 4:当面激活（可取消）
     */
    private Integer expressServiceType;
    
    /**
     * 邮费类型 1:包邮 2:除部分地区包邮 3:不包邮
     */
    private Integer postageType;
    
    /**
     * 默认邮费
     */
    private Long defaultShippingCost;
    
    /**
     * 快递模版描述
     */
    private String remark;
}