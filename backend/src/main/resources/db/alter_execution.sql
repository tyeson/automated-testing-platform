-- Migration: Add environment/creator to execution table
ALTER TABLE `execution`
  ADD COLUMN `environment` VARCHAR(20) DEFAULT NULL COMMENT 'Execution Environment: DEV/TEST/UAT/PROD' AFTER `fail_count`,
  ADD COLUMN `creator`     VARCHAR(50) DEFAULT NULL COMMENT 'Triggered By Username' AFTER `environment`,
  ADD INDEX `idx_environment` (`environment`);
