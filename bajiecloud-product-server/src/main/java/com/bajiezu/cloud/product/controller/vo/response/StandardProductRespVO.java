package com.bajiezu.cloud.product.controller.vo.response;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.bajiezu.cloud.excel.excel.core.annotations.DictFormat;
import com.bajiezu.cloud.excel.excel.core.convert.DictConvert;
import com.bajiezu.cloud.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Schema(description = "标准商品响应实体")
@Data
public class StandardProductRespVO {

    @Schema(description = "标准商品ID", example = "1")
    @ExcelIgnore
    private Long id;

    @Schema(description = "商品名称", example = "1")
    @ExcelProperty(value = "商品名称", index = 0)
    @ColumnWidth(25)
    private String name;

    @Schema(description = "品牌ID", example = "1")
    @ExcelIgnore
    private Long brandId;
    @Schema(description = "品牌名称", example = "1")
    @ExcelProperty(value = "品牌名称", index = 1)
    @ColumnWidth(25)
    private String brandName;

    @Schema(description = "商品编码", example = "1")
    @ExcelProperty(value = "标准商品ID", index = 2)
    @ColumnWidth(25)
    private String code;

    @Schema(description = "经营类目ID", example = "1")
    @ExcelIgnore
    private Long businessCategoryId;
    @Schema(description = "经营类目名称", example = "1")
    @ExcelProperty(value = "经营类目", index = 3)
    @ColumnWidth(25)
    private String businessCategoryName;

    @Schema(description = "营销类目ID", example = "1")
    @ExcelIgnore
    private Long marketingCategoryId;

    @Schema(description = "营销类目名称", example = "1")
    @ExcelProperty(value = "营销类目", index = 4)
    @ColumnWidth(25)
    private String marketingCategoryName;

    @Schema(description = "商品成色", example = "1")
    @ExcelIgnore
    private List<Integer> productConditions;
    @ExcelProperty(value = "商品成色", index = 5)
    @ColumnWidth(20)
    private String productConditionsDesc;

    @Schema(description = "监控属性", example = "1")
    @ExcelIgnore
    private List<Integer> monitorAttributes;
    @ExcelProperty(value = "监控属性", index = 6)
    @ColumnWidth(20)
    private String monitorAttributesDesc;

    @Schema(description = "是否草稿 0:否 1:是", example = "1")
    @ExcelIgnore
    private Integer isDraft;
    @ExcelProperty(value = "是否草稿", index = 7)
    @ColumnWidth(20)
    private String isDraftDesc;

    @Schema(description = "商品状态", example = "1")
    @ExcelProperty(value = "状态", index = 8, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "创建人名称", example = "1")
    @ExcelProperty(value = "创建人", index = 9)
    @ColumnWidth(20)
    private String creatorName;

    @Schema(description = "创建时间", example = "1")
    @ExcelProperty(value = "创建时间", index = 10)
    @ColumnWidth(25)
    private Date createTime;

    @Schema(description = "更新人名称", example = "1")
    @ExcelProperty(value = "更新人", index = 11)
    @ColumnWidth(20)
    private String updaterName;

    @Schema(description = "更新时间", example = "1")
    @ExcelProperty(value = "更新时间", index = 12)
    @ColumnWidth(25)
    private Date updateTime;
}
