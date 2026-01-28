package com.bajiezu.cloud.product.dal.mapper;

import com.bajiezu.cloud.product.dal.dto.*;
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 营销商品SPU表 Mapper 接口
 */
@Mapper
public interface MarketingProductSpuMapper extends BaseMapper<MarketingProductSpu> {

    List<ProductTypeStatisticCountDTO> productTypeStatistic();

    Integer queryCount(@Param("query") MarketingProductQuery query);

    List<ApproveStatusStatisticCountDTO> approveStatusStatistic(@Param("query") MarketingProductQuery query);

    List<ShelvesStatisticCountDTO> shelvesStatistic(@Param("query") MarketingProductQuery query);

    void updateShelvesStatusByIds(@Param("ids") List<Long> ids, @Param("shelvesStatus") Integer shelvesStatus,
                                  @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    void logicDeleteByIds(@Param("ids") List<Long> ids, @Param("updateBy") Long updateBy, @Param("updateTime") Date updateTime);

    List<MarketingProductSpu> selectListByQuery(@Param("query") MarketingProductQuery query);

    Long selectCountByQuery(@Param("query") MarketingProductQuery query);

    List<MarketingProductSpu> selectListByIds(@Param("ids") Collection<Long> ids);

    List<MarketingProductSpu> selectListByCondition(ProductQuery query);

    Long selectCountByCondition(ProductQuery query);

    void updateApproveAndShelvesStatusByIds(@Param("ids") List<Long> ids, @Param("approveStatus") Integer approveStatus,
                                            @Param("shelvesStatus") Integer shelvesStatus, @Param("updateBy") Long updateBy,
                                            @Param("updateTime") Date updateTime);
}