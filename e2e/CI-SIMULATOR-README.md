# 🔄 GitHub Actions CI 本地模拟器

## 📋 目录

- [快速开始](#快速开始)
- [功能特性](#功能特性)
- [使用方式](#使用方式)
- [输出说明](#输出说明)
- [故障排查](#故障排查)
- [与真实 CI 的对比](#与真实-ci-的对比)

---

## 🚀 快速开始（3 步启动）

### 方式一：交互式菜单（推荐新手）

```bash
# 双击运行或命令行执行
cd e2e
run-ci-simulator.bat
```

然后选择：
1. Quick Smoke Test (3 个核心套件)
2. Full Regression (61 个全量测试)
3. Custom Tests (自定义文件)
4. Environment Check Only (仅检查环境)

### 方式二：命令行直接执行

```bash
# 1. 快速冒烟测试（推荐日常使用）
.\simulate-ci.ps1 -Mode Quick

# 2. 全量回归测试（推送前必跑）
.\simulate-ci.ps1 -Mode Full

# 3. 自定义测试文件
.\simulate-ci.ps1 -Mode Custom -TestFiles "01-login.spec.ts","04-testcase.spec.ts"

# 4. 显示详细日志
.\simulate-ci.ps1 -Mode Full -Verbose

# 5. 跳过环境检查（加速）
.\simulate-ci.ps1 -Mode Quick -SkipEnvCheck
```

---

## ✨ 功能特性

### 🎯 完整模拟 GitHub Actions 流程

| 模拟项 | 说明 | 对应 CI 行为 |
|--------|------|-------------|
| **环境变量** | 自动设置 `CI=true`, `GITHUB_ACTIONS=true` | CI 环境检测 |
| **7 步流程** | 环境检查 → 服务检测 → 配置验证 → 依赖检查 → 测试执行 → 结果收集 → 报告生成 | CI Job Steps |
| **HTML 报告** | 生成交互式 HTML 报告，类似 GitHub Artifacts | Upload Artifact |
| **退出码** | 成功返回 0，失败返回 1/2 | CI Status Check |
| **日志记录** | 完整保存测试输出到 `ci-simulation.log` | CI Logs |

### 📊 检查项目清单（自动验证）

#### Step 1: 环境检查（10+ 项）

```
✅ Node.js 版本 ≥ 18
✅ npm 版本 ≥ 8
✅ Java 版本 ≥ 17
✅ Maven 版本 ≥ 3.8
✅ Playwright ≥ 1.40
✅ Git 已安装
✅ E2E 目录结构完整
✅ 关键配置文件存在
✅ node_modules 已安装
```

#### Step 2: 服务健康检查（3 项）

```
✅ 前端 Vite 服务 (localhost:5173)
✅ 后端 Spring Boot (localhost:8080)
✅ 数据库连接（通过登录 API 验证）
```

#### Step 3: 配置验证（15+ 项）

```
✅ env.ts 配置完整性
   ├─ BASE_URL 定义
   ├─ 页面路由定义
   ├─ API 模式定义
   └─ 默认用户配置
   
✅ Playwright 配置正确性
   ├─ 测试目录设置
   ├─ Base URL 配置
   ├─ Headless 模式
   └─ 截图/Trace 策略
   
✅ Fixtures 导出完整性
   ├─ test fixture
   ├─ expect 导出
   ├─ BASE_URL 导出
   ├─ env 配置导出
   └─ loggedIn fixture 定义
```

#### Step 4: 依赖安装检查

```
✅ @playwright/test 包版本
✅ Playwright 浏览器已安装
✅ 工具函数库可用
   ├─ api-helper.ts
   └─ test-data.ts
```

#### Step 5: 执行 E2E 测试

根据模式运行不同范围的测试。

#### Step 6 & 7: 结果收集 + 报告

```
✅ HTML 报告生成
✅ 失败截图收集
✅ Trace 文件归档
✅ 控制台总结输出
✅ 自动打开报告浏览器
```

---

## 📖 使用场景

### 场景 1：首次配置 CI（推荐）

```bash
# 运行全量检查 + 快速测试
.\simulate-ci.ps1 -Mode Quick -Verbose

# 检查报告中的所有通过/失败项
# 修复所有 ❌ 标记的问题
```

### 场景 2：每次提交前

```bash
# 快速冒烟（3 分钟内完成）
.\simulate-ci.ps1 -Mode Quick

# 如果全部通过 ✅ → 可以安全 push
# 如果有失败 ❌ → 先修复再提交
```

### 场景 3：修改了某个测试文件后

```bash
# 仅测试特定文件（秒级反馈）
.\simulate-ci.ps1 -Mode Custom -TestFiles "04-testcase.spec.ts"
```

### 场景 4：调试 CI 问题

```bash
# 详细日志 + 全量测试
.\simulate-ci.ps1 -Mode Full -Verbose

# 查看 ci-simulation.log 了解详细信息
# 查看 ci-report.html 可视化结果
```

---

## 📄 输出文件说明

### 1️⃣ **控制台输出**

实时显示每个步骤的进度和结果：

```
═══════════════════════════════════════════════════════
  🔄 GitHub Actions E2E CI Simulator
  Mode: Quick | Time: 2026-05-30 15:30:00
═══════════════════════════════════════════════════════

━━━ Step 1/7: Environment Check ━━━
  ✅ Node.js v20.11.0
  ✅ npm v10.2.3
  ...
  
━━━ Step 2/7: Service Health Check ━━━
  ✅ Frontend (Vite) is running
  ✅ Backend (Spring Boot) is running
  ✅ Database connection OK
  
...

═══════════════════════════════════════════════════════
  📋 CI SIMULATION SUMMARY
  ┌─────────────────────────────────────┐
  │ Total Checks:  45                   │
  │ ✅ Passed:      43                   │
  │ ❌ Failed:      0                    │
  │ ⏭️  Skipped:     2                    │
  ├─────────────────────────────────────┤
  │ Pass Rate:     95.6%                 │
  └─────────────────────────────────────┘

  🎉 SUCCESS! All checks passed!
  Ready for GitHub Actions deployment!
═══════════════════════════════════════════════════════
```

### 2️⃣ **HTML 报告** (`e2e/ci-report.html`)

自动生成的精美报告，包含：

- 🎨 渐变色头部设计
- 📊 统计卡片（Total/Passed/Failed/Skipped）
- 📈 进度条可视化通过率
- 🔧 详细配置信息表格
- 🏷️ 状态徽章（READY / ISSUES DETECTED）
- 📱 响应式设计（支持移动端查看）

**特点：**
- 双击 `ci-report.html` 即可在浏览器打开
- 类似 GitHub Actions 的 Artifacts 页面
- 可分享给团队成员审查

### 3️⃣ **日志文件** (`e2e/ci-simulation.log`)

完整的 Playwright 测试原始输出：

```bash
# 查看最后 50 行
Get-Content e2e\ci-simulation.log -Tail 50

# 搜索关键字
Select-String -Path e2e\ci-simulation.log -Pattern "FAIL|ERROR"

# 查看测试耗时
Select-String -Path e2e\ci-simulation.log -Pattern "\(\d+\.\d+s\)"
```

---

## ⚙️ 高级用法

### 自定义环境变量

在脚本运行前设置环境变量来覆盖默认值：

```powershell
# 模拟不同的前端端口
$env:BASE_URL = "http://localhost:3000"
.\simulate-ci.ps1 -Mode Quick

# 使用自定义测试账号
$env:TEST_USERNAME = "testuser"
$env:TEST_PASSWORD = "testpass123"
.\simulate-ci.ps1 -Mode Full
```

### 仅运行部分步骤

```powershell
# 只检查环境和服务（不运行测试）
.\simulate-ci.ps1 -Mode Quick -SkipEnvCheck:$false
# 注意：这会跳过 Step 1 但保留其他步骤
```

### 批量自动化

创建一个 PowerShell 脚本定期运行：

```powershell
# auto-check.ps1 - 每 30 分钟运行一次快速检查
while ($true) {
    Write-Host "$(Get-Date) - Running scheduled check..."
    .\simulate-ci.ps1 -Mode Quick
    
    # 如果失败，发送通知（可选）
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ALERT: CI check failed!" -ForegroundColor Red
        # 这里可以添加邮件/Slack 通知逻辑
    }
    
    Start-Sleep -Seconds 1800  # 等待 30 分钟
}
```

---

## 🔍 与真实 GitHub Actions 的对比

| 特性 | 本地模拟器 | GitHub Actions |
|------|-----------|----------------|
| **执行环境** | 你的开发机器 | Ubuntu Linux VM |
| **服务容器** | 手动启动或已运行 | 自动 MySQL + Redis |
| **网络延迟** | ~0ms（本地） | ~10-100ms（容器间） |
| **并行度** | 取决于 CPU 核心 | 可配置 workers |
| **调试能力** | ✅ 极强（可断点） | ⚠️ 受限（需下载 logs） |
| **速度** | 🚀 快（本地资源） | 正常（云资源） |
| **成本** | 免费 | 免费额度有限制 |
| **报告持久化** | 本地文件 | GitHub Artifacts |
| **团队共享** | 需手动分享 | PR 中自动显示 |

### 💡 最佳实践建议

1. **开发阶段**：频繁使用 `Quick` 模式（< 5 分钟）
2. **提交前**：运行一次 `Full` 模式（~ 10 分钟）
3. **CI 失败时**：用模拟器复现问题并修复
4. **首次部署前**：确保模拟器 100% 通过

---

## 🐛 故障排查

### 常见问题

#### Q1: 提示 "Node.js not installed"

**解决方案：**
```bash
# 检查 Node.js 是否在 PATH 中
node --version

# 如果未安装，从 https://nodejs.org 下载安装
# 或使用 winget 安装
winget install OpenJS.NodeJS.LTS
```

#### Q2: 前端/后端服务未运行

**错误信息：**
```
❌ Frontend not responding
❌ Backend not responding
```

**解决方案：**

```bash
# 启动前端（终端 1）
cd frontend
npm run dev

# 启动后端（终端 2）
cd backend
mvn spring-boot:run

# 确认服务正常后再运行模拟器
```

#### Q3: Playwright 浏览器未安装

**解决方案：**
```bash
cd e2e
npx playwright install chromium --with-deps
```

#### Q4: 权限错误（PowerShell 策略）

**错误信息：**
```
simulate-ci.ps1 cannot be loaded because running scripts is disabled...
```

**解决方案：**
```bash
# 方法 1：临时允许（当前会话）
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

# 方法 2：永久允许（需要管理员权限）
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned

# 然后重新运行脚本
.\simulate-ci.ps1 -Mode Quick
```

#### Q5: 测试超时

**原因：** CI 环境比本地慢，可能需要调整超时时间

**解决方案：**
```powershell
# 在 env.ts 中增加超时（CI 环境）
$env:API_TIMEOUT = "20000"  # 默认 10000ms
.\simulate-ci.ps1 -Mode Quick
```

---

## 📁 文件清单

```
e2e/
├── simulate-ci.ps1          # ✨ 主脚本（CI 模拟器）
├── run-ci-simulator.bat     # ✨ Windows 快捷启动器
├── ci-report.html           # 📊 生成的 HTML 报告（运行后）
├── ci-simulation.log       # 📝 完整日志（运行后）
│
├── tests/
│   ├── config/env.ts       # 环境配置（已支持 CI 变量）
│   ├── fixtures.ts         # Playwright fixtures
│   └── utils/
│       ├── api-helper.ts   # API 辅助函数
│       └── test-data.ts    # 数据生成器
│
└── playwright-report/      # Playwright 原生报告
```

---

## 🎯 下一步操作

### 立即尝试（5 分钟体验）

```bash
# 1. 打开 PowerShell，进入 e2e 目录
cd d:\workplace\automated-testing-platform\e2e

# 2. 运行快速冒烟测试
.\simulate-ci.ps1 -Mode Quick

# 3. 查看结果
#   - 控制台会显示详细统计
#   - 浏览器自动打开 HTML 报告
#   - 日志保存在 ci-simulation.log
```

### 集成到工作流

```bash
# 在 .gitignore 中添加（可选）
# e2e/ci-report.html
# e2e/ci-simulation.log

# 提交到仓库供团队使用
git add simulate-ci.ps1 run-ci-simulator.bat
git commit -m "docs: add local CI simulator for E2E tests"
git push origin main
```

---

## ✅ 检查清单

使用模拟器前确认：

- [ ] Node.js ≥ 18 已安装
- [ ] Java ≥ 17 已安装（如需检查后端）
- [ ] Playwright 浏览器已安装（`npx playwright install chromium`）
- [ ] 前端服务正在运行（`npm run dev`）
- [ ] 后端服务正在运行（或跳过服务检查）
- [ ] PowerShell 执行策略允许（见故障排查 Q4）

---

**版本：** v1.0.0  
**最后更新：** 2026-05-30  
**适用平台：** Windows 10/11 + PowerShell 5.1+

---

💡 **提示：** 这个模拟器的目标是让你在**推送代码到 GitHub 前**就能发现 90% 的 CI 配置问题，避免反复提交调试！
