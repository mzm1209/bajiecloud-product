package com.bajiezu.cloud.product.service.impl;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.response.ProductBusinessCategoryRespVO;
import com.bajiezu.cloud.product.dal.entity.ProductBusinessCategory;
import com.bajiezu.cloud.product.service.ProductBusinessCategoryService;
import com.bajiezu.cloud.product.dal.mapper.ProductBusinessCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductBusinessCategoryServiceImpl implements ProductBusinessCategoryService {
    @Autowired
    private ProductBusinessCategoryMapper productBusinessCategoryMapper;

    @Override
    public PageResult<ProductBusinessCategory> list() {
        // 查询所有未删除的经营类目数据
        List<ProductBusinessCategory> categories = productBusinessCategoryMapper.selectList(
                new QueryWrapper<ProductBusinessCategory>().eq("is_deleted", 0)
        );

        // 转换为响应VO对象
        List<ProductBusinessCategory> respList = categories.stream()
                .map(category -> {
                    ProductBusinessCategory respVO = new ProductBusinessCategory();
                    respVO.setId(category.getId());
                    respVO.setName(category.getName());
                    respVO.setParentId(category.getParentId());
                    respVO.setPartnerId(category.getPartnerId());
                    respVO.setCreateBy(category.getCreateBy());
                    respVO.setUpdateBy(category.getUpdateBy());
                    respVO.setCreateTime(category.getCreateTime());
                    respVO.setUpdateTime(category.getUpdateTime());
                    return respVO;
                })
                .collect(Collectors.toList());

        // 返回分页结果，总数量为查询到的数据量
        return new PageResult<>(respList, (long) respList.size());
    }

}
