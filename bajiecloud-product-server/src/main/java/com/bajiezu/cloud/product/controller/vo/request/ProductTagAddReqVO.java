package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "商品标签管理请求VO")
@Data
public class ProductTagAddReqVO {

    @Schema(description = "标签名称", example = "热销")
    @NotBlank(message = "标签名称不能为空")
    private String name;

    @Schema(description = "标签图地址")
    private String picUrl;

    @Schema(description = "展示页面 1:SKU页 2:商品详情页", example = "1")
    @NotNull(message = "展示页面不能为空")
    private List<Integer> showPages;

    @Schema(description = "排序", example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "是否高亮 0:否 1:是", example = "1")
    @NotNull(message = "是否高亮不能为空")
    private Integer isHighlight;

    @Schema(description = "备注")
    private String remark;
}
