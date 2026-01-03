package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import com.bajiezu.cloud.product.dal.dto.ExpressTemplateQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "快递模板列表查询参数")
@Data
public class ExpressTemplateListReqVO extends PageParam {

    @Schema(description = "快递模板名称")
    private String name;

    @Schema(description = "状态")
    private Integer status;

    public ExpressTemplateQuery convert2ExpressTemplateQuery() {
        return ExpressTemplateQuery.builder()
                .templateName(name)
                .status(status)
                .offset((getPageNo() - 1) * getPageSize())
                .pageSize(getPageSize())
                .build();
    }
}
