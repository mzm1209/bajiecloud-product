package com.bajiezu.cloud.product.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "标准商品属性")
public class StandardProductPropertyVO {
    private Long propertyId;
    private String propertyName;
    private Integer sort = 0;
    private Integer isAddPropertyPic;
    private Integer isAddMarketingCorner;
    private Integer isSkuProperty;
    private List<StandardProductPropertyValueVO> propertyValues;
}
