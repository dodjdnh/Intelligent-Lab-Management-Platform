SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `sys_inventory_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `consumable_id` bigint DEFAULT NULL COMMENT '耗材ID',
  `consumable_name` varchar(128) DEFAULT NULL COMMENT '耗材名称',
  `device_id` varchar(64) DEFAULT NULL COMMENT '关联设备ID',
  `change_type` varchar(64) DEFAULT NULL COMMENT '变更类型',
  `before_count` int DEFAULT NULL COMMENT '变更前库存',
  `after_count` int DEFAULT NULL COMMENT '变更后库存',
  `delta_count` int DEFAULT NULL COMMENT '库存变化量',
  `sensor_value` decimal(10,2) DEFAULT NULL COMMENT '传感器原始值',
  `conversion_mode` varchar(32) DEFAULT NULL COMMENT '换算模式',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作者',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_inventory_change_consumable_id` (`consumable_id`),
  KEY `idx_inventory_change_device_id` (`device_id`),
  KEY `idx_inventory_change_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变更日志表';

SET FOREIGN_KEY_CHECKS = 1;
