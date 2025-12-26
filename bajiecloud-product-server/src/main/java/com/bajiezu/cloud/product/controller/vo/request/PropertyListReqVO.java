package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "属性列表查询参数")
public class PropertyListReqVO extends PageParam {

    @Schema(description = "属性名称")
    private String name;
}
