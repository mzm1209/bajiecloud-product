package com.bajiezu.cloud.product.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bajiezu.cloud.common.constants.CommonStatusEnum;
import com.bajiezu.cloud.common.web.cloud.utils.object.BeanUtils;
import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.framework.security.po.LoginUser;
import com.bajiezu.cloud.framework.security.util.SecurityFrameworkUtils;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.ProductMcRespVO;
import com.bajiezu.cloud.product.dal.dto.IdAndCountDTO;
import com.bajiezu.cloud.product.dal.entity.ProductMarketingCategory;
import com.bajiezu.cloud.product.dal.mapper.MarketingProductSkuMapper;
import com.bajiezu.cloud.product.dal.mapper.ProductMarketingCategoryMapper;
import com.bajiezu.cloud.product.dto.McSimpleInfoRespVO;
import com.bajiezu.cloud.product.enums.ShelvesStatusEnum;
import com.bajiezu.cloud.product.service.ProductMarketingCategoryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.jsonwebtoken.lang.Collections;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.bajiezu.cloud.common.web.exception.util.ServiceExceptionUtil.exception;
import static com.bajiezu.cloud.product.enums.ErrorCodeConstants.*;

@Slf4j
@Service
public class ProductMarketingCategoryServiceImpl implements ProductMarketingCategoryService {

    @Resource
    private ProductMarketingCategoryMapper productMarketingCategoryMapper;
    @Resource
    private MarketingProductSkuMapper skuMapper;

    @Override
    public void add(PMCAddReqVO reqVO) {
        log.info("add dto: {}", reqVO);
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();

        // 上级的path
        String parentPath = "";
        if (Objects.isNull(reqVO.getParentId())) {
            reqVO.setParentId(NumberUtils.LONG_ZERO);
        } else {
            parentPath = validateAndGetParentPath(reqVO.getParentId());
        }

        // 判断分组名称是否已存在
        List<ProductMarketingCategory> marketingCategories = productMarketingCategoryMapper.queryByParentIdAndName(
                reqVO.getParentId(), reqVO.getName());
        if (CollectionUtil.isNotEmpty(marketingCategories)) {
            throw exception(PRODUCT_MARKETING_CATEGORY_NAME_EXIST);
        }

        ProductMarketingCategory category = buildCategory(reqVO, loginUser);
        productMarketingCategoryMapper.insert(category);

        // 更新类目的path
        String path;
        if (StringUtils.isNotBlank(parentPath)) {
            path = parentPath + "," + category.getId();
        } else {
            path = String.valueOf(category.getId());
        }
        productMarketingCategoryMapper.updatePathById(category.getId(), path);
    }

    @Override
    public void mod(PMCModReqVO reqVO) {
        log.info("mod dto: {}", reqVO);
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();

        // 判断父分组是否存在
        String parentPath = validateAndGetParentPath(reqVO.getParentId());

        // 判断编辑的分组是否存在
        ProductMarketingCategory category = productMarketingCategoryMapper.selectById(reqVO.getId());
        if (category == null) {
            throw exception(PRODUCT_MARKETING_CATEGORY_NOT_EXIST);
        }

        // 判断编辑之后的分组名称是否已存在
        List<ProductMarketingCategory> marketingCategories = productMarketingCategoryMapper.queryByParentIdAndName(
                reqVO.getParentId(), reqVO.getName());
        if (CollectionUtil.isNotEmpty(marketingCategories)) {
            if (marketingCategories.size() >= 2 || marketingCategories.get(0).getId().longValue() != reqVO.getId()) {
                throw exception(PRODUCT_MARKETING_CATEGORY_NAME_EXIST);
            }
        }
        category.setName(reqVO.getName());
        category.setParentId(reqVO.getParentId());
        category.setSort(reqVO.getSort());
        category.setRemark(reqVO.getRemark());
        category.setUpdateBy(loginUser.getId());
        category.setUpdateTime(new Date());
        productMarketingCategoryMapper.updateById(category);

        String path;
        if (StringUtils.isNotBlank(parentPath)) {
            path = parentPath + "," + category.getId();
        } else {
            path = String.valueOf(category.getId());
        }
        productMarketingCategoryMapper.updatePathById(category.getId(), path);
    }

