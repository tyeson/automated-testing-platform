# GitHub Actions E2E 测试自动化配置指南

## 📋 目录

- [工作流概览](#工作流概览)
- [快速开始](#快速开始)
- [工作流详解](#工作流详解)
- [环境变量配置](#环境变量配置)
- [测试报告查看](#测试报告查看)
- [故障排查](#故障排查)
- [最佳实践](#最佳实践)

---

## 🎯 工作流概览

本项目包含 **2 个 GitHub Actions 工作流**：

| 工作流文件 | 触发条件 | 测试范围 | 预计耗时 |
|-----------|----------|----------|----------|
| **[e2e-tests.yml](.github/workflows/e2e-tests.yml)** | Push/PR 到 main/develop | 全量 61 个测试 | 8-12 分钟 |
| **[e2e-quick.yml](.github/workflows/e2e-quick.yml)** | Push/PR（排除文档变更）| 关键 3 个套件 (01/02/08) | 3-5 分钟 |

### 触发条件

```yaml
# 自动触发
on:
  push:
    branches: [ main, develop, 'feature/**' ]  # 推送到这些分支时触发
  pull_request:
    branches: [ main, develop ]                # PR 到这些分支时触发
  workflow_dispatch:                           # 手动触发（推荐用于调试）
```

---

## 🚀 快速开始

### 方式一：首次推送自动运行

1. 将代码推送到 GitHub：
```bash
git add .
git commit -m "feat: 添加 E2E 测试和 CI/CD 配置"
git push origin main
```

2. 访问 Actions 页面查看运行状态：
```
https://github.com/<your-repo>/actions
```

### 方式二：手动触发

1. 打开 GitHub 仓库 → **Actions** 标签页
2. 左侧选择 **"E2E Tests"** 或 **"E2E Quick Tests"**
3. 点击右侧 **"Run workflow"** 按钮
4. 选择分支，点击 **"Run workflow"**

### 方式三：本地预览（模拟 CI 环境）

```bash
# 设置环境变量模拟 CI
export CI=true
export GITHUB_ACTIONS=true

# 运行测试
cd e2e
npm test
```

---

## 📖 工作流详解

### 主工作流：`e2e-tests.yml`（全量测试）

#### 架构流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    GitHub Runner                             │
│                                                             │
│  ┌──────────────┐    ┌──────────────┐                       │
│  │   MySQL 8.0   │    │ Redis 7      │  ← Service Containers │
│  │  (port 3306)  │    │ (port 6379)  │                       │
│  └──────┬───────┘    └──────┬───────┘                       │
│         │                   │                                 │
│         ▼                   ▼                                 │
│  ┌──────────────────────────────────┐                        │
│  │        Backend (Spring Boot)     │                        │
│  │   mvn package → java -jar ...    │                        │
│  │          (port 8080)             │                        │
│  └─────────────────┬────────────────┘                        │
│                    │                                         │
│                    ▼                                         │
│  ┌──────────────────────────────────┐                        │
│  │       Frontend (Vite Preview)    │                        │
│  │     npm run build → preview      │                        │
│  │           (port 5173)            │                        │
│  └─────────────────┬────────────────┘                        │
│                    │                                         │
│                    ▼                                         │
│  ┌──────────────────────────────────┐                        │
│  │     Playwright E2E Tests (61)     │                        │
│  │   npx playwright test --reporter  │                        │
│  └──────────────────────────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

#### 关键步骤说明

**Step 1: 环境准备** (约 2 分钟)
```yaml
- name: Setup Node.js
  uses: actions/setup-node@v4
  with:
    node-version: '20'
    cache: 'npm'  # 缓存依赖加速构建

- name: Setup Java
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    cache: maven  # Maven 缓存
```

**Step 2: 服务容器启动** (自动)
```yaml
services:
  mysql:
    image: mysql:8.0
    env:
      MYSQL_ROOT_PASSWORD: Szna123456.
      MYSQL_DATABASE: test_platform
    ports: ['3306:3306']
    # 健康检查确保服务就绪
    
  redis:
    image: redis:7-alpine
    ports: ['6379:6379']
```

**Step 3: 数据库初始化**
```yaml
- name: Initialize database
  run: |
    # 按顺序执行 SQL 脚本
    mysql -h 127.0.0.1 -u root -pSzna123456. test_platform \
      < backend/src/main/resources/db/schema.sql
    mysql ... < phase2_schema.sql
    mysql ... < alter_user.sql
```

**Step 4: 启动前后端服务**
```yaml
# 后端：Maven 构建 + Spring Boot 启动
- name: Build and start backend
  run: |
    mvn clean package -DskipTests -q  # 跳过单元测试加速
    nohup java -jar target/*.jar --spring.profiles.active=dev &

# 前端：Vite 构建预览模式
- name: Build and start frontend
  run: |
    npm run build
    nohup npm run preview -- --port 5173 &
```

**Step 5: 健康检查与等待**
```yaml
- name: Wait for services to be ready
  run: |
    # 循环检测直到服务可用（带超时）
    timeout 60s bash -c '
      until curl -sf http://localhost:8080/api/auth/login > /dev/null; do 
        sleep 2; 
      done'
```

**Step 6: 执行 E2E 测试**
```yaml
- name: Run E2E tests
  working-directory: ./e2e
  run: |
    npx playwright test --reporter=list,html 2>&1 | tee test-output.txt
```

**Step 7: 结果收集与上传**
```yaml
# 上传 HTML 报告（始终）
- uses: actions/upload-artifact@v4
  with:
    name: playwright-report
    path: e2e/playwright-report/

# 失败时上传截图和日志
- if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: test-screenshots
    path: e2e/test-results/
```

---

## ⚙️ 环境变量配置

### 方法一：在 `config/env.ts` 中配置（推荐）

项目已支持通过**环境变量**覆盖默认值：

```typescript
// config/env.ts
export const env = {
  BASE_URL: process.env.BASE_URL || 'http://localhost:5173',
  
  DEFAULT_USER: {
    username: process.env.TEST_USERNAME || 'admin',  // ✅ 可配置
    password: process.env.TEST_PASSWORD || 'admin123' // ✅ 可配置
  },
  
  API_TIMEOUT: parseInt(process.env.API_TIMEOUT || '10000', 10),
  
  isCI: process.env.CI === 'true' || process.env.GITHUB_ACTIONS === 'true'
}
```

### 方法二：在 GitHub Secrets 中存储敏感信息

1. 进入仓库设置：**Settings → Secrets and variables → Actions**
2. 添加 Repository secrets：

| Secret 名称 | 说明 | 示例值 |
|-------------|------|--------|
| `TEST_USERNAME` | 测试账号用户名 | `admin` |
| `TEST_PASSWORD` | 测试账号密码 | `admin123` |
| `MYSQL_ROOT_PASSWORD` | 数据库 root 密码 | `Szna123456.` |

3. 在工作流中使用：
```yaml
env:
  TEST_USERNAME: ${{ secrets.TEST_USERNAME }}
  TEST_PASSWORD: ${{ secrets.TEST_PASSWORD }}
```

### 方法三：在工作流中直接配置

```yaml
- name: Run E2E tests
  env:
    BASE_URL: 'http://localhost:5173'
    API_TIMEOUT: '15000'  # CI 环境可适当增加超时
  run: npx playwright test
```

---

## 📊 测试报告查看

### 方式一：GitHub Artifacts（推荐）

1. 打开失败的 Workflow 运行
2. 滚动到页面底部的 **Artifacts** 区域
3. 下载并解压 `playwright-report.zip`
4. 用浏览器打开 `index.html`

### 方式二：Playwright Test Results Viewer

如果安装了 VS Code 插件：
```bash
# 安装插件后直接打开
code e2e/playwright-report/index.html
```

### 方式三：命令行查看摘要

Workflow 运行完成后，在 **Summary** 标签页会显示：
```
## ✅ All E2E Tests Passed!

### Test Output
```
✅  1 登录与认证 › 页面加载正常 (3.2s)
✅  2 登录与认证 › 正确账号密码登录成功 (7.8s)
...
```
```

---

## 🔧 故障排查

### 常见问题及解决方案

#### 问题 1：MySQL 连接失败

**错误信息：**
```
Could not create connection to database server
```

**解决方案：**
1. 检查 MySQL 服务是否启动：
```bash
docker ps | grep mysql
```
2. 检查端口是否正确映射
3. 验证密码是否匹配 `application-dev.yml`

#### 问题 2：前端无法访问

**错误信息：**
```
TimeoutError: page.goto: Timeout 30000ms exceeded
```

**解决方案：**
1. 增加 Vite preview 启动等待时间
2. 检查 `vite.config.ts` 的 server 配置
3. 在 CI 日志中查看 `/tmp/frontend.log`

#### 问题 3：Playwright 浏览器未安装

**错误信息：**
```
Executable doesn't exist at .../chromium-xxxx/chrome
```

**解决方案：**
```bash
cd e2e
npx playwright install chromium --with-deps
```

#### 问题 4：测试超时

**解决方案：**
1. 在 CI 环境中增加超时时间：
```yaml
env:
  API_TIMEOUT: '20000'  # 默认 10000ms
```
2. 或修改 `playwright.config.ts`：
```typescript
timeout: 60000,  // 全局超时改为 60s
use: {
  actionTimeout: 15000,  // 操作超时 15s
}
```

#### 问题 5：并发测试导致数据冲突

**解决方案：**
1. 减少并行 worker 数量：
```yaml
# playwright.config.ts
fullyParallel: false,
workers: 1,  // CI 环境建议串行执行
```
2. 使用唯一数据生成器（已实现）：
```typescript
import { generateTestCaseName } from './utils/test-data'

const caseName = generateTestCaseName('UI')  // 包含随机数
```

---

## 💡 最佳实践

### 1️⃣ 分层测试策略

```
┌─────────────────────────────────────────────┐
│  PR 触发：快速冒烟测试 (e2e-quick.yml)        │
│  ├─ 01-login.spec.ts (登录认证)              │
│  ├─ 02-dashboard.spec.ts (核心页面)          │
│  └─ 08-api-intercept.spec.ts (API验证)      │
│  预计耗时：3-5 分钟                          │
├─────────────────────────────────────────────┤
│  Main 分支合并：全量回归 (e2e-tests.yml)      │
│  ├─ 01-07 业务功能测试                      │
│  └─ 08 API 拦截测试                         │
│  预计耗时：8-12 分钟                         │
└─────────────────────────────────────────────┘
```

### 2️⃣ 缓存优化

工作流已配置多层缓存：
```yaml
# Node.js 依赖缓存
cache: 'npm'
cache-dependency-path: frontend/package-lock.json

# Playwright 浏览器缓存
uses: actions/cache@v4
with:
  path: ~/.cache/ms-playwright/
  key: playwright-${{ runner.os }}-...

# Maven 依赖缓存
cache: maven
```

**效果：** 首次运行 ~10 分钟，后续 ~5 分钟

### 3️⃣ 失败重试机制

**方法 A：Playwright 内置重试**
```typescript
// playwright.config.ts
retries: 1,  // 失败自动重试 1 次
```

**方法 B：GitHub Actions 重试步骤**
```yaml
- name: Run E2E tests
  retry: 2  # 步骤级别重试
  run: npx playwright test
```

### 4️⃣ 并行化策略

根据资源选择合适的并行度：

| 场景 | workers 配置 | 说明 |
|------|--------------|------|
| **本地开发** | `workers: 4` | 利用多核 CPU |
| **CI 快速测试** | `workers: 2` | 平衡速度和稳定性 |
| **CI 全量测试** | `workers: 1` | 避免数据竞争 |

### 5️⃣ 测试数据隔离

使用工具库生成唯一数据：

```typescript
// utils/test-data.ts
export function generateTestCaseName(type: 'UI' | 'API') {
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 1000)
  return `E2E${type}Test_${timestamp}_${random}`
}

// 使用示例
const caseName = generateTestCaseName('UI')
// 输出: E2EUITest_1717048800000_723
```

### 6️⃣ 监控与告警（可选）

集成 Slack/Email 通知：

```yaml
- name: Notify on failure
  if: failure()
  uses: slackapi/slack-github-action@v1.24.0
  with:
    payload: |
      {
        "text": "❌ E2E 测试失败",
        "blocks": [
          {
            "type": "section",
            "text": {
              "type": "mrkdwn",
              "text": "*Repository:* ${{ github.repository }}\n*Branch:* ${{ github.ref }}\n*Commit:* ${{ github.sha }}"
            }
          }
        ]
      }
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}
```

---

## 📁 文件结构总览

```
.github/
└── workflows/
    ├── e2e-tests.yml        # 全量测试工作流（主）
    └── e2e-quick.yml        # 快速冒烟测试（轻量）

e2e/
├── tests/
│   ├── config/
│   │   ├── env.ts          # ✅ 支持环境变量配置
│   │   └── README.md       # 配置说明文档
│   ├── utils/
│   │   ├── api-helper.ts   # API 验证辅助函数
│   │   └── test-data.ts    # 唯一数据生成器
│   ├── fixtures.ts         # Playwright fixtures
│   ├── playwright.config.ts # Playwright 配置
│   └── *.spec.ts           # 测试用例 (01-08)
├── playwright-report/      # 生成的测试报告
└── test-results/           # 测试结果（失败时的截图/trace）
```

---

## 🔄 工作流生命周期示例

### 场景：开发者提交代码

```
Developer pushes code to feature/login-fix
         ↓
GitHub detects push to feature/** branch
         ↓
Trigger: .github/workflows/e2e-quick.yml
         ↓
┌─────────────────────────────────────┐
│  ✅ Checkout code                  │
│  ✅ Setup Node.js + Java           │
│  ✅ Start MySQL + Redis services   │
│  ✅ Initialize database schema     │
│  ✅ Build & start backend (30s)    │
│  ✅ Build & start frontend (15s)   │
│  ✅ Run quick smoke tests (3 files) │
│  ├─ 01-login.spec.ts ✓ (4/4)      │
│  ├─ 02-dashboard.spec.ts ✓ (3/3)  │
│  └─ 08-api-intercept.spec.ts ✓(12/12)│
│  ✅ Upload report artifact         │
│  ✅ Post summary to PR             │
└─────────────────────────────────────┘
         ↓
Result: ✅ All tests passed (19/19)
         ↓
PR can be safely merged! 🎉
```

---

## 📞 获取帮助

遇到问题？请按以下顺序排查：

1. **查看 CI 日志** - GitHub Actions 页面的详细输出
2. **下载 Artifacts** - 查看 screenshot 和 trace 文件
3. **本地复现** - 使用相同命令在本地运行
4. **查阅本文档** - 故障排查章节
5. **提交 Issue** - 包含完整的错误日志和环境信息

---

## ✅ 检查清单

部署前确认以下事项：

- [ ] `.github/workflows/e2e-tests.yml` 已推送到仓库
- [ ] `backend/src/main/resources/db/*.sql` 文件完整
- [ ] `application-dev.yml` 中的数据库密码与 workflow 一致
- [ ] `frontend/package.json` 和 `e2e/package.json` 依赖完整
- [ ] 本地能成功运行 `npx playwright test`
- [ ] （可选）配置了 GitHub Secrets 存储敏感信息

---

**最后更新：** 2026-05-30  
**维护者：** E2E 测试团队  
**版本：** v1.0.0
