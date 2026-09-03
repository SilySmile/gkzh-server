/*
 Navicat Premium Data Transfer

 Source Server         : 测试服务器
 Source Server Type    : MySQL
 Source Server Version : 50743 (5.7.43)
 Source Host           : 39.99.149.236:8306
 Source Schema         : gkzh

 Target Server Type    : MySQL
 Target Server Version : 50743 (5.7.43)
 File Encoding         : 65001

 Date: 09/11/2025 11:38:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cyzs_answer_detail
-- ----------------------------
DROP TABLE IF EXISTS `cyzs_answer_detail`;
CREATE TABLE `cyzs_answer_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `round_id` bigint(20) NOT NULL COMMENT '关卡记录ID，外键关联 cyzs_game_round.id',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID，外键关联 cyzs_question.id',
  `user_answer` char(1) NOT NULL COMMENT '用户答案 (A, B, C)',
  `is_correct` tinyint(1) NOT NULL COMMENT '是否答对 (0: 错， 1: 对)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13633 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='答题详情表';

-- ----------------------------
-- Table structure for cyzs_game_round
-- ----------------------------
DROP TABLE IF EXISTS `cyzs_game_round`;
CREATE TABLE `cyzs_game_round` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `user_id` bigint(20) NOT NULL COMMENT '参与者ID',
  `is_success` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否通关 (0: 失败， 1: 成功)',
  `start_time` datetime NOT NULL COMMENT '开始答题时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束答题时间',
  `create_time` datetime NOT NULL COMMENT '记录创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4545 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='答题关卡记录表';

-- ----------------------------
-- Table structure for cyzs_question
-- ----------------------------
DROP TABLE IF EXISTS `cyzs_question`;
CREATE TABLE `cyzs_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `question_text` varchar(500) NOT NULL COMMENT '题目内容',
  `type` char(1) NOT NULL COMMENT '题目类型，来源于字典',
  `option_a` varchar(200) NOT NULL COMMENT '选项A内容',
  `option_b` varchar(200) NOT NULL COMMENT '选项B内容',
  `option_c` varchar(200) NOT NULL COMMENT '选项C内容',
  `correct_option_key` varchar(10) NOT NULL COMMENT '正确选项的键，多选时为多个选项的键，用英文逗号分隔',
  `option_d` varchar(200) NOT NULL COMMENT '选项D内容',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='题目表';

SET FOREIGN_KEY_CHECKS = 1;
