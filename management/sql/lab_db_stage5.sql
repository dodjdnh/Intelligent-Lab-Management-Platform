SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `sys_device_binding`
  ADD COLUMN IF NOT EXISTS `conversion_mode` varchar(32) DEFAULT 'DIRECT_VALUE' COMMENT '换算模式' AFTER `topic`,
  ADD COLUMN IF NOT EXISTS `unit_weight` decimal(10,2) DEFAULT NULL COMMENT '单件重量' AFTER `conversion_mode`,
  ADD COLUMN IF NOT EXISTS `tare_weight` decimal(10,2) DEFAULT NULL COMMENT '皮重' AFTER `unit_weight`;

SET FOREIGN_KEY_CHECKS = 1;
