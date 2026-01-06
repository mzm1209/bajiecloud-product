package com.bajiezu.cloud.product.controller.vo.request;

import com.bajiezu.cloud.common.web.pojo.PageParam;
import com.bajiezu.cloud.product.dal.dto.ProductQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "商品查询参数")
@EqualsAndHashCode(callSuper = true)
@Data
public class ProductListReqVO extends PageParam {

    @Schema(description = "商品编码")
    private String code;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品属性值ID")
    private List<Long> propertyValuesIds;

    @Schema(description = "商品类型 1:租赁商品 2:售卖商品 3:回收商品 4:实物商品 5:虚拟商品")
    private Integer productType;

    public ProductQuery convert2ProductQuery() {
        ProductQuery query = new ProductQuery();
        query.setName(this.getName());
        query.setCode(this.getCode());
        query.setProductType(this.getProductType());
        query.setOffset((getPageNo() - 1) * getPageSize());
        query.setPageSize(getPageSize());
        return query;
    }
}
