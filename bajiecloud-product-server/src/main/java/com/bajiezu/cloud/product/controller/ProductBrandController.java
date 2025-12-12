package com.bajiezu.cloud.product.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 品牌管理")
@RestController
@RequestMapping("/product/brand")
@Validated
@Slf4j
public class ProductBrandController {


}
