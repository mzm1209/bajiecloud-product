package com.bajiezu.cloud.product.controller.vo.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.util.Date;

@Data
public class MallProductRespVO {

    @ExcelProperty(value = "商品名称", index = 0)
    @ColumnWidth(25)
    private String marketingProductName;

    @ExcelProperty(value = "商品编码", index = 1)
    @ColumnWidth(25)
    private String marketingProductCode;

    @ExcelProperty(value = "库存", index = 2)
    private Long stock;

    @ExcelProperty(value = "上架渠道", index = 3)
    @ColumnWidth(25)
    private String channelNames;

    @ExcelProperty(value = "商品状态", index = 4)
    @ColumnWidth(20)
    private String statusDesc;

    @ExcelProperty(value = "标准商品名称", index = 5)
    @ColumnWidth(25)
    private String standardProductName;

    @ExcelProperty(value = "标准商品编码", index = 6)
    @ColumnWidth(25)
    private String standardProductCode;

    @ExcelProperty(value = "SKU数量", index = 7)
    @ColumnWidth(20)
    private Long skuCount;

    @ExcelProperty(value = "采购价区间", index = 8)
    @ColumnWidth(25)
    private String officialPriceRange = "0.00~0.00";

    @ExcelProperty(value = "建议售价区间", index = 9)
    @ColumnWidth(25)
    private String SuggestedRetailPriceRange = "0.00~0.00";

    @ExcelProperty(value = "创建人", index = 10)
    @ColumnWidth(20)
    private String creatorName;

    @ExcelProperty(value = "创建时间", index = 11)
    @ColumnWidth(25)
    private Date createTime;

    @ExcelProperty(value = "更新人", index = 12)
    @ColumnWidth(20)
    private String updaterName;

    @ExcelProperty(value = "更新时间", index = 13)
    @ColumnWidth(25)
    private Date updateTime;
}
