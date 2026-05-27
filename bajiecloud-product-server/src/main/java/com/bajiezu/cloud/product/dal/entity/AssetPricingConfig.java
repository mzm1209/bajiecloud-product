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
    private Long deviceValue;
    private BigDecimal deviceTotalPriceCoefficient;
    private Long deviceTotalPrice;
    private BigDecimal totalRentCoefficient;
    private Long totalRent;
    private Long monthlyRent;
    private Long dailyRent;
    private Long annualDepreciationAmount;
    private Long expirationPurchaseAmount;
    private Long residualValueConfigId;
    private Integer pricingSource;
    private Integer status;
    private String remark;
}
