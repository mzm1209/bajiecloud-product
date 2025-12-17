package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 品牌列表 Request VO")
public class PBListReqVO extends PageParam {

    @Schema(description = "品牌名称", example = "xiaomi")
    private String brandName;

}
