package com.bajiezu.cloud.product.controller.vo.response;

import com.bajiezu.cloud.product.dto.PropertyVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "增值服务SKU信息")
@Data
public class ValueAddedSkuRespVO {

    @Schema(description = "增值服务SKU ID")
    private Long id;

    @Schema(description = "增值服务SKU 名称")
    private String name;

    @Schema(description = "sku属性")
    private List<PropertyVO> properties;

    @Schema(description = "审批状态 0:未审批 1:审批通过 2:审批拒绝")
    private Integer approveStatus;

    @Schema(description = "上下架状态 0:待上架 1:已上架 2:已下架")
    private Integer shelvesStatus;
}
