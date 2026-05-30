# E2E 测试环境配置说明

## 📁 配置文件结构

```
e2e/tests/
├── config/
│   └── env.ts          # 统一环境配置（唯一数据源）
├── fixtures.ts         # Playwright fixtures（引用 env）
├── utils/
│   └── api-helper.ts   # API 验证辅助函数
└── *.spec.ts           # 测试文件（通过 fixtures 引用 env）
```

## 🔧 使用方式

### 基础用法（已实现）

```typescript
// ✅ 正确：从 fixtures 导入 env
import { test, expect, BASE_URL, env } from './fixtures'

// 使用环境变量
await page.goto(BASE_URL + env.LOGIN_PAGE)        // 页面路径
await page.fill('...', env.DEFAULT_USER.username) // 默认用户
resp.url().includes(env.API_PATTERNS.PROJECTS)    // API 模式
{ timeout: env.API_TIMEOUT }                       // 超时配置
```

### ❌ 禁止的写法

```typescript
// ❌ 错误：硬编码 URL
await page.goto('http://localhost:5173/login')

// ❌ 错误：硬编码超时
await expect(locator).toBeVisible({ timeout: 10000 })

// ❌ 错误：硬编码用户名密码
await page.fill('username', 'admin')
```

## 📋 配置项清单

### 服务器地址
| 变量 | 值 | 用途 |
|------|-----|------|
| `BASE_URL` | `http://localhost:5173` | 前端服务地址 |

### 页面路径
| 变量 | 值 | 示例 |
|------|-----|------|
| `LOGIN_PAGE` | `/login` | 登录页 |
| `DASHBOARD_PAGE` | `/dashboard` | 工作台 |
| `PROJECT_PAGE` | `/project` | 项目管理 |
| `TESTCASE_PAGE` | `/testcase` | 用例管理 |
| `EXECUTION_PAGE` | `/execution` | 执行中心 |
| `REPORT_PAGE` | `/report` | 报告中心 |
| `SYSTEM_USER_PAGE` | `/system/user` | 用户管理 |
| `SYSTEM_ROLE_PAGE` | `/system/role` | 角色管理 |
| `SYSTEM_ENVIRONMENT_PAGE` | `/system/environment` | 环境管理 |
| `SYSTEM_JENKINS_PAGE` | `/system/jenkins` | Jenkins集成 |

### API 接口模式
| 变量 | 值 | 说明 |
|------|-----|------|
| `API_PATTERNS.AUTH_LOGIN` | `/api/auth/login` | 登录接口 |
| `API_PATTERNS.PROJECTS` | `/api/projects` | 项目 CRUD |
| `API_PATTERNS.TESTCASES` | `/api/testcases` | 用例 CRUD |
| `API_PATTERNS.USERS` | `/api/users` | 用户管理 |
| `API_PATTERNS.ROLES` | `/api/roles` | 角色管理 |
| `API_PATTERNS.PERMISSIONS` | `/api/permissions` | 权限配置 |
| `API_PATTERNS.REPORTS` | `/api/reports` | 报告接口 |
| `API_PATTERNS.EXECUTIONS` | `/api/executions` | 执行记录 |
| `API_PATTERNS.ENVIRONMENTS` | `/api/environments` | 环境管理 |
| `API_PATTERNS.JENKINS_CONFIGS` | `/api/jenkins/configs` | Jenkins配置 |

### 超时配置（毫秒）
| 变量 | 值 | 用途 |
|------|-----|------|
| `API_TIMEOUT` | `10000` | API 响应等待 |
| `DIALOG_TIMEOUT` | `5000` | 弹窗操作等待 |
| `NAVIGATION_TIMEOUT` | `5000` | 页面导航等待 |

### 默认测试账号
```typescript
DEFAULT_USER: {
  username: 'admin',
  password: 'admin123'
}
```

### 测试数据前缀（避免冲突）
| 变量 | 值 | 用途 |
|------|-----|------|
| `TEST_USER_PREFIX` | `'e2e_'` | 测试用户名 |
| `TEST_PROJECT_PREFIX` | `'E2E测试项目_'` | 测试项目名 |
| `TEST_CASE_PREFIX` | `'E2E登录功能测试_'` | 测试用例名 |
| `TEST_ROLE_PREFIX` | `'E2E测试角色_'` | 测试角色名 |

## 🚀 扩展配置

如需支持多环境（dev/staging/prod），可修改 `config/env.ts`：

```typescript
const ENV = process.env.TEST_ENV || 'development'

const configs = {
  development: {
    BASE_URL: 'http://localhost:5173',
    API_TIMEOUT: 10000,
  },
  staging: {
    BASE_URL: 'https://staging.example.com',
    API_TIMEOUT: 15000,
  },
  production: {
    BASE_URL: 'https://example.com',
    API_TIMEOUT: 20000,
  }
}

export const env = { ...configs[ENV], /* 其他公共配置 */ }
```

运行时指定环境：
```bash
# Windows PowerShell
$env:TEST_ENV="staging"; npx playwright test

# Linux/Mac
TEST_ENV=staging npx playwright test
```

## ✅ 已完成的优化

1. **消除所有硬编码 URL** - 仅在 `config/env.ts` 中保留一处定义
2. **统一导出** - 通过 `fixtures.ts` 导出 `{ test, expect, BASE_URL, env }`
3. **类型安全** - TypeScript 类型提示，避免拼写错误
4. **集中管理** - 修改一处即可全局生效

## 📊 重构统计

| 文件 | 修复前 | 修复后 |
|------|--------|--------|
| `01-login.spec.ts` | 4 处硬编码 URL | 0 处 ✅ |
| `fixtures.ts` | 1 处硬编码 + 3 处魔法值 | 全部引用 env ✅ |
| **其他 6 个文件** | 使用 BASE_URL（已正确） | 可选优化为使用页面常量 |

## 💡 最佳实践

1. **新增测试文件时**：始终从 `fixtures` 导入 `env`
2. **添加新页面时**：先在 `env.ts` 中添加页面路径常量
3. **调用新接口时**：先在 `env.API_PATTERNS` 中添加模式
4. **修改超时时**：优先使用已有的超时常量，必要时新增
