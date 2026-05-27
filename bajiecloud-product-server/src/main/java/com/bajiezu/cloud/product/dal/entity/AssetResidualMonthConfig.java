package com.bajiezu.cloud.product.dal.entity;
import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
@EqualsAndHashCode(callSuper = true )
@Data @TableName("asset_residual_month_config")
public class AssetResidualMonthConfig extends BaseDO { @TableId(type = IdType.AUTO) private Long id; @TableField("asset_residual_config_id") private Long residualConfigId; @TableField("asset_residual_year_config_id") private Long residualYearConfigId; private Long partnerId; private Integer useYear; private Integer useMonth; private Integer globalMonth; private BigDecimal depreciationRuleValue; private BigDecimal beginValue; private BigDecimal depreciationAmount; private BigDecimal residualValue; private BigDecimal accumulatedDepreciationAmount; private BigDecimal currentPurchaseAmount; }
