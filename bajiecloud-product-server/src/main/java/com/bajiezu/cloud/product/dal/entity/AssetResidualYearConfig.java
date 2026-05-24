package com.bajiezu.cloud.product.dal.entity;
import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
@EqualsAndHashCode(callSuper = true )
@Data @TableName("asset_residual_year_config")
public class AssetResidualYearConfig extends BaseDO { @TableId(type = IdType.AUTO) private Long id; private Long residualConfigId; private Long partnerId; private Integer useYear; private BigDecimal upperCoefficient; private BigDecimal lowerCoefficient; private BigDecimal yearBeginValue; private BigDecimal yearDepreciationAmount; private BigDecimal yearEndResidualValue; private BigDecimal totalPriceUpperLimit; private BigDecimal totalPriceLowerLimit; }
