-- ============================================
-- Enterprise Automated Testing Platform
-- Database Initialization Script
-- ============================================
CREATE DATABASE IF NOT EXISTS test_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE test_platform;
-- --------------------------------------------
-- User Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
  `password` VARCHAR(255) NOT NULL COMMENT 'Password (BCrypt Encrypted)',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT 'Real Name',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone Number',
  `email` VARCHAR(100) DEFAULT NULL COMMENT 'Email Address',
  `status` INT DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User Table';
-- --------------------------------------------
-- Role Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `name` VARCHAR(50) NOT NULL COMMENT 'Role Name',
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Role Code',
  `description` VARCHAR(200) DEFAULT NULL COMMENT 'Description',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Role Table';
-- --------------------------------------------
-- Permission Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `name` VARCHAR(50) NOT NULL COMMENT 'Permission Name',
  `code` VARCHAR(100) NOT NULL UNIQUE COMMENT 'Permission Code',
  `type` VARCHAR(20) DEFAULT 'menu' COMMENT 'Type: menu/button/api',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Permission Table';
-- --------------------------------------------
-- Role-Permission Mapping Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `role_permission` (
  `role_id` BIGINT NOT NULL COMMENT 'Role ID',
  `permission_id` BIGINT NOT NULL COMMENT 'Permission ID',
  PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Role-Permission Mapping Table';
-- --------------------------------------------
-- User-Role Mapping Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `user_role` (
  `user_id` BIGINT NOT NULL COMMENT 'User ID',
  `role_id` BIGINT NOT NULL COMMENT 'Role ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'User-Role Mapping Table';
-- --------------------------------------------
-- Project Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `name` VARCHAR(100) NOT NULL COMMENT 'Project Name',
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Project Code',
  `owner` VARCHAR(50) DEFAULT NULL COMMENT 'Owner Username',
  `description` VARCHAR(500) DEFAULT NULL COMMENT 'Description',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Project Table';
-- --------------------------------------------
-- Test Environment Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `test_environment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `project_id` BIGINT NOT NULL COMMENT 'Project ID',
  `name` VARCHAR(100) NOT NULL COMMENT 'Environment Name',
  `type` VARCHAR(20) NOT NULL COMMENT 'Type: DEV/TEST/UAT/PROD',
  `url` VARCHAR(500) DEFAULT NULL COMMENT 'Environment URL',
  `description` VARCHAR(500) DEFAULT NULL COMMENT 'Description',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
  PRIMARY KEY (`id`),
  INDEX `idx_project_id` (`project_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Test Environment Table';
-- --------------------------------------------
-- Test Case Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `test_case` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `project_id` BIGINT NOT NULL COMMENT 'Project ID',
  `name` VARCHAR(200) NOT NULL COMMENT 'Test Case Name',
  `type` VARCHAR(20) NOT NULL COMMENT 'Type: UI/API/App',
  `priority` VARCHAR(10) DEFAULT 'P2' COMMENT 'Priority: P0/P1/P2/P3',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT 'Tags (comma-separated)',
  `git_path` VARCHAR(500) DEFAULT NULL COMMENT 'Git Repository Path',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT 'Description',
  `creator` VARCHAR(50) DEFAULT NULL COMMENT 'Creator Username',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
  PRIMARY KEY (`id`),
  INDEX `idx_project_id` (`project_id`),
  INDEX `idx_type` (`type`),
  INDEX `idx_priority` (`priority`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Test Case Table';
-- --------------------------------------------
-- Test Suite Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `test_suite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `project_id` BIGINT NOT NULL COMMENT 'Project ID',
  `name` VARCHAR(200) NOT NULL COMMENT 'Suite Name',
  `description` VARCHAR(500) DEFAULT NULL COMMENT 'Description',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
  PRIMARY KEY (`id`),
  INDEX `idx_project_id` (`project_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Test Suite Table';
-- --------------------------------------------
-- Suite-Case Mapping Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `suite_case` (
  `suite_id` BIGINT NOT NULL COMMENT 'Suite ID',
  `case_id` BIGINT NOT NULL COMMENT 'Case ID',
  PRIMARY KEY (`suite_id`, `case_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Suite-Case Mapping Table';
-- --------------------------------------------
-- Execution Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `execution` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `project_id` BIGINT NOT NULL COMMENT 'Project ID',
  `suite_id` BIGINT DEFAULT NULL COMMENT 'Suite ID (NULL for single case execution)',
  `case_id` BIGINT DEFAULT NULL COMMENT 'Case ID (NULL for suite execution)',
  `trigger_type` VARCHAR(20) NOT NULL COMMENT 'Trigger Type: MANUAL/SCHEDULED/JENKINS/API',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Status: PENDING/RUNNING/SUCCESS/FAILED/TIMEOUT',
  `start_time` DATETIME DEFAULT NULL COMMENT 'Start Time',
  `end_time` DATETIME DEFAULT NULL COMMENT 'End Time',
  `total_count` INT DEFAULT 0 COMMENT 'Total Test Count',
  `pass_count` INT DEFAULT 0 COMMENT 'Passed Count',
  `fail_count` INT DEFAULT 0 COMMENT 'Failed Count',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  PRIMARY KEY (`id`),
  INDEX `idx_project_id` (`project_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Execution Table';
-- --------------------------------------------
-- Execution Log Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `execution_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `execution_id` BIGINT NOT NULL COMMENT 'Execution ID',
  `case_id` BIGINT NOT NULL COMMENT 'Case ID',
  `status` VARCHAR(20) NOT NULL COMMENT 'Status: SUCCESS/FAILED/TIMEOUT/SKIPPED',
  `error_message` TEXT DEFAULT NULL COMMENT 'Error Message',
  `screenshot_path` VARCHAR(500) DEFAULT NULL COMMENT 'Screenshot File Path',
  `video_path` VARCHAR(500) DEFAULT NULL COMMENT 'Video File Path',
  `console_log` TEXT DEFAULT NULL COMMENT 'Console Log',
  `start_time` DATETIME DEFAULT NULL COMMENT 'Start Time',
  `end_time` DATETIME DEFAULT NULL COMMENT 'End Time',
  PRIMARY KEY (`id`),
  INDEX `idx_execution_id` (`execution_id`),
  INDEX `idx_case_id` (`case_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Execution Log Table';
-- --------------------------------------------
-- Report Table
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `execution_id` BIGINT NOT NULL COMMENT 'Execution ID',
  `title` VARCHAR(200) NOT NULL COMMENT 'Report Title',
  `summary` TEXT DEFAULT NULL COMMENT 'Report Summary',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  PRIMARY KEY (`id`),
  INDEX `idx_execution_id` (`execution_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Report Table';
-- ============================================
-- Initial Data
-- ============================================
-- Insert Default Admin User (password: admin123)
INSERT IGNORE INTO `user` (`username`, `password`, `email`, `status`)
VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    'admin@testplatform.com',
    1
  );
-- Insert Default Roles
INSERT IGNORE INTO `role` (`name`, `code`, `description`)
VALUES ('超级管理员', 'ADMIN', '系统超级管理员'),
  ('测试经理', 'TEST_MANAGER', '测试经理角色'),
  ('测试工程师', 'TEST_ENGINEER', '测试工程师角色'),
  ('开发工程师', 'DEVELOPER', '开发工程师角色'),
  ('只读用户', 'READER', '只读用户角色');
-- Insert Default Permissions
INSERT IGNORE INTO `permission` (`name`, `code`, `type`)
VALUES ('项目管理', 'project:manage', 'menu'),
  ('用例管理', 'case:manage', 'menu'),
  ('执行中心', 'execution:manage', 'menu'),
  ('报告中心', 'report:manage', 'menu'),
  ('用户管理', 'user:manage', 'menu'),
  ('角色管理', 'role:manage', 'menu'),
  ('权限配置', 'permission:manage', 'menu'),
  ('环境配置', 'env:manage', 'menu'),
  ('系统设置', 'system:manage', 'menu');
-- Assign all permissions to ADMIN role
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1,
  id
FROM `permission`;
-- Assign admin user to ADMIN role
INSERT IGNORE INTO `user_role` (`user_id`, `role_id`)
VALUES (1, 1);