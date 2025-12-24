package com.bajiezu.cloud.product.config;

import com.bajiezu.cloud.system.api.dict.DictDataApi;
import com.bajiezu.cloud.system.api.partner.BusinessPartnerApi;
import com.bajiezu.cloud.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "productRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, BusinessPartnerApi.class, DictDataApi.class})
public class RpcConfiguration {

}
