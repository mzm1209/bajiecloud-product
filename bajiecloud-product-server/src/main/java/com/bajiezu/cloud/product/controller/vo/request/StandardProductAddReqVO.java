package com.bajiezu.cloud.product.controller.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.bajiezu.cloud.product.controller.vo.StandardProductPropertyVO;
import com.bajiezu.cloud.product.controller.vo.StandardProductSkuVO;
import lombok.Data;

import java.util.List;

@Schema(description = "标准产品添加请求参数")
@Data
public class StandardProductAddReqVO {

    @Schema(description = "标准商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标准商品名称不能为空")
    private String name;

    @Schema(description = "品牌id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "品牌id不能为空")
    private Long brandId;

    @Schema(description = "经营类目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "经营类目id不能为空")
    private Long businessCategoryId;

    @Schema(description = "营销类目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "营销类目id不能为空")
    private Long marketingCategoryId;

    @Schema(description = "商品成色 0:全新 1:非全新", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品成色不能为空")
    private List<Integer> productConditions;

    @Schema(description = "监控属性 0:监管 1:非监管", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "监控属性不能为空")
    private List<Integer> monitorAttribute;

    @Schema(description = "操作类型：1-保存（草稿），2-提交")
    @NotNull(message = "操作类型不能为空")
    private Integer operationType;

    private List<StandardProductPropertyVO> spuProperties;

    private List<StandardProductSkuVO> skus;
}

