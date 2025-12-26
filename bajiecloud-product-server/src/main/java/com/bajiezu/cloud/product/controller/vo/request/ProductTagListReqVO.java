package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品标签列表查询参数")
@Data
public class ProductTagListReqVO extends PageParam {

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签状态 0:禁用 1:启用")
    private Integer status;
}
