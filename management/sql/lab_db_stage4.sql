SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `sys_device_binding` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_id` varchar(64) NOT NULL COMMENT '设备编号',
  `consumable_id` bigint DEFAULT NULL COMMENT '绑定耗材ID',
  `consumable_name` varchar(128) DEFAULT NULL COMMENT '绑定耗材名称',
  `area` varchar(64) DEFAULT NULL COMMENT '绑定区域',
  `topic` varchar(255) DEFAULT NULL COMMENT 'MQTT Topic',
  `enabled_flag` tinyint DEFAULT 1 COMMENT '启用标记',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_binding_device_id` (`device_id`),
  KEY `idx_binding_consumable_id` (`consumable_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备绑定表';

SET FOREIGN_KEY_CHECKS = 1;
