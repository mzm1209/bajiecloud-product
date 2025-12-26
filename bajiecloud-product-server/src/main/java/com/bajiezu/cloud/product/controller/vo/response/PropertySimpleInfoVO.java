package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "属性简明信息")
@Data
public class PropertySimpleInfoVO {

    private Long id;

    private String name;

    private List<PropertyValueSimpleInfoVO> propertyValues;
}
