package com.bajiezu.cloud.product.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.excel.export.AbstractExportService;
import com.bajiezu.cloud.product.controller.MarketingProductPropertyValueVO;
import com.bajiezu.cloud.product.controller.vo.MarketingProductPropertyVO;
import com.bajiezu.cloud.product.controller.vo.request.MarketingProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.response.MarketingProductRespVO;
import com.bajiezu.cloud.product.controller.vo.response.MallProductRespVO;
import com.bajiezu.cloud.product.controller.vo.response.RecycledProductRespVO;
import com.bajiezu.cloud.product.controller.vo.response.RentalProductRespVO;
import com.bajiezu.cloud.product.enums.ApproveStatusEnum;
import com.bajiezu.cloud.product.enums.ProductTypeEnum;
import com.bajiezu.cloud.product.enums.ShelvesStatusEnum;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketingProductExportService extends AbstractExportService {

    private static final int PAGE_SIZE = 100;
    private static final double PRICE_SCALE = 10000.0;
    private static final String COLOR = "颜色";
    private static final String PRODUCT_SPECIFICATIONS = "规格";
    private static final String LEASE_TYPE = "租赁方式";
    private static final String INITIAL_LEASE_TERM = "租期(天)";
    private static final String RENEWAL_LEASE_TERM = "续租租期(天)";

    private Integer productType;

    @Resource
    private MarketingProductService marketingProductService;

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    @Override
    protected String generateFileName() {
        ProductTypeEnum productTypeEnum = ProductTypeEnum.get(productType);
        return productTypeEnum.getDesc() + "_" + System.currentTimeMillis() + EXPORT_FILE_TYPE;
    }

    @Override
    protected String createExportFile(String fileName, Object params) {
        log.info("开始导出营销商品,params:{}", params);

        try {
            ProductTypeEnum productTypeEnum = ProductTypeEnum.get(productType);
            int pageNo = 1;
            MarketingProductListReqVO marketingProductListReqVO = (MarketingProductListReqVO) params;
            marketingProductListReqVO.setPageNo(pageNo);
            marketingProductListReqVO.setPageSize(PAGE_SIZE);

            List<RentalProductRespVO> rentalProductRespVOS = Lists.newArrayList(); // 租赁商品
            List<MallProductRespVO> mallProductRespVOS = Lists.newArrayList();  // 售卖、实物、虚拟商品
            List<RecycledProductRespVO> recycledProductRespVOS = Lists.newArrayList(); // 回收商品

            while (true) {
                PageResult<MarketingProductRespVO> marketingProductRespVOPageResult = marketingProductService.page(marketingProductListReqVO);
                if (CollectionUtil.isEmpty(marketingProductRespVOPageResult.getList())) {
                    break;
                }

                switch (productTypeEnum) {
                    case RENTAL_PRODUCT: // 租赁商品
                        processRentalProducts(marketingProductRespVOPageResult.getList(), rentalProductRespVOS);
                        break;
                    case RECYCLED_PRODUCT: // 回收商品
                        processRecycledProducts(marketingProductRespVOPageResult.getList(), recycledProductRespVOS);
                        break;
                    case PRODUCT_FOR_SALE: // 售卖商品
                    case PHYSICAL_PRODUCT: // 实物商品
                    case VIRTUAL_PRODUCT: // 虚拟商品
                        processMallProducts(marketingProductRespVOPageResult.getList(), mallProductRespVOS);
                        break;
                    default:
                        break;
                }

                pageNo++;
                marketingProductListReqVO.setPageNo(pageNo);
            }

            String filePath = EXPORT_DIR + fileName;
            String sheetName = productTypeEnum.getDesc();
            switch (productTypeEnum) {
                case RENTAL_PRODUCT: // 租赁商品
                    writeExcel(filePath, sheetName, RentalProductRespVO.class, rentalProductRespVOS);
                    break;
                case RECYCLED_PRODUCT: // 回收商品
                    writeExcel(filePath, sheetName, RecycledProductRespVO.class, recycledProductRespVOS);
                    break;
                case PRODUCT_FOR_SALE: // 售卖商品
                case PHYSICAL_PRODUCT: // 实物商品
                case VIRTUAL_PRODUCT: // 虚拟商品
                    writeExcel(filePath, sheetName, MallProductRespVO.class, mallProductRespVOS);
                    break;
                default:
                    break;
            }

            log.info("导出营销商品完成,filePath:{}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("导出营销商品失败,params:{}", params, e);
            throw new RuntimeException("导出营销商品失败", e);
        }
    }

    private void processRentalProducts(List<MarketingProductRespVO> marketingProductRespVOs, List<RentalProductRespVO> rentalProductRespVOS) {
        for (MarketingProductRespVO marketingProductRespVO : marketingProductRespVOs) {
            RentalProductRespVO rentalProductRespVO = new RentalProductRespVO();
            rentalProductRespVOS.add(rentalProductRespVO);

            rentalProductRespVO.setMarketingProductName(marketingProductRespVO.getMarketingProductName());
            rentalProductRespVO.setMarketingProductCode(marketingProductRespVO.getMarketingProductCode());
            if (Objects.nonNull(marketingProductRespVO.getMinDailyRentPrice())) {
                rentalProductRespVO.setMinDailyRentPrice(formatPrice(marketingProductRespVO.getMinDailyRentPrice()));
            }
            rentalProductRespVO.setStock(marketingProductRespVO.getStock());
            setChannelNames(rentalProductRespVO, marketingProductRespVO);
            setStatusDesc(rentalProductRespVO, marketingProductRespVO);
            rentalProductRespVO.setStandardProductName(marketingProductRespVO.getStandardProductName());
            rentalProductRespVO.setStandardProductCode(marketingProductRespVO.getStandardProductCode());
            rentalProductRespVO.setSkuCount(marketingProductRespVO.getSkuCount());

            for (MarketingProductPropertyVO marketingProductPropertyVO : marketingProductRespVO.getProperties()) {
                String values = "";
                if (CollectionUtil.isNotEmpty(marketingProductPropertyVO.getPropertyValues())) {
                    values = marketingProductPropertyVO.getPropertyValues().stream().map(
                            MarketingProductPropertyValueVO::getValue).collect(Collectors.joining("；"));
                }

                if (marketingProductPropertyVO.getPropertyName().equals(COLOR)) {
                    rentalProductRespVO.setColor(values);
                } else if (marketingProductPropertyVO.getPropertyName().equals(PRODUCT_SPECIFICATIONS)) {
                    rentalProductRespVO.setProductSpecifications(values);
                } else if (marketingProductPropertyVO.getPropertyName().equals(LEASE_TYPE)) {
                    rentalProductRespVO.setLeaseType(values);
                } else if (marketingProductPropertyVO.getPropertyName().equals(INITIAL_LEASE_TERM)) {
                    rentalProductRespVO.setInitialLeaseTerm(values);
                } else if (marketingProductPropertyVO.getPropertyName().equals(RENEWAL_LEASE_TERM)) {
                    rentalProductRespVO.setRenewalLeaseTerm(values);
                }
            }

            rentalProductRespVO.setIsDraftDesc(marketingProductRespVO.getIsDraft() == 0 ? "否" : "是");
            rentalProductRespVO.setCreatorName(marketingProductRespVO.getCreatorName());
            rentalProductRespVO.setCreateTime(marketingProductRespVO.getCreateTime());
            rentalProductRespVO.setUpdaterName(marketingProductRespVO.getUpdaterName());
            rentalProductRespVO.setUpdateTime(marketingProductRespVO.getUpdateTime());
        }
    }

    /**
     * 处理回收商品
     */
    private void processRecycledProducts(List<MarketingProductRespVO> marketingProductRespVOs, List<RecycledProductRespVO> recycledProductRespVOS) {
        for (MarketingProductRespVO marketingProductRespVO : marketingProductRespVOs) {
            RecycledProductRespVO recycledProductRespVO = new RecycledProductRespVO();
            recycledProductRespVOS.add(recycledProductRespVO);

            recycledProductRespVO.setMarketingProductName(marketingProductRespVO.getMarketingProductName());
            recycledProductRespVO.setMarketingProductCode(marketingProductRespVO.getMarketingProductCode());
            setChannelNames(recycledProductRespVO, marketingProductRespVO);
            setStatusDesc(recycledProductRespVO, marketingProductRespVO);
            recycledProductRespVO.setStandardProductName(marketingProductRespVO.getStandardProductName());
            recycledProductRespVO.setStandardProductCode(marketingProductRespVO.getStandardProductCode());

            // 最低回收价
            if (marketingProductRespVO.getMinBuybackPrice() != null) {
                recycledProductRespVO.setMinBuybackPrice(formatPrice(marketingProductRespVO.getMinBuybackPrice()));
            }
            // 最高回收价
            if (marketingProductRespVO.getMaxBuybackPrice() != null) {
                recycledProductRespVO.setMaxBuybackPrice(formatPrice(marketingProductRespVO.getMaxBuybackPrice()));
            }

            recycledProductRespVO.setCreatorName(marketingProductRespVO.getCreatorName());
            recycledProductRespVO.setCreateTime(marketingProductRespVO.getCreateTime());
            recycledProductRespVO.setUpdaterName(marketingProductRespVO.getUpdaterName());
            recycledProductRespVO.setUpdateTime(marketingProductRespVO.getUpdateTime());
        }
    }

    /**
     * 处理商场商品（实物、虚拟）
     */
    private void processMallProducts(List<MarketingProductRespVO> marketingProductRespVOs, List<MallProductRespVO> mallProductRespVOS) {
        for (MarketingProductRespVO marketingProductRespVO : marketingProductRespVOs) {
            MallProductRespVO mallProductRespVO = new MallProductRespVO();
            mallProductRespVOS.add(mallProductRespVO);

            mallProductRespVO.setMarketingProductName(marketingProductRespVO.getMarketingProductName());
            mallProductRespVO.setMarketingProductCode(marketingProductRespVO.getMarketingProductCode());
            mallProductRespVO.setStock(marketingProductRespVO.getStock());
            setChannelNames(mallProductRespVO, marketingProductRespVO);
            setStatusDesc(mallProductRespVO, marketingProductRespVO);
            mallProductRespVO.setStandardProductName(marketingProductRespVO.getStandardProductName());
            mallProductRespVO.setStandardProductCode(marketingProductRespVO.getStandardProductCode());
            mallProductRespVO.setSkuCount(marketingProductRespVO.getSkuCount());

            // 采购价区间
            if (marketingProductRespVO.getMinOfficialPrice() != null && marketingProductRespVO.getMaxOfficialPrice() != null) {
                mallProductRespVO.setOfficialPriceRange(formatPriceRange(marketingProductRespVO.getMinOfficialPrice(), marketingProductRespVO.getMaxOfficialPrice()));
            }
            // 建议售价区间
            if (marketingProductRespVO.getMinSuggestedRetailPrice() != null && marketingProductRespVO.getMaxSuggestedRetailPrice() != null) {
                mallProductRespVO.setSuggestedRetailPriceRange(formatPriceRange(marketingProductRespVO.getMinSuggestedRetailPrice(), marketingProductRespVO.getMaxSuggestedRetailPrice()));
            }

            mallProductRespVO.setCreatorName(marketingProductRespVO.getCreatorName());
            mallProductRespVO.setCreateTime(marketingProductRespVO.getCreateTime());
            mallProductRespVO.setUpdaterName(marketingProductRespVO.getUpdaterName());
            mallProductRespVO.setUpdateTime(marketingProductRespVO.getUpdateTime());
        }
    }

    /**
     * 设置渠道名称
     */
    private void setChannelNames(Object target, MarketingProductRespVO source) {
        if (target instanceof MallProductRespVO) {
            if (CollectionUtil.isNotEmpty(source.getChannelNames())) {
                ((MallProductRespVO) target).setChannelNames(String.join("；", source.getChannelNames()));
            }
        } else if (target instanceof RecycledProductRespVO) {
            if (CollectionUtil.isNotEmpty(source.getChannelNames())) {
                ((RecycledProductRespVO) target).setChannelNames(String.join("；", source.getChannelNames()));
            }
        } else if (target instanceof RentalProductRespVO) {
            if (CollectionUtil.isNotEmpty(source.getChannelNames())) {
                ((RentalProductRespVO) target).setChannelNames(String.join("；", source.getChannelNames()));
            }
        }
    }

    /**
     * 设置状态描述
     */
    private void setStatusDesc(Object target, MarketingProductRespVO source) {
        String statusDesc;
        if (ApproveStatusEnum.APPROVE_PASS.getValue().equals(source.getApproveStatus())) {
            statusDesc = ShelvesStatusEnum.get(source.getShelvesStatus()).getDesc();
        } else {
            statusDesc = ApproveStatusEnum.get(source.getApproveStatus()).getDesc();
        }

        if (target instanceof MallProductRespVO) {
            ((MallProductRespVO) target).setStatusDesc(statusDesc);
        } else if (target instanceof RecycledProductRespVO) {
            ((RecycledProductRespVO) target).setStatusDesc(statusDesc);
        } else if (target instanceof RentalProductRespVO) {
            ((RentalProductRespVO) target).setStatusDesc(statusDesc);
        }
    }

    /**
     * 格式化价格
     */
    private String formatPrice(Long price) {
        return String.format("%.2f", price / PRICE_SCALE);
    }

    /**
     * 格式化价格区间
     */
    private String formatPriceRange(Long minPrice, Long maxPrice) {
        return formatPrice(minPrice) + "~" + formatPrice(maxPrice);
    }

    /**
     * 写入Excel文件
     */
    private <T> void writeExcel(String filePath, String sheetName, Class<T> clazz, List<T> dataList) {
        try (ExcelWriter excelWriter = EasyExcelFactory.write(filePath, clazz).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
            excelWriter.write(dataList, writeSheet);
        }
    }
}
