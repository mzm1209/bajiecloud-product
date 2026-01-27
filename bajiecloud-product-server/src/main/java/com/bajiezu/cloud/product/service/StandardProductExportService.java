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
import org.springframework.stereotype.Service;

import java.util.List;


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
            standardProductRespVOS.addAll(standardProductRespVOPageResult.getList());
            pageNo++;
            standardProductListReqVO.setPageNo(pageNo);
            standardProductRespVOPageResult = standardProductService.page(standardProductListReqVO);
        }

        String filePath = EXPORT_DIR + fileName;
        String sheetName = "字典类型";
        ExcelWriter excelWriter = EasyExcelFactory.write(filePath, StandardProductRespVO.class).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
        excelWriter.write(standardProductRespVOS, writeSheet);
        excelWriter.finish();
        return filePath;
    }
}
