-- DDL
ALTER TABLE marketing_product_spu_property
    ADD COLUMN is_add_property_pic tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否添加属性图 0否1是' AFTER sort,
    ADD COLUMN is_add_marketing_corner tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否添加营销角标 0否1是' AFTER is_add_property_pic;

ALTER TABLE marketing_product_spu_property_value
    ADD COLUMN marketing_corner_text varchar(64) NOT NULL DEFAULT '' COMMENT '营销角标文案' AFTER pic_url;

-- 历史数据默认值策略（兼容老数据）
UPDATE marketing_product_spu_property
SET is_add_property_pic = 0,
    is_add_marketing_corner = 0
WHERE is_add_property_pic IS NULL
   OR is_add_marketing_corner IS NULL;

UPDATE marketing_product_spu_property_value
SET marketing_corner_text = ''
WHERE marketing_corner_text IS NULL;

-- 回滚脚本
-- ALTER TABLE marketing_product_spu_property_value DROP COLUMN marketing_corner_text;
-- ALTER TABLE marketing_product_spu_property DROP COLUMN is_add_marketing_corner, DROP COLUMN is_add_property_pic;