    @Override
    public void del(PMCDelReqVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("del dto: {},operatorId:{}", reqVO, loginUser.getId());

        ProductMarketingCategory productMarketingCategory = productMarketingCategoryMapper.selectById(reqVO.getId());
        if (productMarketingCategory == null) {
            throw exception(PRODUCT_MARKETING_CATEGORY_NOT_EXIST);
        }

        Date date = new Date();
        // 删除指定的营销类目
        productMarketingCategoryMapper.logicDelById(reqVO.getId(), loginUser.getId(), date);

        // 逻辑删除子类目
        String path = productMarketingCategory.getPath();
        if (StringUtils.isEmpty(path)) {
            path = String.valueOf(reqVO.getId());
        } else {
            path = productMarketingCategory.getPath() + "," + reqVO.getId();
        }
        productMarketingCategoryMapper.logicDelByPathLike(path, loginUser.getId(), date);
    }

    @Override
    public void changeStatus(PMCStatusChangeVO reqVO) {
        LoginUser<?> loginUser = SecurityFrameworkUtils.getLoginUser();
        log.info("statusChange reqVO: {},operatorId:{}", reqVO, loginUser.getId());

        // 校验指定的营销类目存不存在
        ProductMarketingCategory productMarketingCategory = productMarketingCategoryMapper.selectById(reqVO.getId());
        if (productMarketingCategory == null) {
            throw exception(PRODUCT_MARKETING_CATEGORY_NOT_EXIST);
        }

        Date now = new Date();
        // 更新指定的营销类目的状态
        productMarketingCategoryMapper.updateStatusById(reqVO.getId(), reqVO.getStatus(), loginUser.getId(), now);

        // 更新指定类目下的所有子类目状态
        String path = productMarketingCategory.getPath();
        if (StringUtils.isEmpty(path)) {
            path = String.valueOf(reqVO.getId());
        } else {
            path = productMarketingCategory.getPath() + "," + reqVO.getId();
        }
        productMarketingCategoryMapper.updateStatusByPathLike(path, reqVO.getStatus(), loginUser.getId(), now);
    }

    @Override
    public PageResult<ProductMcRespVO> page(ProductMCListReq reqVO) {
        log.info("list dto: {}", reqVO);

        // 根据营销类目名称查询营销类目
        Set<Long> firstLevelCategoryIds = Sets.newHashSet();
        if (StringUtils.isNotEmpty(reqVO.getName())) {
            List<ProductMarketingCategory> marketingCategories = productMarketingCategoryMapper.queryByName(reqVO.getName());
            if (CollectionUtil.isEmpty(marketingCategories)) {
                return PageResult.empty();
            }
            for (ProductMarketingCategory marketingCategory : marketingCategories) {
                firstLevelCategoryIds.add(Long.parseLong(marketingCategory.getPath().split(",")[0]));
            }
        }

        // 查询一级类目
        int offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        int level = org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
        List<ProductMarketingCategory> levelCategories = productMarketingCategoryMapper.selectByLevel(
                firstLevelCategoryIds, level, offset, reqVO.getPageSize());
        if (CollectionUtil.isEmpty(levelCategories)) {
            return PageResult.empty();
        }
        List<ProductMcRespVO> firstLevelCategoryRespVOList = levelCategories.stream().map(category -> {
            ProductMcRespVO respVO = new ProductMcRespVO();
            BeanUtils.copyProperties(category, respVO);
            return respVO;
        }).toList();
        long count = productMarketingCategoryMapper.selectCountByLevel(firstLevelCategoryIds, level);

        // 获取一级类目的id
        List<Long> categoryIds = levelCategories.stream().map(ProductMarketingCategory::getId).toList();
        // 获取所有的类目id
        List<Long> allCategoryIds = Lists.newArrayList(categoryIds);
        // 批量查询所有子孙类目 （使用路径前缀匹配）
        List<ProductMarketingCategory> childrenCategories = productMarketingCategoryMapper.batchSelectByPathPrefix(categoryIds);
        List<ProductMcRespVO> childrenCategoryRespVOList = childrenCategories.stream().map(category -> {
            ProductMcRespVO respVO = new ProductMcRespVO();
            BeanUtils.copyProperties(category, respVO);

            allCategoryIds.add(category.getId());
            return respVO;
        }).toList();

        // 获取类目id下的已上架的sku数量
        List<IdAndCountDTO> categoryId2SkuCounts = skuMapper.selectCategoryId2SkuCount(allCategoryIds, ShelvesStatusEnum.ON_SHELVES.getValue());
        Map<Long, Long> categoryId2SkuCountsMap = categoryId2SkuCounts.stream().collect(Collectors.toMap(IdAndCountDTO::getId, IdAndCountDTO::getCount));

        // 根据一级类目构造成树形结构
        List<ProductMcRespVO> treeList = buildTree(firstLevelCategoryRespVOList, childrenCategoryRespVOList, categoryId2SkuCountsMap);

        return new PageResult<>(treeList, count);
    }

