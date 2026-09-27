-- =============================================
-- 医生评估平台：清空并重新初始化数据库
-- 自动创建并切换到 yy 数据库，无需额外建库或迁移脚本。
-- 执行后会删除并重建以下 7 张业务表，清空全部现有业务数据。
-- 初始管理员：账号 admin，密码 123456，角色 1，状态 1。
-- 已合并：儿童绑定医生、每个儿童仅一个未删除任务及一个 main 主视频（删除后可重建）。
-- 引擎：InnoDB  字符集：utf8mb4
-- =============================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `yy`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE `yy`;

-- 按依赖顺序先删除表单和业务记录，最后删除儿童、医生。
DROP TABLE IF EXISTS `assessment_form2`;
DROP TABLE IF EXISTS `assessment_form1`;
DROP TABLE IF EXISTS `assessment_task`;
DROP TABLE IF EXISTS `child_report`;
DROP TABLE IF EXISTS `video_resource`;
DROP TABLE IF EXISTS `child_info`;
DROP TABLE IF EXISTS `sys_doctor`;

-- 1. 医生用户表
CREATE TABLE `sys_doctor` (
                              `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `doctor_name` varchar(64) NOT NULL COMMENT '医生姓名',
                              `title` varchar(32) DEFAULT NULL COMMENT '职称（主任医师/主治医师等）',
                              `hospital` varchar(128) NOT NULL COMMENT '所在医院',
                              `account` varchar(64) NOT NULL COMMENT '登录账号（手机号/邮箱）',
                              `password` varchar(128) NOT NULL COMMENT '加密密码',
                              `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                              `role` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '角色类型 0-普通医生 1-管理员',
                              `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '账号状态 0-禁用 1-正常',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_account` (`account`) COMMENT '账号唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生用户表';

-- 2. 儿童基本信息表
CREATE TABLE `child_info` (
                              `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `doctor_id` bigint unsigned NOT NULL COMMENT '负责医生ID',
                              `child_no` varchar(32) NOT NULL COMMENT '儿童编号（唯一）',
                              `child_name` varchar(64) DEFAULT NULL COMMENT '儿童姓名',
                              `age` int unsigned DEFAULT NULL COMMENT '年龄（岁）',
                              `gender` varchar(8) DEFAULT NULL COMMENT '性别（男/女/未提供）',
                              `assess_type` varchar(64) DEFAULT NULL COMMENT '评估方式',
                              `remark` varchar(255) DEFAULT NULL COMMENT '补充备注',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                              PRIMARY KEY (`id`),
                              KEY `idx_child_doctor_id` (`doctor_id`),
                              UNIQUE KEY `uk_child_no` (`child_no`) COMMENT '儿童编号唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='儿童基本信息表';

-- 3. 视频资源表
CREATE TABLE `video_resource` (
                                  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `child_id` bigint unsigned NOT NULL COMMENT '关联儿童ID',
                                  `video_url` varchar(512) NOT NULL COMMENT '视频存储地址',
                                  `video_name` varchar(128) NOT NULL DEFAULT 'main.mp4' COMMENT '视频名称，以main开头为主视频',
                                  `upload_doctor_id` bigint unsigned DEFAULT NULL COMMENT '上传医生ID',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                                  `main_child_id` bigint unsigned GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 AND LEFT(LOWER(TRIM(`video_name`)), 4) COLLATE utf8mb4_bin = 'main' THEN `child_id` ELSE NULL END) STORED,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_child_main_video` (`main_child_id`) COMMENT '每个儿童只能有一个未删除的main主视频',
                                  KEY `idx_child_id` (`child_id`) COMMENT '儿童ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频资源表';

-- 4. 评估任务表（儿童列表核心表）
CREATE TABLE `assessment_task` (
                                   `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `doctor_id` bigint unsigned NOT NULL COMMENT '负责医生ID',
                                   `child_id` bigint unsigned NOT NULL COMMENT '关联儿童ID',
                                   `task_no` varchar(32) NOT NULL COMMENT '评估任务编号（唯一）',
                                   `task_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '完成状态 0-未开始 1-进行中 2-已完成',
                                   `current_step` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '当前步骤 1-第一阶段表单 2-第二阶段表单',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                                   `active_child_id` bigint unsigned GENERATED ALWAYS AS (CASE WHEN `is_deleted` = 0 THEN `child_id` ELSE NULL END) STORED,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_task_no` (`task_no`) COMMENT '任务编号唯一索引',
                                   UNIQUE KEY `uk_assessment_child_id` (`active_child_id`) COMMENT '每个儿童只能有一个未删除的评估任务',
                                   KEY `idx_doctor_status` (`doctor_id`, `task_status`) COMMENT '医生+状态索引，用于列表查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估任务表';

-- 5. 第一阶段评估表单表
CREATE TABLE `assessment_form1` (
                                    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `task_id` bigint unsigned NOT NULL COMMENT '关联评估任务ID',
                                    `child_id` bigint unsigned NOT NULL COMMENT '关联儿童ID（冗余，优化查询）',
                                    `disease_type` varchar(32) NOT NULL COMMENT '疾病判定 ASD/TD/DD',
                                    `confidence` tinyint unsigned DEFAULT NULL COMMENT '诊断信心 0-100',
                                    `diagnostic_basis` text COMMENT '诊断依据',
                                    `doctor_remark` varchar(512) DEFAULT NULL COMMENT '医生补充备注',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_task_id` (`task_id`) COMMENT '任务ID唯一，一个任务对应一份一阶段表单',
                                    KEY `idx_child_id` (`child_id`) COMMENT '儿童ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第一阶段评估表单表';

-- 6. 儿童筛查报告表
CREATE TABLE `child_report` (
                                `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `child_id` bigint unsigned NOT NULL COMMENT '关联儿童ID',
                                `report_no` varchar(32) NOT NULL COMMENT '报告编号（唯一）',
                                `report_url` varchar(512)  NOT NULL COMMENT '报告url',
                                `upload_doctor_id` bigint unsigned DEFAULT NULL COMMENT '上传报告医生ID',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_report_no` (`report_no`) COMMENT '报告编号唯一索引',
                                KEY `idx_child_id` (`child_id`) COMMENT '儿童ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='儿童筛查报告表';

-- 7. 第二阶段评估表单表
CREATE TABLE `assessment_form2` (
                                    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `task_id` bigint unsigned NOT NULL COMMENT '关联评估任务ID',
                                    `child_id` bigint unsigned NOT NULL COMMENT '关联儿童ID（冗余，优化查询）',
                                    `disease_type` varchar(32) NOT NULL COMMENT '最终疾病判定 ASD/TD/DD',
                                    `confidence` tinyint unsigned DEFAULT NULL COMMENT '最终诊断信心 0-100',
                                    `diagnostic_basis` text COMMENT '最终诊断依据',
                                    `final_conclusion` text COMMENT '最终诊断结论',
                                    `doctor_remark` varchar(512) DEFAULT NULL COMMENT '医生补充备注',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_task_id` (`task_id`) COMMENT '任务ID唯一，一个任务对应一份二阶段表单',
                                    KEY `idx_child_id` (`child_id`) COMMENT '儿童ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第二阶段评估表单表';


-- 8. 初始化管理员（与后端 DefaultValue.SALT + 密码的 MD5 规则保持一致）
-- 只创建管理员，不插入儿童、普通医生、任务或评估示例数据。
INSERT INTO `sys_doctor`
    (`id`, `doctor_name`, `hospital`, `account`, `password`, `role`, `status`, `is_deleted`)
VALUES
    (1, '系统管理员', '系统管理', 'admin', MD5(CONCAT('lxh', '123456')), 1, 1, 0);
