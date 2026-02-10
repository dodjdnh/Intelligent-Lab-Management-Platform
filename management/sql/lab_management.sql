/*
 Navicat Premium Dump SQL

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : lab_db

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 03/02/2026 21:17:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_appointment
-- ----------------------------
DROP TABLE IF EXISTS `sys_appointment`;
CREATE TABLE `sys_appointment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lab_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实验室名称',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预约人姓名',
  `reserve_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预约日期(YYYY-MM-DD)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '审核中' COMMENT '状态(审核中/已通过)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户ID',
  `user_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号/工号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_appointment
-- ----------------------------
INSERT INTO `sys_appointment` VALUES (1, '人工智能实验室', 'Anno', '2026-02-04', '已通过', '2026-02-03 11:42:42', 1, 'ADMIN001');
INSERT INTO `sys_appointment` VALUES (2, '人工智能实验室', 'Tomori', '2026-02-04', '已通过', '2026-02-03 14:53:37', 2, '2023001');
INSERT INTO `sys_appointment` VALUES (3, '微电子实验室', 'Tomori', '2026-02-20', '已通过', '2026-02-03 15:53:53', 2, '2023001');

-- ----------------------------
-- Table structure for sys_consumable
-- ----------------------------
DROP TABLE IF EXISTS `sys_consumable`;
CREATE TABLE `sys_consumable`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '耗材名称',
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格型号',
  `count` int NULL DEFAULT 0 COMMENT '库存数量',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位(个/箱/支)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '耗材库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_consumable
-- ----------------------------
INSERT INTO `sys_consumable` VALUES (1, '500ml烧杯', 'GG-17', 60, '个', '2026-02-03 12:15:47');
INSERT INTO `sys_consumable` VALUES (2, '一次性手套', 'L号橡胶', 500, '双', '2026-02-03 12:15:47');
INSERT INTO `sys_consumable` VALUES (3, '精密pH试纸', '0.5-5.0', 20, '盒', '2026-02-03 12:15:47');

-- ----------------------------
-- Table structure for sys_consumable_apply
-- ----------------------------
DROP TABLE IF EXISTS `sys_consumable_apply`;
CREATE TABLE `sys_consumable_apply`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `consumable_id` bigint NOT NULL COMMENT '耗材ID',
  `consumable_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '耗材名称(冗余字段方便查)',
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请人姓名',
  `user_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请人学号',
  `num` int NOT NULL COMMENT '申请数量',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '审核中' COMMENT '状态: 审核中/已通过/已驳回',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '耗材申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_consumable_apply
-- ----------------------------
INSERT INTO `sys_consumable_apply` VALUES (1, 1, '500ml烧杯', 1, 'Anno', 'ADMIN001', 10, '已通过', '2026-02-03 14:47:49');
INSERT INTO `sys_consumable_apply` VALUES (2, 1, '500ml烧杯', 2, 'Tomori', '2023001', 50, '已通过', '2026-02-03 15:53:32');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色: admin 或 student',
  `user_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号/工号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'Anno', '20100908', 'admin', 'ADMIN001');
INSERT INTO `sys_user` VALUES (2, 'Tomori', '20101122', 'student', '2023001');
INSERT INTO `sys_user` VALUES (3, 'Rana', '20110222', 'student', '2023002');
INSERT INTO `sys_user` VALUES (4, 'Soyo', '20100527', 'student', '2023003');
INSERT INTO `sys_user` VALUES (5, 'Taki', '20100809', 'student', '2023004');
INSERT INTO `sys_user` VALUES (6, 'Mortis', '20100210', 'student', '2023005');

SET FOREIGN_KEY_CHECKS = 1;
