export const env = {
  BASE_URL: process.env.BASE_URL || 'http://localhost:5173',
  API_TIMEOUT: parseInt(process.env.API_TIMEOUT || '10000', 10),
  DIALOG_TIMEOUT: parseInt(process.env.DIALOG_TIMEOUT || '5000', 10),
  NAVIGATION_TIMEOUT: parseInt(process.env.NAVIGATION_TIMEOUT || '5000', 10),
  
  LOGIN_PAGE: '/login',
  DASHBOARD_PAGE: '/dashboard',
  PROJECT_PAGE: '/project',
  TESTCASE_PAGE: '/testcase',
  EXECUTION_PAGE: '/execution',
  REPORT_PAGE: '/report',
  SYSTEM_USER_PAGE: '/system/user',
  SYSTEM_ROLE_PAGE: '/system/role',
  SYSTEM_ENVIRONMENT_PAGE: '/system/environment',
  SYSTEM_JENKINS_PAGE: '/system/jenkins',

  DEFAULT_USER: {
    username: process.env.TEST_USERNAME || 'admin',
    password: process.env.TEST_PASSWORD || 'admin123'
  },

  TEST_USER_PREFIX: 'e2e_',
  TEST_PROJECT_PREFIX: 'E2E测试项目_',
  TEST_CASE_PREFIX: 'E2E登录功能测试_',
  TEST_ROLE_PREFIX: 'E2E测试角色_',

  API_PATTERNS: {
    AUTH_LOGIN: '/api/auth/login',
    PROJECTS: '/api/projects',
    TESTCASES: '/api/testcases',
    USERS: '/api/users',
    ROLES: '/api/roles',
    PERMISSIONS: '/api/permissions',
    REPORTS: '/api/reports',
    EXECUTIONS: '/api/executions',
    ENVIRONMENTS: '/api/environments',
    JENKINS_CONFIGS: '/api/jenkins/configs'
  },

  isCI: process.env.CI === 'true' || process.env.GITHUB_ACTIONS === 'true'
} as const

export type EnvConfig = typeof env
