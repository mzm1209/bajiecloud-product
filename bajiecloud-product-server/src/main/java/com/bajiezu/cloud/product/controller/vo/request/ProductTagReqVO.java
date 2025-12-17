package com.bajiezu.cloud.product.controller.vo.request;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Schema(description = "商品标签管理请求VO")
@Data
@NoArgsConstructor
public class ProductTagReqVO {

    @Schema(description = "标签ID", example = "1")
    private Long id;

    @Schema(description = "标签名称", example = "热销")
    private String name;

    @Schema(description = "标签图地址")
    private String picUrl;

    @Schema(description = "展示页面 1:SKU页 2:商品详情页", example = "1")
    private Integer showPage;

    @Schema(description = "状态 0:已停用 1:使用中", example = "1")
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "是否高亮 0:否 1:是", example = "1")
    private Integer isHighlight;

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    public void validate() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "标签名称不能为空");
    }

}
