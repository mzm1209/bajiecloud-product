package com.bajiezu.cloud.product.job;

import com.bajiezu.cloud.product.service.MarketingProductService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ProductXxlJob {

    @Resource
    private MarketingProductService marketingProductService;

    @XxlJob("updateProductShelvesStatusJob")
    public ReturnT<String> updateProductShelvesStatus() {
        log.info("updateProductShelvesStatus, start...");
        try {
            marketingProductService.updateProductShelvesStatus();
            log.info("updateProductShelvesStatus, finished");
            return ReturnT.SUCCESS;
        } catch (Exception ex) {
            log.error("updateProductShelvesStatus, error", ex);
            return ReturnT.FAIL;
        }
    }
}