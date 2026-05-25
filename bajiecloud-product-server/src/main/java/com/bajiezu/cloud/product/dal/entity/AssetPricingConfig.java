package com.bajiezu.cloud.product.dal.entity;

import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("asset_pricing_config")
public class AssetPricingConfig extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long standardSpuId;
    private Long standardProductSkuId;
    private Long partnerId;
    private Integer useYear;
    private Integer leaseMode;
    private BigDecimal deviceValue;
    private BigDecimal deviceTotalPriceCoefficient;
    private BigDecimal deviceTotalPrice;
    private BigDecimal totalRentCoefficient;
    private BigDecimal totalRent;
    private BigDecimal monthlyRent;
    private BigDecimal dailyRent;
    private BigDecimal annualDepreciationAmount;
    private BigDecimal expirationPurchaseAmount;
    private Long residualValueConfigId;
    private Integer pricingSource;
    private Integer status;
    private String remark;
}
