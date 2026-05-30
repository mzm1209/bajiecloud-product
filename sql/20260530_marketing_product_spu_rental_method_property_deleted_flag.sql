-- 营销商品SPU租赁方式属性表：软删除标记改为删除版本，避免历史删除记录与再次逻辑删除时唯一索引冲突
ALTER TABLE `marketing_product_spu_rental_method_property`
  MODIFY COLUMN `is_deleted` bigint(20) NOT NULL DEFAULT '0' COMMENT '删除标记，0-未删除，非0-已删除；逻辑删除时写入主键ID避免唯一索引冲突';

UPDATE `marketing_product_spu_rental_method_property`
SET `is_deleted` = `id`
WHERE `is_deleted` = 1;
