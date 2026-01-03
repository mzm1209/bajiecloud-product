package com.bajiezu.cloud.product.controller.vo.response;

import com.bajiezu.cloud.product.controller.vo.AreaCodeAndNameVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Schema(description = "快递模板响应结果")
@Data
public class ExpressTemplateRespVO {

    @Schema(description = "模板id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "default")
    private String code;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认模板")
    private String name;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "发货地区")
    private List<AreaCodeAndNameVO> shippingFroms;

    @Schema(description = "快递服务 1:普通快递 2:面签 3:当面激活 4:当面激活（可取消）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer expressServiceType;

    @Schema(description = "邮费类型 1:包邮 2:除部分地区包邮 3:不包邮", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer postageType;

    @Schema(description = "默认邮费", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long defaultShippingCost;

    @Schema(description = "状态 1:启用 0:禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "收货地区", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AreaCodeAndNameVO> shippingTos;

    @Schema(description = "合作商id")
    private Long partnerId;

    @Schema(description = "合作商名称")
    private String partnerName;

    @Schema(description = "创建人")
    private String creatorName;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新人")
    private String updaterName;

    @Schema(description = "更新时间")
    private Date updateTime;
}
