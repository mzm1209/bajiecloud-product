-- 营销商品SKU租赁方式租期属性表
-- 用于维护 SKU + 租赁方式 + 租期 维度下的总租金、月租金、日租金、买断金、溢价金、库存
-- 金额字段统一按 元*10000 存储，例如 1.11 元存储为 11100

CREATE TABLE `marketing_product_sku_rental_method_property` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `marketing_spu_id` bigint(20) NOT NULL COMMENT '营销商品SPU ID',
  `marketing_sku_id` bigint(20) NOT NULL COMMENT '营销商品SKU ID',
  `rental_method` tinyint(4) NOT NULL COMMENT '租赁方式：1-租完归还，2-灵活租',
  `rental_period_month` int(11) NOT NULL COMMENT '租期，单位月：如3、6、12',

  `total_rent` bigint(20) NOT NULL DEFAULT '0' COMMENT '总租金，金额按元*10000存储，如1.11元存储11100',
  `monthly_rent` bigint(20) NOT NULL DEFAULT '0' COMMENT '月租金，金额按元*10000存储，如1.11元存储11100',
  `daily_rent` bigint(20) NOT NULL DEFAULT '0' COMMENT '日租金，金额按元*10000存储，如1.11元存储11100',
  `buyout_amount` bigint(20) NOT NULL DEFAULT '0' COMMENT '到期购买金/买断金，金额按元*10000存储，如1.11元存储11100',
  `premium` bigint(20) NOT NULL DEFAULT '0' COMMENT '溢价金，金额按元*10000存储，如1.11元存储11100',
  `stock` int(11) DEFAULT NULL COMMENT '库存',

  `partner_id` bigint(20) DEFAULT NULL COMMENT '合作方ID',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` bigint(20) NOT NULL DEFAULT '0' COMMENT '删除标记，0-未删除，非0-已删除；逻辑删除时写入主键ID避免唯一索引冲突',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_method_period_not_deleted`
    (`marketing_sku_id`, `rental_method`, `rental_period_month`, `is_deleted`),
  KEY `idx_marketing_spu_id` (`marketing_spu_id`),
  KEY `idx_spu_sku` (`marketing_spu_id`, `marketing_sku_id`),
  KEY `idx_spu_method_period` (`marketing_spu_id`, `rental_method`, `rental_period_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='营销商品SKU租赁方式租期属性表';
