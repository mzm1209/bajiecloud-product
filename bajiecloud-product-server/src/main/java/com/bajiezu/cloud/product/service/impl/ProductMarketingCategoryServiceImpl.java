package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.ProductMarketingCategoryVO;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import com.bajiezu.cloud.product.dal.mapper.ProductMarketingCategoryMapper;
import com.bajiezu.cloud.product.service.ProductMarketingCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.baijiazu.cloud.product.enums.ErrorCodeConstants.PRODUCT_MARKETING_CATEGORY_NOT_EXIST;

@Slf4j
@Service
public class ProductMarketingCategoryServiceImpl implements ProductMarketingCategoryService {

    @Autowired
    private ProductMarketingCategoryMapper productMarketingCategoryMapper;

    @Override
    public void add(PMCAddReqVO reqVO) {
        log.info("add dto: {}", reqVO);
        reqVO.validateParam();
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        ProductMarketingCategory category = buildCategory(reqVO, loginUser);
        productMarketingCategoryMapper.insert(category);
    }

    private ProductMarketingCategory buildCategory(PMCAddReqVO reqVO, LoginUser user) {
        ProductMarketingCategory category = new ProductMarketingCategory();
        category.setName(reqVO.getName());
        category.setParentId(reqVO.getParentId());
        category.setSort(reqVO.getSort());
        category.setLevel(reqVO.getLevel());
        category.setStatus(reqVO.getStatus());
        category.setRemark(reqVO.getRemark());
        category.setPartnerId(user.getPartnerId());
        category.setCreateBy(user.getId());
        category.setUpdateBy(user.getId());
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        category.setIsDeleted(0);
        return category;
    }

    @Override
    public void mod(PMCModReqVO reqVO) {
        log.info("mod dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductMarketingCategory category = productMarketingCategoryMapper.selectById(reqVO.getId());
        if (category == null) {
            throw exception(PRODUCT_MARKETING_CATEGORY_NOT_EXIST);
        }
        category.setName(reqVO.getName());
        category.setParentId(reqVO.getParentId());
        category.setSort(reqVO.getSort());
        category.setRemark(reqVO.getRemark());
        category.setUpdateBy(loginUser.getId());
        category.setUpdateTime(new Date());
        productMarketingCategoryMapper.updateById(category);
    }

    @Override
    public void del(PMCDelReqVO reqVO) {
        log.info("del dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductMarketingCategory category = productMarketingCategoryMapper.selectById(reqVO.getId());
        if (category == null) {
            throw exception(PRODUCT_MARKETING_CATEGORY_NOT_EXIST);
        }
        category.setUpdateBy(loginUser.getId());
        category.setUpdateTime(new Date());
        category.setIsDeleted(1);
        productMarketingCategoryMapper.updateById(category);
    }

    @Override
    public void statusChange(PMCStatusChangeVO reqVO) {
        log.info("statusChange dto: {}", reqVO);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        ProductMarketingCategory category = productMarketingCategoryMapper.selectById(reqVO.getId());
        if (category == null) {
            throw exception(PRODUCT_MARKETING_CATEGORY_NOT_EXIST);
        }
        category.setStatus(reqVO.getStatus());
        category.setUpdateBy(loginUser.getId());
        category.setUpdateTime(new Date());
        productMarketingCategoryMapper.updateById(category);
    }

    @Override
    public PageResult<ProductMarketingCategoryVO> list(ProductMarketingCategoryVO reqVO) {
        log.info("list dto: {}", reqVO);

        QueryWrapper<ProductMarketingCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        if (reqVO.getName() != null && !reqVO.getName().isEmpty()) {
            queryWrapper.like("name", reqVO.getName());
        }
        queryWrapper.orderByAsc("sort");

        List<ProductMarketingCategory> categories = productMarketingCategoryMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(categories)) {
            return new PageResult<>();
        }

        // 构建树形结构
        List<ProductMarketingCategoryVO> treeList = buildTree(categories, 0L);
        return new PageResult<>(treeList,(long)categories.size());
    }

    /**
     * 递归构建树形结构
     * @param categories 所有分类列表
     * @param parentId 父级ID
     * @return 树形结构列表
     */
    private List<ProductMarketingCategoryVO> buildTree(List<ProductMarketingCategory> categories, Long parentId) {
        return categories.stream()
                .filter(category ->
                        (parentId == 0 && (category.getParentId() == null || category.getParentId() == 0)) ||
                                (parentId != 0 && category.getParentId() != null && category.getParentId().equals(parentId))
                )
                .map(category -> {
                    ProductMarketingCategoryVO vo = new ProductMarketingCategoryVO();
                    BeanUtil.copyProperties(category, vo);
                    // 递归查找子节点
                    vo.setChildren(buildTree(categories, category.getId()));
                    return vo;
                })
                .sorted(Comparator.comparing(ProductMarketingCategoryVO::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }
}
