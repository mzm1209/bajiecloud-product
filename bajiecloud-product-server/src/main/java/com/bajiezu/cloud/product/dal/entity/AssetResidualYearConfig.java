package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("asset_residual_year_config")
public class AssetResidualYearConfig extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("asset_residual_config_id")
    private Long assetResidualConfigId;

    private Long partnerId;

    private Integer useYear;

    @TableField("total_price_upper_coefficient")
    private BigDecimal totalPriceUpperCoefficient;

    @TableField("total_price_lower_coefficient")
    private BigDecimal totalPriceLowerCoefficient;

    private Long yearBeginValue;

    private Long yearDepreciationAmount;

    private Long yearEndResidualValue;

    private Long totalPriceUpperLimit;

    private Long totalPriceLowerLimit;
}
