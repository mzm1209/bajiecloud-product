package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductAddReqVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductApproveReqVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.request.OnOffShelvesReqVO;
import com.bajiezu.cloud.product.controller.vo.response.MarketingProductDetailRespVO;
import com.bajiezu.cloud.product.controller.vo.response.MarketingProductRespVO;
import com.bajiezu.cloud.product.controller.vo.response.ProductTypeStatisticRespVO;
import com.bajiezu.cloud.product.controller.vo.response.StatusStatisticRespVO;
import com.bajiezu.cloud.product.dal.dto.ApproveStatusStatisticCountDTO;
import com.bajiezu.cloud.product.dal.dto.ProductTypeStatisticCountDTO;
import com.bajiezu.cloud.product.dal.dto.ShelvesStatisticCountDTO;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.enums.ApproveStatusEnum;
import com.bajiezu.cloud.product.enums.ProductTypeEnum;
import com.bajiezu.cloud.product.enums.ShelvesStatusEnum;
import com.bajiezu.cloud.product.service.MarketingProductService;
import com.bajiezu.cloud.product.util.SequenceGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MarketingProductServiceImpl implements MarketingProductService {

    @Resource
    private SequenceGenerator sequenceGenerator;
    @Resource
    private MarketingProductSpuMapper spuMapper;
    @Resource
    private MarketingProductSpuPropertyMapper spuPropertyMapper;
    @Resource
    private MarketingProductSpuPropertyValueMapper spuPropertyValueMapper;
    @Resource
    private MarketingProductSkuMapper skuMapper;
    @Resource
    private MarketingProductSkuPropertyValueMapper skuPropertyValueMapper;

    @Override
    public PageResult<MarketingProductRespVO> page(MarketingProductListReqVO reqVO) {
        return null;
    }

    @Override
    public ProductTypeStatisticRespVO productTypeStatistic() {

        List<ProductTypeStatisticCountDTO> productTypeStatisticCountDTOS = spuMapper.productTypeStatistic();
        ProductTypeStatisticRespVO productTypeStatisticRespVO = new ProductTypeStatisticRespVO();
        for (ProductTypeStatisticCountDTO productTypeStatisticCountDTO : productTypeStatisticCountDTOS) {
            if (productTypeStatisticCountDTO.getType() == null) {
                continue;
            }
            ProductTypeEnum productTypeEnum = ProductTypeEnum.get(productTypeStatisticCountDTO.getType());
            switch (productTypeEnum) {
                case RENTAL_PRODUCT:
                    productTypeStatisticRespVO.setRentalProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                case PRODUCT_FOR_SALE:
                    productTypeStatisticRespVO.setProductForSaleCount(productTypeStatisticCountDTO.getCount());
                    break;
                case RECYCLED_PRODUCT:
                    productTypeStatisticRespVO.setRecycledProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                case PHYSICAL_PRODUCT:
                    productTypeStatisticRespVO.setPhysicalProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                case VIRTUAL_PRODUCT:
                    productTypeStatisticRespVO.setVirtualProductCount(productTypeStatisticCountDTO.getCount());
                    break;
                default:
                    break;
            }
        }
        return productTypeStatisticRespVO;
    }

    @Override
    public void add(MarketingProductAddReqVO reqVO) {

    }

    @Override
    public void mod(MarketingProductAddReqVO reqVO) {

    }

    @Override
    public MarketingProductDetailRespVO detail(Long id) {
        return null;
    }

    @Override
    public void del(List<Long> ids) {

    }

    @Override
    public void onOffShelves(OnOffShelvesReqVO reqVO) {

    }

    @Override
    public void approve(MarketingProductApproveReqVO reqVO) {

    }

    @Override
    public StatusStatisticRespVO statusStatistic(MarketingProductListReqVO reqVO) {
        log.info("查询商品状态统计信息,reqVO:{}", reqVO);

        // 获取商品总数
        Integer totalCount = spuMapper.queryCount(reqVO);

        // 获取草稿商品数
        reqVO.setIsDraft(1);
        Integer draftCount = spuMapper.queryCount(reqVO);

        // 获取审核商品数
        reqVO.setIsDraft(null);
        List<ApproveStatusStatisticCountDTO> approveStatusStatisticCountDTOS = spuMapper.approveStatusStatistic(reqVO);

        // 获取上下架商品数
        List<ShelvesStatisticCountDTO> shelvesStatisticCountDTOS = spuMapper.shelvesStatistic(reqVO);

        StatusStatisticRespVO statusStatisticRespVO = new StatusStatisticRespVO();
        statusStatisticRespVO.setTotalCount(totalCount);
        statusStatisticRespVO.setDraftCount(draftCount);
        for (ApproveStatusStatisticCountDTO approveStatusStatisticCountDTO : approveStatusStatisticCountDTOS) {
            if (approveStatusStatisticCountDTO.getApproveStatus() == null) {
                continue;
            }
            ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.get(approveStatusStatisticCountDTO.getApproveStatus());
            switch (approveStatusEnum) {
                case WAIT_APPROVE:
                    statusStatisticRespVO.setWaitApproveCount(approveStatusStatisticCountDTO.getCount());
                    break;
                case APPROVE_PASS:
                    statusStatisticRespVO.setApprovePassCount(approveStatusStatisticCountDTO.getCount());
                    break;
                case APPROVE_REJECT:
                    statusStatisticRespVO.setApproveRejectCount(approveStatusStatisticCountDTO.getCount());
            }
        }
        for (ShelvesStatisticCountDTO shelvesStatisticCountDTO : shelvesStatisticCountDTOS) {
            if (shelvesStatisticCountDTO.getShelvesStatus() == null) {
                continue;
            }
            ShelvesStatusEnum shelvesStatusEnum = ShelvesStatusEnum.get(shelvesStatisticCountDTO.getShelvesStatus());
            switch (shelvesStatusEnum) {
                case WAIT_SHELVES:
                    statusStatisticRespVO.setWaitShelvesCount(shelvesStatisticCountDTO.getCount());
                    break;
                case ON_SHELVES:
                    statusStatisticRespVO.setOnShelvesCount(shelvesStatisticCountDTO.getCount());
                    break;
                case OFF_SHELVES:
                    statusStatisticRespVO.setOffShelvesCount(shelvesStatisticCountDTO.getCount());
                    break;
                default:
                    break;
            }
        }
        return statusStatisticRespVO;
    }
}
