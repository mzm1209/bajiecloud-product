package com.bajiezu.cloud.product.controller.vo.app.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "App端商品SPU分页请求")
public class AppProductSpuPageReqVO extends PageParam {
    private Long categoryId;
    private Long subCategoryId;
    private String keyword;
    private Integer sortType;
}
