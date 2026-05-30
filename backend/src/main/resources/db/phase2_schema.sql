SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `jenkins_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `project_id` BIGINT NOT NULL COMMENT 'Project ID',
  `name` VARCHAR(100) NOT NULL COMMENT 'Jenkins Config Name',
  `jenkins_url` VARCHAR(500) NOT NULL COMMENT 'Jenkins Server URL',
  `username` VARCHAR(100) NOT NULL COMMENT 'Jenkins Username',
  `api_token` VARCHAR(500) NOT NULL COMMENT 'Jenkins API Token',
  `job_name` VARCHAR(200) NOT NULL COMMENT 'Jenkins Job Name',
  `status` INT DEFAULT 1 COMMENT 'Status: 0=disabled, 1=enabled',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
  PRIMARY KEY (`id`),
  INDEX `idx_project_id` (`project_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Jenkins Configuration Table';

INSERT IGNORE INTO `permission` (`name`, `code`, `type`) VALUES
('Jenkins集成', 'jenkins:manage', 'menu'),
('执行触发', 'execution:trigger', 'button');

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `permission` WHERE `code` IN ('jenkins:manage', 'execution:trigger');
