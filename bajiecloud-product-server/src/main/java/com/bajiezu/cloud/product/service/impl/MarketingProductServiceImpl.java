package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
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
import com.bajiezu.cloud.product.dal.entity.MarketingProductSpu;
import com.bajiezu.cloud.product.dal.mapper.*;
import com.bajiezu.cloud.product.enums.ApproveStatusEnum;
import com.bajiezu.cloud.product.enums.ProductTypeEnum;
import com.bajiezu.cloud.product.enums.ShelvesStatusEnum;
import com.bajiezu.cloud.product.service.MarketingProductService;
import com.bajiezu.cloud.product.util.SequenceGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.*;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void del(List<Long> ids) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("del ids: {},operatorId:{}", ids, loginUser.getId());
        if (CollUtil.isNotEmpty(ids)) {
            return;
        }
        // 批量逻辑删除
        //spuMapper.logicDeleteByIds(ids, loginUser.getId(), new Date());

    }

    @Override
    public void onOffShelves(OnOffShelvesReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("onOffShelves reqVO: {},operatorId:{}", reqVO, loginUser.getId());
        if (CollUtil.isNotEmpty(reqVO.getIds())) {
            return;
        }

        // 批量更新商品的上下架状态
        spuMapper.updateShelvesStatusByIds(reqVO.getIds(), reqVO.getShelvesStatus(), loginUser.getId(), new Date());
    }

    @Override
    public void approve(MarketingProductApproveReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("status change product approve dto: {},operatorId:{}", reqVO, loginUser.getId());
        MarketingProductSpu marketingProductSpu = spuMapper.selectById(reqVO.getId());
        if (marketingProductSpu == null) {
            throw exception(MARKETING_PRODUCT_NOT_EXIST);
        }
        if (NumberUtils.INTEGER_ONE.equals(marketingProductSpu.getIsDeleted())) {
            throw exception(MARKETING_PRODUCT_DELETED);
        }
        if (NumberUtils.INTEGER_ONE.equals(marketingProductSpu.getIsDraft())) {
            throw exception(MARKETING_PRODUCT_IS_DRAFT);
        }
        if (Objects.equals(marketingProductSpu.getApprovalStatus(), reqVO.getApproveStatus())) {
            log.info("商品状态未改变,productId:{}", marketingProductSpu.getId());
            return;
        }
        if (!Objects.equals(marketingProductSpu.getApprovalStatus(), ApproveStatusEnum.WAIT_APPROVE.getValue())) {
            throw exception(MARKETING_PRODUCT_STATUS_NOT_WAIT_APPROVE);
        }

        marketingProductSpu.setApproverId(loginUser.getId());
        marketingProductSpu.setApprovalStatus(reqVO.getApproveStatus());
        marketingProductSpu.setApprovalRemark(reqVO.getApprovalRemark());
        marketingProductSpu.setUpdateTime(new Date());
        marketingProductSpu.setUpdateBy(loginUser.getId());
        spuMapper.updateById(marketingProductSpu);
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
