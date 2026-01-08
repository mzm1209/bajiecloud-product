package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.product.controller.vo.AreaCodeAndNameVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Schema(description = "快递模板添加参数")
@Data
public class ExpressTemplateAddReqVO {

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认模板")
    @NotBlank(message = "模板名称不能为空")
    private String name;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "发货地区")
    private Set<String> shippingFroms;

    @Schema(description = "快递服务 1:普通快递 2:面签 3:当面激活 4:当面激活（可取消）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "快递服务不能为空")
    private Integer expressServiceType;

    @Schema(description = "邮费类型 1:包邮 2:除部分地区包邮 3:不包邮", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "邮费类型不能为空")
    private Integer postageType;

    @Schema(description = "默认邮费", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "默认邮费不能为空")
    private Long defaultShippingCost;

    @Schema(description = "状态 1:启用 0:禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "收货地区", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AreaCodeAndNameVO> shippingTos;
}
