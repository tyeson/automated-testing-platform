SET NAMES utf8mb4;
TRUNCATE TABLE role;
INSERT INTO role (id, name, code, description) VALUES
(1, '超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限'),
(2, '测试经理', 'TEST_MANAGER', '测试经理，管理测试项目和用例'),
(3, '测试工程师', 'TEST_ENGINEER', '测试工程师，执行测试用例'),
(4, '开发人员', 'DEVELOPER', '开发人员，查看测试报告'),
(5, '只读用户', 'READER', '只读用户，仅查看权限');
DELETE FROM user_role WHERE user_id = 1;
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);
UPDATE user SET real_name = '系统管理员' WHERE username = 'admin';
