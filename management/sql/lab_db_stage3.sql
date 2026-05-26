SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `sys_alert_event` (
  `id` varchar(64) NOT NULL COMMENT '告警主键',
  `type` varchar(64) DEFAULT NULL COMMENT '告警类型',
  `level` varchar(32) DEFAULT NULL COMMENT '告警级别',
  `title` varchar(128) DEFAULT NULL COMMENT '告警标题',
  `message` text COMMENT '告警内容',
  `source_id` varchar(64) DEFAULT NULL COMMENT '来源设备或业务对象',
  `status` varchar(32) DEFAULT NULL COMMENT '状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `acknowledged_at` datetime DEFAULT NULL COMMENT '确认时间',
  PRIMARY KEY (`id`),
  KEY `idx_alert_created_at` (`created_at`),
  KEY `idx_alert_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统告警事件表';

CREATE TABLE IF NOT EXISTS `sys_device_registry` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_id` varchar(64) NOT NULL COMMENT '设备编号',
  `area` varchar(64) DEFAULT NULL COMMENT '区域',
  `transport` varchar(32) DEFAULT NULL COMMENT '通信方式',
  `status` varchar(32) DEFAULT NULL COMMENT '在线状态',
  `last_value` decimal(10,2) DEFAULT NULL COMMENT '最近数值',
  `last_seen` datetime DEFAULT NULL COMMENT '最近上报时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_id` (`device_id`),
  KEY `idx_device_last_seen` (`last_seen`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备注册与状态表';

SET FOREIGN_KEY_CHECKS = 1;
