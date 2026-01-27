package com.bajiezu.cloud.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.bajiezu.cloud.product.dal.mapper") // 指定Mapper接口的包路径
@EnableFeignClients(basePackages = {"com.bajiezu.cloud.system.api", "com.bajiezu.cloud.marketing.api"})
public class ProductServerApplication {


    public static void main(String[] args) {
        SpringApplication.run(ProductServerApplication.class, args);
    }


}