    @Override
    public List<ProductMcRespVO> tree() {
        List<ProductMarketingCategory> allCategories = productMarketingCategoryMapper.queryAll();
        if (CollectionUtil.isEmpty(allCategories)) {
            return java.util.Collections.emptyList();
        }

        List<ProductMcRespVO> firstLevelCategoryRespVOList = Lists.newArrayList();
        List<ProductMcRespVO> childrenCategoryRespVOList = Lists.newArrayList();
        for (ProductMarketingCategory category : allCategories) {
            ProductMcRespVO respVO = new ProductMcRespVO();
            BeanUtils.copyProperties(category, respVO);
            if (NumberUtils.INTEGER_ONE.equals(category.getLevel())) {
                firstLevelCategoryRespVOList.add(respVO);
            } else {
                childrenCategoryRespVOList.add(respVO);
            }
        }

        return buildTree(firstLevelCategoryRespVOList, childrenCategoryRespVOList, Maps.newHashMap());
    }


    @Override
    public List<McSimpleInfoRespVO> getByIds(List<Long> ids) {
        log.info("mc getByIds ids: {}", ids);
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ProductMarketingCategory> marketingCategories = productMarketingCategoryMapper.selectByIds(ids);
        return buildSimpleList(marketingCategories);
    }

    private List<McSimpleInfoRespVO> buildSimpleList(List<ProductMarketingCategory> marketingCategories) {
        if (CollectionUtils.isEmpty(marketingCategories)) {
            return Collections.emptyList();
        }
        List<McSimpleInfoRespVO> simpleList = Lists.newArrayList();
        for (ProductMarketingCategory marketingCategory : marketingCategories) {
            McSimpleInfoRespVO vo = new McSimpleInfoRespVO();
            simpleList.add(vo);

            vo.setId(marketingCategory.getId());
            vo.setName(marketingCategory.getName());
            vo.setLevel(marketingCategory.getLevel());
            vo.setParentId(marketingCategory.getParentId());
        }

        return simpleList;
    }

    private String validateAndGetParentPath(Long parentId) {
        if (parentId == null || parentId == 0) {
            return "";
        }
        // 判断父级分组是否存在
        ProductMarketingCategory parentCategory = productMarketingCategoryMapper.selectById(parentId);
        if (Objects.isNull(parentCategory)) {
            throw exception(PRODUCT_MARKETING_CATEGORY_NOT_EXIST);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(parentCategory.getStatus())) {
            throw exception(PRODUCT_MARKETING_CATEGORY_DISABLED);
        }
        return parentCategory.getPath();
    }

