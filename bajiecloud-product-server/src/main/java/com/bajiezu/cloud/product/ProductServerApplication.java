package com.bajiezu.cloud.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bajiezu.cloud.product.dal.mapper.**") // 指定Mapper接口的包路径
public class ProductServerApplication {


    public static void main(String[] args) {
        SpringApplication.run(ProductServerApplication.class, args);
    }


}
