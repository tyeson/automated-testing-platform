SET NAMES utf8mb4;
TRUNCATE TABLE permission;
INSERT INTO permission (id, name, code, type) VALUES
(1, '项目管理', 'project:manage', 'menu'),
(2, '用例管理', 'case:manage', 'menu'),
(3, '执行中心', 'execution:manage', 'menu'),
(4, '报告中心', 'report:manage', 'menu'),
(5, '用户管理', 'user:manage', 'menu'),
(6, '角色管理', 'role:manage', 'menu'),
(7, '权限配置', 'permission:manage', 'menu'),
(8, '环境配置', 'env:manage', 'menu'),
(9, '系统设置', 'system:manage', 'menu');
TRUNCATE TABLE role_permission;
INSERT INTO role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9);
