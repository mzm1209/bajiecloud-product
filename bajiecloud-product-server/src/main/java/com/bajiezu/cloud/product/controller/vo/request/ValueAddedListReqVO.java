package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "增值服务列表查询参数")
@Data
public class ValueAddedListReqVO extends PageParam {

    @Schema(description = "增值服务名称")
    private String name;

    @Schema(description = "增值服务状态")
    private Integer status;

    @Schema(description = "创建时间开始")
    private Date createTimeBegin;

    @Schema(description = "创建时间结束")
    private Date createTimeEnd;
}
