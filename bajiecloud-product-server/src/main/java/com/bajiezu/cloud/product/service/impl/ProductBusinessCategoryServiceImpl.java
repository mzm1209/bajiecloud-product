package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.web.cloud.utils.object.BeanUtils;
import com.bajiezu.cloud.common.web.pojo.PageParam;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.response.BusinessCategoryRespVO;
import com.bajiezu.cloud.product.dal.entity.ProductBusinessCategory;
import com.bajiezu.cloud.product.service.ProductBusinessCategoryService;
import com.bajiezu.cloud.product.dal.mapper.ProductBusinessCategoryMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ProductBusinessCategoryServiceImpl implements ProductBusinessCategoryService {


    @Resource
    private ProductBusinessCategoryMapper businessCategoryMapper;

    @Override
    public PageResult<BusinessCategoryRespVO> page(PageParam pageParam) {
        int offset = (pageParam.getPageNo() - 1) * pageParam.getPageSize();

        // 查询一级类目
        int level = NumberUtils.INTEGER_ONE;
        List<ProductBusinessCategory> levelCategories = businessCategoryMapper.selectByLevel(level, offset, pageParam.getPageSize());
        if (CollectionUtil.isEmpty(levelCategories)) {
            return PageResult.empty();
        }
        List<BusinessCategoryRespVO> firstLevelCategoryRespVOList = levelCategories.stream().map(category -> {
            BusinessCategoryRespVO respVO = new BusinessCategoryRespVO();
            BeanUtils.copyProperties(category, respVO);
            return respVO;
        }).toList();
        long count = businessCategoryMapper.selectCountByLevel(level);

        // 获取一级类目的id
        List<Long> categoryIds = levelCategories.stream().map(ProductBusinessCategory::getId).toList();
        // 批量查询所有子孙类目 （使用路径前缀匹配）
        List<ProductBusinessCategory> childrenCategories = businessCategoryMapper.batchSelectByPathPrefix(categoryIds);
        List<BusinessCategoryRespVO> childrenCategoryRespVOList = childrenCategories.stream().map(category -> {
            BusinessCategoryRespVO respVO = new BusinessCategoryRespVO();
            BeanUtils.copyProperties(category, respVO);
            return respVO;
        }).toList();

        // 根据一级类目构造成树形结构
        List<BusinessCategoryRespVO> treeList = buildTree(firstLevelCategoryRespVOList, childrenCategoryRespVOList);
        return new PageResult<>(treeList, count);
    }

    @Override
    public List<BusinessCategoryRespVO> listAll() {
        List<ProductBusinessCategory> allCategories = businessCategoryMapper.queryAll();
        if (CollectionUtil.isEmpty(allCategories)) {
            return Collections.emptyList();
        }

        List<BusinessCategoryRespVO> firstLevelCategoryRespVOList = Lists.newArrayList();
        List<BusinessCategoryRespVO> childrenCategoryRespVOList = Lists.newArrayList();
        for (ProductBusinessCategory category : allCategories) {
            BusinessCategoryRespVO respVO = new BusinessCategoryRespVO();
            BeanUtils.copyProperties(category, respVO);
            if (NumberUtils.INTEGER_ONE.equals(category.getLevel())) {
                firstLevelCategoryRespVOList.add(respVO);
            } else {
                childrenCategoryRespVOList.add(respVO);
            }
        }

        return buildTree(firstLevelCategoryRespVOList, childrenCategoryRespVOList);
    }

    /**
     * 构建树形结构
     *
     * @param rootCategories 根节点列表
     * @param allCategories 所有节点列表（包含根节点和子节点）
     * @return 构建后的树形结构
     */
    private List<BusinessCategoryRespVO> buildTree(List<BusinessCategoryRespVO> rootCategories,
                                                    List<BusinessCategoryRespVO> allCategories) {
        // 创建一个Map，以父ID为key，子节点列表为value
        Map<Long, List<BusinessCategoryRespVO>> childrenMap = Maps.newHashMap();

        // 遍历所有分类，构建父ID到子节点列表的映射
        for (BusinessCategoryRespVO category : allCategories) {
            Long parentId = category.getParentId();
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
        }

        // 递归构建树形结构
        return buildTreeRecursive(rootCategories, childrenMap);
    }

    /**
     * 递归构建树形结构
     *
     * @param categories 当前层级的节点
     * @param childrenMap 子节点映射表
     * @return 构建后的树形结构
     */
    private List<BusinessCategoryRespVO> buildTreeRecursive(List<BusinessCategoryRespVO> categories,
                                                             Map<Long, List<BusinessCategoryRespVO>> childrenMap) {
        List<BusinessCategoryRespVO> result = new ArrayList<>();

        for (BusinessCategoryRespVO category : categories) {
            // 复制当前节点
            BusinessCategoryRespVO node = new BusinessCategoryRespVO();
            BeanUtils.copyProperties(category, node);

            // 获取当前节点的子节点
            List<BusinessCategoryRespVO> children = childrenMap.get(category.getId());
            if (CollectionUtil.isNotEmpty(children)) {
                // 递归构建子节点的树形结构
                List<BusinessCategoryRespVO> childTree = buildTreeRecursive(children, childrenMap);
                // 这里需要设置子节点的属性，具体取决于你的实体类设计
                node.setChildren(childTree);
            }

            result.add(node);
        }

        return result;
    }

}
