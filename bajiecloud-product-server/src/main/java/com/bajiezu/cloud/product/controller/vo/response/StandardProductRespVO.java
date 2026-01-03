package com.bajiezu.cloud.product.controller.vo.response;

import com.bajiezu.cloud.product.controller.vo.request.ProductPropertyReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Schema(description = "标准商品响应实体")
@Data
public class StandardProductRespVO {

    @Schema(description = "标准商品ID", example = "1")
    private Long id;

    @Schema(description = "品牌ID", example = "1")
    private Long brandId;
    @Schema(description = "品牌名称", example = "1")
    private String brandName;

    @Schema(description = "商品编码", example = "1")
    private String code;

    @Schema(description = "商品名称", example = "1")
    private String name;

    @Schema(description = "营销类目ID", example = "1")
    private Long marketingCategoryId;
    @Schema(description = "营销类目名称", example = "1")
    private String marketingCategoryName;

    @Schema(description = "经营类目ID", example = "1")
    private Long businessCategoryId;
    @Schema(description = "经营类目名称", example = "1")
    private String businessCategoryName;

    @Schema(description = "商品状态", example = "1")
    private Integer status;

    @Schema(description = "商品成色", example = "1")
    private List<Integer> productConditions;

    @Schema(description = "监控属性", example = "1")
    private List<Integer> monitorAttributes;

    @Schema(description = "是否草稿 0:否 1:是", example = "1")
    private Integer isDraft;

    @Schema(description = "创建人名称", example = "1")
    private String creatorName;

    @Schema(description = "创建时间", example = "1")
    private Date createTime;

    @Schema(description = "更新人名称", example = "1")
    private String updaterName;

    @Schema(description = "更新时间", example = "1")
    private Date updateTime;
}
