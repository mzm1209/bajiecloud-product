-- value_added 增值服务金额赔付规则明细表
CREATE TABLE `value_added_compensation_amount_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `value_added_id` bigint(20) NOT NULL COMMENT '增值服务ID，关联 value_added.id',
  `compensation_amount` bigint(20) NOT NULL COMMENT '赔付金额上限，单位为毫',
  `compensation_amount_ratio` int(11) NOT NULL COMMENT '平台赔付比例，0~100',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序值，越小越靠前',
  `partner_id` bigint(20) NOT NULL COMMENT '合作商ID',
  `create_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人id',
  `update_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '更新人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  KEY `idx_value_added_id` (`value_added_id`),
  KEY `idx_value_added_amount` (`value_added_id`, `compensation_amount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='增值服务金额赔付规则表';

ALTER TABLE `value_added`
  MODIFY COLUMN `compensation_amount` bigint(20) DEFAULT NULL COMMENT '赔付金额，历史兼容字段；金额赔付规则改用 value_added_compensation_amount_rule',
  MODIFY COLUMN `compensation_amount_ratio` int(11) DEFAULT NULL COMMENT '金额赔付比例，历史兼容字段；金额赔付规则改用 value_added_compensation_amount_rule';
