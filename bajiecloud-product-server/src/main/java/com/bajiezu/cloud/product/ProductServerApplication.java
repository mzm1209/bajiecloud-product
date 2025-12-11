package com.bajiezu.cloud.product;

import com.bajiezu.cloud.common.mybatis.config.MybatisPlusConfig;
import com.bajiezu.cloud.rpc.mse.NacosThreadConfig;
import com.fhs.trans.config.EasyTransMybatisPlusConfig;
import com.fhs.trans.config.TransServiceConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@Import({MybatisPlusConfig.class, EasyTransMybatisPlusConfig.class, TransServiceConfig.class,
        NacosThreadConfig.class})
@MapperScan("com.bajiezu.cloud.product.dal.mapper.**") // 指定Mapper接口的包路径
public class ProductServerApplication {


    public static void main(String[] args) {
        SpringApplication.run(ProductServerApplication.class, args);
    }


}
