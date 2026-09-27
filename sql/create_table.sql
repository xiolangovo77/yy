-- =============================================
-- 医生评估平台：清空并重新初始化数据库
-- 自动创建并切换到 yy 数据库，无需额外建库或迁移脚本。
-- 执行后会删除并重建以下 9 张表，清空全部现有业务数据及项目介绍配置。
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
DROP TABLE IF EXISTS `project_introduction`;
DROP TABLE IF EXISTS `assessment_form2`;
DROP TABLE IF EXISTS `assessment_feedback`;
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
                                    `social_response` tinyint unsigned DEFAULT NULL COMMENT '社交回应，1-5分，严重程度递增',
                                    `eye_contact` tinyint unsigned DEFAULT NULL COMMENT '眼神交流，1-5分，严重程度递增',
                                    `social_reciprocity` tinyint unsigned DEFAULT NULL COMMENT '社会互惠性，1-5分，严重程度递增',
                                    `social_emotional_expression` tinyint unsigned DEFAULT NULL COMMENT '社会情绪表达，1-5分，严重程度递增',
                                    `repetitive_behavior` text COMMENT '重复刻板或感觉异常行为',
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
                                    `report_usefulness` tinyint unsigned DEFAULT NULL COMMENT '报告有用程度，1-5分，程度递增',
                                    `evidence_validity` tinyint unsigned DEFAULT NULL COMMENT '报告证据有效性，1-5分，程度递增',
                                    `fact_accuracy` tinyint unsigned DEFAULT NULL COMMENT '事实是否准确，1-5分，程度递增',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '软删除标记 0-未删除 1-已删除',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_task_id` (`task_id`) COMMENT '任务ID唯一，一个任务对应一份二阶段表单',
                                    KEY `idx_child_id` (`child_id`) COMMENT '儿童ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第二阶段评估表单表';


-- 8. 医生报告评价表（完成两个阶段后提交，每个任务一份）
CREATE TABLE `assessment_feedback` (
    `id` bigint unsigned NOT NULL COMMENT '主键ID',
    `task_id` bigint unsigned NOT NULL COMMENT '关联评估任务ID',
    `child_id` bigint unsigned NOT NULL COMMENT '关联儿童ID',
    `doctor_id` bigint unsigned NOT NULL COMMENT '评价医生ID',
    `core_problem_clarity` varchar(16) NOT NULL COMMENT '快速找到核心问题：符合/一般/不符合',
    `judgment_improvement` varchar(16) NOT NULL COMMENT '提高判断质量或效率：符合/一般/不符合',
    `observation_accuracy` varchar(16) NOT NULL COMMENT '观察事实准确：符合/一般/不符合',
    `suggestions` varchar(2000) DEFAULT NULL COMMENT '报告改进建议',
    `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '软删除标记',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_feedback_task_id` (`task_id`),
    KEY `idx_feedback_doctor_id` (`doctor_id`),
    KEY `idx_feedback_child_id` (`child_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生报告评价表';

-- 9. 项目介绍（面向评估医生，由管理员编辑，固定使用 id=1 的记录）
CREATE TABLE `project_introduction` (
    `id` tinyint unsigned NOT NULL COMMENT '固定为1的项目介绍',
    `title` varchar(120) NOT NULL COMMENT '说明标题',
    `content` mediumtext NOT NULL COMMENT '纯文本正文，保留换行',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生端项目介绍';

INSERT INTO `project_introduction` (`id`, `title`, `content`)
VALUES (1, '项目介绍与医生评估说明', '欢迎参与儿童早期疾病筛查评估。本项目通过两个阶段记录您在观看儿童行为视频、阅读筛查报告前后的判断，并收集您对报告的使用反馈。请根据观察到的事实与您的专业判断完成评估。

一、开始评估
请在“我的任务”中进入分配给您的儿童评估任务，核对儿童信息后开始评估。

二、阶段一：视频观察
本阶段仅展示儿童主视频。请观看视频并填写疾病判定、诊断信心和诊断依据。
请对以下四项分别评1–5分，分数越高表示严重程度越高：
社交回应：观察叫名、说话或主动接近时，儿童是否对人产生合理回应。
眼神交流：观察是否自然、适当地看向互动者。
社会互惠性：观察是否存在自然的来回互动。
社会情绪表达：观察情绪表达是否与互动情境匹配。
请记录重复刻板或感觉异常行为，例如重复动作、刻板摆弄物体、异常感觉兴趣等；未观察到时请填写“未观察到”。提交本阶段后进入阶段二。

三、阶段二：报告复核
请阅读筛查报告，结合主视频和报告中的相关证据完成综合评估。表单会预填阶段一的疾病判定、诊断信心和诊断依据，您可根据本阶段观察进行调整，并填写最终诊断结论。
请对报告有用程度、报告证据有效性、事实准确性分别评1–5分，分数越高分别表示报告越有用、证据越有效、事实越准确。核对后提交最终评估。

四、完成后：报告使用评价
请对报告是否帮助您快速找到患者核心问题、是否有助于提高判断质量或效率、观察事实是否准确，分别选择“符合”“一般”或“不符合”，并可填写改进建议。
每个任务的报告使用评价仅提交一次；您也可以从“我的任务”中进入已完成任务补填或查看评价。');

-- 10. 初始化管理员（与后端 DefaultValue.SALT + 密码的 MD5 规则保持一致）
-- 只创建管理员，不插入儿童、普通医生、任务或评估示例数据。
INSERT INTO `sys_doctor`
    (`id`, `doctor_name`, `hospital`, `account`, `password`, `role`, `status`, `is_deleted`)
VALUES
    (1, '系统管理员', '系统管理', 'admin', MD5(CONCAT('lxh', '123456')), 1, 1, 0);
