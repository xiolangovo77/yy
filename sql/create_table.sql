-- =============================================
-- 医生评估平台 数据库建表语句
-- 引擎：InnoDB  字符集：utf8mb4
-- =============================================

-- 1. 医生用户表
DROP TABLE IF EXISTS `sys_doctor`;
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
DROP TABLE IF EXISTS `child_info`;
CREATE TABLE `child_info` (
                              `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
                              UNIQUE KEY `uk_child_no` (`child_no`) COMMENT '儿童编号唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='儿童基本信息表';

-- 3. 视频资源表
DROP TABLE IF EXISTS `video_resource`;
CREATE TABLE `video_resource` (
                                  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `child_id` bigint unsigned NOT NULL COMMENT '关联儿童ID',
                                  `video_url` varchar(512) NOT NULL COMMENT '视频存储地址',
                                  `video_name` varchar(128) DEFAULT NULL COMMENT '视频名称',
                                  `upload_doctor_id` bigint unsigned DEFAULT NULL COMMENT '上传医生ID',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_child_id` (`child_id`) COMMENT '儿童ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频资源表';

-- 4. 评估任务表（儿童列表核心表）
DROP TABLE IF EXISTS `assessment_task`;
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
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_task_no` (`task_no`) COMMENT '任务编号唯一索引',
                                   KEY `idx_doctor_status` (`doctor_id`, `task_status`) COMMENT '医生+状态索引，用于列表查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估任务表';

-- 5. 第一阶段评估表单表
DROP TABLE IF EXISTS `assessment_form1`;
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
DROP TABLE IF EXISTS `child_report`;
CREATE TABLE `child_report` (
                                `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `child_id` bigint unsigned NOT NULL COMMENT '关联儿童ID',
                                `report_no` varchar(32) NOT NULL COMMENT '报告编号（唯一）',
                                `report_content` longtext NOT NULL COMMENT '报告完整内容（Markdown/HTML）',
                                `generate_doctor_id` bigint unsigned DEFAULT NULL COMMENT '生成报告医生ID',
                                `report_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '报告状态 0-草稿 1-已生成',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_report_no` (`report_no`) COMMENT '报告编号唯一索引',
                                KEY `idx_child_id` (`child_id`) COMMENT '儿童ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='儿童筛查报告表';

-- 7. 第二阶段评估表单表
DROP TABLE IF EXISTS `assessment_form2`;
CREATE TABLE `assessment_form2` (
                                    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `task_id` bigint unsigned NOT NULL COMMENT '关联评估任务ID',
                                    `child_id` bigint unsigned NOT NULL COMMENT '关联儿童ID（冗余，优化查询）',
                                    `report_id` bigint unsigned NOT NULL COMMENT '关联报告ID',
                                    `disease_type` varchar(32) NOT NULL COMMENT '最终疾病判定 ASD/TD/DD',
                                    `confidence` tinyint unsigned DEFAULT NULL COMMENT '最终诊断信心 0-100',
                                    `diagnostic_basis` text COMMENT '最终诊断依据',
                                    `final_conclusion` text COMMENT '最终诊断结论',
                                    `intervention_suggest` text COMMENT '干预建议',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_task_id` (`task_id`) COMMENT '任务ID唯一，一个任务对应一份二阶段表单',
                                    KEY `idx_child_id` (`child_id`) COMMENT '儿童ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第二阶段评估表单表';