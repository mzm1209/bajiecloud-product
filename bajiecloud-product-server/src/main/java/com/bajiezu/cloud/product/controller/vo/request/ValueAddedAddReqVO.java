package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "增值服务添加参数")
@Data
public class ValueAddedAddReqVO {

    @Schema(description = "增值服务名称")
    @NotBlank(message = "增值服务名称不能为空")
    private String name;

    @Schema(description = "增值服务状态")
    @NotNull(message = "增值服务状态不能为空")
    private Integer status;

    @Schema(description = "增值服务销售价格")
    @NotNull(message = "增值服务销售价格不能为空")
    private Long salePrice;

    @Schema(description = "增值服务续租售价")
    private Long renewalPrice;

    @Schema(description = "增值服务划线价格")
    private Long strikethroughPrice;

    @Schema(description = "服务概要")
    private String serviceOverview;

    @Schema(description = "服务内容")
    @NotBlank(message = "服务内容不能为空")
    private String serviceContent;

    @Schema(description = "详情图URL,多个英文逗号分隔")
    private List<String> picUrls;

    @Schema(description = "关联的SKU ID集合")
    private List<Long> marketingProductSkuIds;
}
