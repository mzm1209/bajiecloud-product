package com.bajiezu.cloud.product.controller.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Schema(description = "商品标签列表响应结果")
@Data
public class ProductTagRespVO {

    @Schema(description = "商品标签ID")
    private Long id;

    @Schema(description = "商品标签名称")
    private String name;

    @Schema(description = "商品标签图片,多张图片用英文逗号分隔")
    private String picUrl;

    @Schema(description = "商品标签描述")
    private String remark;

    @Schema(description = "商品标签状态")
    private Integer status;

    @Schema(description = "商品标签创建时间")
    private Date createTime;

    @Schema(description = "商品标签排序")
    private Integer sort;

    @Schema(description = "商品标签是否高亮 0:否 1:是")
    private Integer isHighlight;

    @Schema(description = "商品标签显示页面 1:SKU页 2:商品详情页")
    private List<Integer> showPages;

    @Schema(description = "商品标签下商品数量")
    private Long productCount;
}
