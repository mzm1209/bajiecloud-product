package com.bajiezu.cloud.product.controller.vo.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.util.Date;

/**
 * 租赁商品导出VO
 */
@Data
public class RentalProductRespVO {

    @ExcelProperty(value = "租赁商品名称", index = 0)
    @ColumnWidth(25)
    private String marketingProductName;

    @ExcelProperty(value = "租赁商品编码", index = 1)
    @ColumnWidth(25)
    private String marketingProductCode;

    @ExcelProperty(value = "最低日租金（元/天）", index = 2)
    @ColumnWidth(30)
    private String minDailyRentPrice = "0.00";

    @ExcelProperty(value = "库存", index = 3)
    private Long stock;

    @ExcelProperty(value = "上架渠道", index = 4)
    @ColumnWidth(25)
    private String channelNames;

    @ExcelProperty(value = "商品状态", index = 5)
    @ColumnWidth(20)
    private String statusDesc;

    @ExcelProperty(value = "标准商品名称", index = 6)
    @ColumnWidth(25)
    private String standardProductName;

    @ExcelProperty(value = "标准商品编码", index = 7)
    @ColumnWidth(25)
    private String standardProductCode;

    @ExcelProperty(value = "SKU数量", index = 8)
    @ColumnWidth(20)
    private Long skuCount;

    @ExcelProperty(value = "商品颜色", index = 9)
    @ColumnWidth(20)
    private String color;

    @ExcelProperty(value = "商品规格", index = 10)
    @ColumnWidth(20)
    private String productSpecifications;

    @ExcelProperty(value = "租赁套餐", index = 11)
    @ColumnWidth(20)
    private String leaseType;

    @ExcelProperty(value = "首租租期", index = 12)
    @ColumnWidth(20)
    private String initialLeaseTerm;

    @ExcelProperty(value = "续租租期", index = 13)
    @ColumnWidth(20)
    private String renewalLeaseTerm;

    @ExcelProperty(value = "是否草稿", index = 14)
    @ColumnWidth(20)
    private String isDraftDesc;

    @ExcelProperty(value = "创建人", index = 15)
    @ColumnWidth(20)
    private String creatorName;

    @ExcelProperty(value = "创建时间", index = 16)
    @ColumnWidth(25)
    private Date createTime;

    @ExcelProperty(value = "更新人", index = 17)
    @ColumnWidth(20)
    private String updaterName;

    @ExcelProperty(value = "更新时间", index = 18)
    @ColumnWidth(25)
    private Date updateTime;
}
