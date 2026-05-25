package com.bajiezu.cloud.product.dal.entity;
import com.bajiezu.cloud.common.mybatis.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
@EqualsAndHashCode(callSuper = true )
@Data @TableName("asset_residual_config")
public class AssetResidualConfig extends BaseDO { @TableId(type = IdType.AUTO) private Long id; private Long standardSpuId; private Long standardProductSkuId; private Long partnerId; private BigDecimal officialPrice; private Integer depreciationRuleType; private Integer depreciationRuleSubType; private String remark; private Integer status; }
