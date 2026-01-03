package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.controller.vo.request.MarketingProductListReqVO;
import com.bajiezu.cloud.product.dal.dto.ApproveStatusStatisticCountDTO;
import com.bajiezu.cloud.product.dal.dto.ProductTypeStatisticCountDTO;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 营销商品SPU表 Mapper 接口
 */
@Mapper
public interface MarketingProductSpuMapper extends BaseMapper<MarketingProductSpu> {

    List<ProductTypeStatisticCountDTO> productTypeStatistic();

    Integer queryCount(@Param("reqVO") MarketingProductListReqVO reqVO);

    List<ApproveStatusStatisticCountDTO> approveStatusStatistic(@Param("reqVO") MarketingProductListReqVO reqVO);
}