    private ProductMarketingCategory buildCategory(PMCAddReqVO reqVO, LoginUser<?> user) {
        ProductMarketingCategory category = new ProductMarketingCategory();
        category.setName(reqVO.getName());
        category.setParentId(reqVO.getParentId());
        category.setSort(reqVO.getSort());
        category.setLevel(reqVO.getLevel());
        category.setStatus(CommonStatusEnum.ENABLE.getStatus());
        category.setPath(reqVO.getPath());
        category.setRemark(reqVO.getRemark());
        category.setPartnerId(user.getPartnerId());
        category.setCreateBy(user.getId());
        category.setUpdateBy(user.getId());
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        category.setIsDeleted(0);
        return category;
    }

    private List<ProductMcRespVO> buildTree(List<ProductMcRespVO> rootCategories, List<ProductMcRespVO> childrenCategories,
                                            Map<Long, Long> categoryId2SkuCountsMap) {
        // 创建一个Map，以父ID为key，子节点列表为value
        Map<Long, List<ProductMcRespVO>> childrenMap = Maps.newHashMap();

        for (ProductMcRespVO category : rootCategories) {
            if (categoryId2SkuCountsMap != null && categoryId2SkuCountsMap.containsKey(category.getId())) {
                category.setSkuCount(categoryId2SkuCountsMap.get(category.getId()));
            } else {
                category.setSkuCount(0L);
            }
        }

        // 遍历所有分类，构建父ID到子节点列表的映射
        for (ProductMcRespVO category : childrenCategories) {
            if (categoryId2SkuCountsMap != null && categoryId2SkuCountsMap.containsKey(category.getId())) {
                category.setSkuCount(categoryId2SkuCountsMap.get(category.getId()));
            } else {
                category.setSkuCount(0L);
            }
            Long parentId = category.getParentId();
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
        }

        // 递归构建树形结构
        List<ProductMcRespVO> result =  buildTreeRecursive(rootCategories, childrenMap);

        // 计算每个节点的总SKU数量（包含子节点的SKU数量）
        calculateTotalSkuCount(result);

        return result;
    }

    /**
     * 递归构建树形结构
     *
     * @param categories 当前层级的节点
     * @param childrenMap 子节点映射表
     * @return 构建后的树形结构
     */
    private List<ProductMcRespVO> buildTreeRecursive(List<ProductMcRespVO> categories,
                                                     Map<Long, List<ProductMcRespVO>> childrenMap) {
        List<ProductMcRespVO> result = new ArrayList<>();
        for (ProductMcRespVO category : categories) {
            // 复制当前节点
            ProductMcRespVO node = new ProductMcRespVO();
            BeanUtils.copyProperties(category, node);

            // 获取当前节点的子节点并按sort字段排序
            List<ProductMcRespVO> children = childrenMap.get(category.getId());
            if (CollectionUtil.isNotEmpty(children)) {
                // 按sort字段排序
                children.sort(Comparator.comparing(ProductMcRespVO::getSort));
                // 递归构建子节点的树形结构
                List<ProductMcRespVO> childTree = buildTreeRecursive(children, childrenMap);
                node.setChildren(childTree);
            }
            result.add(node);
        }
        // 对当前层级的节点也按sort字段排序
        result.sort(Comparator.comparing(ProductMcRespVO::getSort));
        return result;
    }

    /**
     * 递归计算每个节点的总SKU数量（包含子节点的SKU数量）
     *
     * @param nodes 当前节点列表
     * @return 该层节点的总SKU数量
     */
    private Long calculateTotalSkuCount(List<ProductMcRespVO> nodes) {
        if (CollectionUtil.isEmpty(nodes)) {
            return 0L;
        }

        long totalSkuCount = 0L;

        for (ProductMcRespVO node : nodes) {
            long childSkuCount = 0L;

            // 如果有子节点，先计算子节点的总SKU数量
            if (CollectionUtil.isNotEmpty(node.getChildren())) {
                childSkuCount = calculateTotalSkuCount(node.getChildren());
            }

            // 当前节点的总SKU数量 = 本身的SKU数量 + 所有子节点的SKU数量
            long currentTotalSkuCount = node.getSkuCount() + childSkuCount;
            node.setSkuCount(currentTotalSkuCount);

            totalSkuCount += currentTotalSkuCount;
        }

        return totalSkuCount;
    }
}
