package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("asset_residual_config")
public class AssetResidualConfig extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long standardSpuId;
    private Long standardProductSkuId;
    private Long partnerId;

    /** 金额放大10000倍 */
    private Long officialPrice;

    private Integer depreciationRuleType;
    private Integer depreciationRuleSubType;
    private String remark;
    private Integer status;
}
