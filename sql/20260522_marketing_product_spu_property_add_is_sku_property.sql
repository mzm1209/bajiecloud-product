-- DDL
ALTER TABLE marketing_product_spu_property
    ADD COLUMN is_sku_property tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否SKU销售属性 0否1是' AFTER is_add_marketing_corner;

-- 历史数据默认值策略（兼容老数据）
UPDATE marketing_product_spu_property
SET is_sku_property = 0
WHERE is_sku_property IS NULL;

-- 回滚脚本
-- ALTER TABLE marketing_product_spu_property DROP COLUMN is_sku_property;
