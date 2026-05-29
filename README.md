# 企业自动化测试管理平台

基于 Vue3 + Spring Boot + Playwright 的企业级自动化测试管理平台。

## 技术栈

### 前端

- Vue3 + TypeScript + Vite
- Element Plus UI
- Pinia 状态管理
- Vue Router 路由
- ECharts 图表

### 后端

- Spring Boot 3.2.5
- Spring Security + JWT
- MyBatis Plus
- MySQL 8.0
- Redis

### 自动化

- Playwright
- Jenkins CI/CD

## 快速开始

### 方式一：Docker Compose（推荐）

```bash
docker-compose up -d
```

访问 http://localhost

默认账号：admin / admin123

### 方式二：本地开发

#### 后端启动

```bash
cd backend
mvn spring-boot:run
```

后端运行在 http://localhost:8080

#### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端运行在 http://localhost:5173

## 项目结构

```
automated-testing-platform/
├── frontend/                 # Vue3 前端项目
│   ├── src/
│   │   ├── api/             # API 接口层
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 通用组件
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── router/          # 路由配置
│   │   └── types/           # TypeScript 类型定义
│   ├── package.json
│   ├── vite.config.ts
│   └── Dockerfile
├── backend/                  # Spring Boot 后端项目
│   ├── src/main/java/
│   │   └── com/testplatform/
│   │       ├── controller/  # REST API 控制器
│   │       ├── service/     # 业务逻辑层
│   │       ├── mapper/      # MyBatis Plus Mapper
│   │       ├── entity/      # 数据库实体
│   │       ├── config/      # 配置类
│   │       ├── security/    # JWT 安全模块
│   │       └── dto/         # 数据传输对象
│   ├── src/main/resources/
│   │   └── db/schema.sql    # 数据库初始化脚本
│   ├── pom.xml
│   └── Dockerfile
├── docker-compose.yml        # Docker 编排配置
└── docs/                     # 需求文档
```

## 功能模块

### MVP 阶段（第一期）

- 登录权限管理
- 项目管理
- 执行中心
- 报告中心

### 第二阶段

- Jenkins 集成
- Playwright 集成
- 视频回放

### 第三阶段

- AI 生成脚本
- AI 失败分析

## 默认账号

| 角色       | 用户名 | 密码     |
| ---------- | ------ | -------- |
| 超级管理员 | admin  | admin123 |

## API 文档

后端 API 文档访问：http://localhost:8080/swagger-ui.html

## 开发指南

### 前端开发

```bash
cd frontend
npm run dev          # 启动开发服务器
npm run build        # 生产构建
npm run lint         # 代码检查
```

### 后端开发

```bash
cd backend
mvn spring-boot:run  # 启动后端服务
mvn test             # 运行测试
mvn clean package    # 打包
```

## License

MIT
