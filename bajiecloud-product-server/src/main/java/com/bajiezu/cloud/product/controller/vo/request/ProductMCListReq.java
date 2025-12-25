package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "营销类目列表查询参数")
@Data
public class ProductMCListReq extends PageParam {

    @Schema(description = "营销类目名称")
    private String name;
}
