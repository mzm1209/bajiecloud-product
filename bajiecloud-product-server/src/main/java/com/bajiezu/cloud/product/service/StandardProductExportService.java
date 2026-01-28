package com.bajiezu.cloud.product.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.bajiezu.cloud.common.web.cloud.utils.object.BeanUtils;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.excel.export.AbstractExportService;
import com.bajiezu.cloud.product.controller.vo.request.StandardProductListReqVO;
import com.bajiezu.cloud.product.controller.vo.response.StandardProductRespVO;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class StandardProductExportService extends AbstractExportService {

    @Resource
    private StandardProductService standardProductService;

    @Override
    protected String generateFileName() {
        return "标准商品_" + System.currentTimeMillis() + EXPORT_FILE_TYPE;
    }

    @Override
    protected String createExportFile(String fileName, Object params) {
        log.info("开始导出标准商品,params:{}", params);
        int pageNo = 1;
        StandardProductListReqVO standardProductListReqVO = (StandardProductListReqVO) params;
        standardProductListReqVO.setPageNo(pageNo);
        standardProductListReqVO.setPageSize(100);
        PageResult<StandardProductRespVO> standardProductRespVOPageResult = standardProductService.page(standardProductListReqVO);
        List<StandardProductRespVO> standardProductRespVOS = Lists.newArrayList();
        while (CollectionUtil.isNotEmpty(standardProductRespVOPageResult.getList())) {
            for (StandardProductRespVO standardProductRespVO : standardProductRespVOPageResult.getList()) {
                // 设置是否为草稿描述
                setDraftDescription(standardProductRespVO);
                // 设置商品成色描述
                setProductConditionsDescription(standardProductRespVO);
                // 设置监管属性描述
                setMonitorAttributesDescription(standardProductRespVO);
                standardProductRespVOS.add(standardProductRespVO);
            }
            pageNo++;
            standardProductListReqVO.setPageNo(pageNo);
            standardProductRespVOPageResult = standardProductService.page(standardProductListReqVO);
        }

        String filePath = EXPORT_DIR + fileName;
        String sheetName = "标准商品";
        ExcelWriter excelWriter = EasyExcelFactory.write(filePath, StandardProductRespVO.class).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
        excelWriter.write(standardProductRespVOS, writeSheet);
        excelWriter.finish();
        return filePath;
    }

    /**
     * 设置是否为草稿的描述文本
     */
    private void setDraftDescription(StandardProductRespVO product) {
        product.setIsDraftDesc(NumberUtils.INTEGER_ZERO.equals(product.getIsDraft()) ? "否" : "是");
    }

    /**
     * 设置商品成色描述文本
     */
    private void setProductConditionsDescription(StandardProductRespVO product) {
        List<Integer> conditions = product.getProductConditions();
        if (CollectionUtil.isNotEmpty(conditions)) {
            String desc = conditions.stream()
                    .map(condition -> NumberUtils.INTEGER_ZERO.equals(condition) ? "全新" : "非全新")
                    .collect(Collectors.joining("、"));
            product.setProductConditionsDesc(desc);
        } else {
            product.setProductConditionsDesc("");
        }
    }

    /**
     * 设置监管属性描述文本
     */
    private void setMonitorAttributesDescription(StandardProductRespVO product) {
        List<Integer> attributes = product.getMonitorAttributes();
        if (CollectionUtil.isNotEmpty(attributes)) {
            String desc = attributes.stream()
                    .map(attribute -> NumberUtils.INTEGER_ZERO.equals(attribute) ? "监管" : "非监管")
                    .collect(Collectors.joining("、"));
            product.setMonitorAttributesDesc(desc);
        } else {
            product.setMonitorAttributesDesc("");
        }
    }
}
