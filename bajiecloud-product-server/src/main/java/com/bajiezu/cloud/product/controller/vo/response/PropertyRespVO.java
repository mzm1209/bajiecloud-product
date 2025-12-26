package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
@Schema(description = "属性列表响应信息")
public class PropertyRespVO {

    @Schema(description = "属性ID")
    private Long id;

    @Schema(description = "属性名称")
    private String name;

    @Schema(description = "属性值")
    private Set<String> propertyValues;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建者名称")
    private String creatorName;

    @Schema(description = "创建时间")
    private Date createTime;
}
