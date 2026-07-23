# NeoScholar — AI 驱动的智能学伴平台

> 基于大语言模型，为学生提供**个性化学习画像**、**定制学习路径**、**智能辅导对话**和**自动资源生成**的一站式学习平台。

---

##  功能概览

| 模块 | 说明 |
|---|---|
|   **学生画像管理** | 创建、编辑、合并学生画像，描述专业方向、学习目标、知识基础、思维风格等 |
|   **AI 画像合并** | 调用 LLM 智能融合两份画像，提取互补信息生成综合画像 |
|   **学习路径生成** | 基于学生画像，AI 自动生成 5-8 节点的个性化学习路线图 |
|   **学习资源生成** | 为每个路径节点自动生成 PPT 大纲和测验题目 |
|   **AI 智能辅导** | 基于画像的对话式 AI 导师，支持 SSE 流式输出 |
|   **Dashboard 仪表盘** | 学习数据统计、活跃度热力图、路径进度追踪 |
| ✅ **用户认证** | JWT 登录/注册、路由守卫、请求拦截器自动携带 Token |

---

##  技术架构

```
┌─────────────────────────────────────────────────┐
│                   Frontend                       │
│        Vue 3 + Element Plus + Pinia              │
│              Vite · Axios · SSE                  │
└────────────────────┬────────────────────────────┘
                     │ REST API / SSE
┌────────────────────┴────────────────────────────┐
│                   Backend                        │
│        Spring Boot 3.5 + MyBatis-Plus            │
│     Spring Security + JWT + LangChain4j          │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────┐
│               Infrastructure                     │
│     MySQL 8.0 · Redis 7 · Qwen (DashScope)       │
│          Docker · Docker Compose · CI/CD         │
└─────────────────────────────────────────────────┘
```

### 技术栈

| 层级 | 技术 |
|---|---|
| **前端** | Vue 3 (Composition API), Element Plus, Pinia, Vue Router, Axios, Vite |
| **后端** | Java 21, Spring Boot 3.5, MyBatis-Plus, Spring Security, JJWT |
| **AI** | LangChain4j + Qwen (通义千问) via DashScope API |
| **数据** | MySQL 8.0, Redis 7 |
| **测试** | JUnit 5, Vitest, @vue/test-utils |
| **部署** | Docker, Docker Compose, GitHub Actions CI/CD |

---

##  项目结构

```
NeoScholar/
├── Backend/                          # Spring Boot 后端
│   ├── src/main/java/com/javaee/backend/
│   │   ├── AIService/               # LangChain4j AI 服务接口
│   │   ├── config/                  # 配置（Security, Async, DataInit）
│   │   ├── controller/              # REST 控制器
│   │   ├── entity/                  # 数据库实体
│   │   ├── mapper/                  # MyBatis-Plus Mapper
│   │   ├── po/dto/                  # 数据传输对象
│   │   ├── security/                # JWT 工具类 & 过滤器
│   │   ├── service/                 # 业务服务接口 & 实现
│   │   └── tools/                   # LangChain4j @Tool
│   └── src/main/resources/
│       ├── application.yaml         # 主配置
│       ├── application-ci.yml       # CI 环境配置
│       ├── schema.sql               # 数据库建表脚本
│       └── SystemPrompt.txt         # AI 系统提示词
├── Frontend/                        # Vue 3 前端
│   └── src/
│       ├── api/                     # Axios API 封装
│       ├── composables/             # 组合式函数（useChat）
│       ├── layouts/                 # 布局组件
│       ├── router/                  # 路由配置 + 守卫
│       ├── stores/                  # Pinia 状态管理
│       └── views/                   # 页面组件
├── .github/workflows/               # CI/CD 流水线
├── Dockerfile                       # 多阶段 Docker 构建
├── docker-compose.yml               # 一键部署配置
└── docs/                            # 文档
```

---

##  快速开始

### 环境要求

- **JDK 21**+
- **Maven 3.9**+
- **Node.js 22**+
- **MySQL 8.0**
- **Redis 7**
- **Qwen API Key**（[DashScope](https://dashscope.aliyuncs.com/) 获取）

### 1. 克隆项目

```bash
git clone https://github.com/VibingzzZ/NeoScholar.git
cd NeoScholar
```

### 2. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS neo_scholar DEFAULT CHARSET utf8mb4;"
mysql -u root -p neo_scholar < Backend/src/main/resources/schema.sql
```

### 3. 配置环境变量

```bash
# Linux/macOS
export QWEN_API_KEY="你的 DashScope API Key"

# Windows PowerShell
$env:QWEN_API_KEY="你的 DashScope API Key"
```

### 4. 启动后端

```bash
cd Backend
mvn spring-boot:run
```

后端启动后访问 `http://localhost:8080`，默认用户 `demo / 123456` 自动创建。

### 5. 启动前端

```bash
cd Frontend
npm install
npm run dev
```

前端启动后访问 `http://localhost:5173`

### 6. Docker 一键部署

```bash
# 设置 API Key
export QWEN_API_KEY="你的 DashScope API Key"

# 启动所有服务（MySQL + Redis + 应用）
docker compose up -d
```

---

##  API 概览

### 认证
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册 |

### 学生画像
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/user/profile/detail/{id}` | 查询画像 |
| GET | `/api/user/profile/list/{userId}` | 画像列表 |
| POST | `/api/user/profile/create` | 创建画像 |
| POST | `/api/user/profile/update` | 更新画像 |
| POST | `/api/user/profile/merge/{id}/{userId}` | AI 合并画像 |

### 学习路径
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/learning-path/list/{userId}` | 路径列表 |
| GET | `/api/learning-path/detail/{pathId}` | 路径详情 |
| POST | `/api/learning-path/generate/{userId}` | 生成路径 |
| POST | `/api/learning-path/progress` | 更新进度 |

### 智能辅导
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/chat/stream` | SSE 流式对话 |

### Dashboard
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/dashboard/stats/{userId}` | 学习统计 + 热力图 |

---

##  数据库表

| 表名 | 说明 |
|---|---|
| `users` | 用户表（BCrypt 加密密码） |
| `student_profile` | 学生画像 |
| `learning_paths` | 学习路径（节点存为 JSON） |
| `learning_resources` | 学习资源（PPT/测验） |
| `chat_messages` | 聊天记录 |

---

##  开发指南

### 运行测试

```bash
# 后端测试
cd Backend
mvn test

# 前端测试
cd Frontend
npm test
```

### 分支策略

```
feature/xxx  →  develop  →  main
   功能开发      集成分支    生产分支
```

---

##  开源项目使用声明

本项目在开发过程中使用了以下开源项目和 AI 工具/框架，在此标注名称、来源及协议要求：

### 后端框架与库

| 项目名称 | 版本 | 来源 | 协议 | 用途 |
|----------|------|------|------|------|
| Spring Boot | 3.5.14 | https://spring.io/projects/spring-boot | Apache 2.0 | 后端应用框架 |
| MyBatis-Plus | 3.5.10.1 | https://github.com/baomidou/mybatis-plus | Apache 2.0 | ORM 数据库操作 |
| LangChain4j | 1.0.1-beta6 | https://github.com/langchain4j/langchain4j | Apache 2.0 | LLM 集成框架，AI 服务编排 |
| Spring Security | 6.x (随 Boot) | https://spring.io/projects/spring-security | Apache 2.0 | 认证与授权 |
| JJWT | 0.12.6 | https://github.com/jwtk/jjwt | Apache 2.0 | JWT Token 生成与验证 |
| Lombok | 最新 | https://projectlombok.org | MIT | 简化 Java 代码 |
| MySQL Connector/J | 最新 | https://github.com/mysql/mysql-connector-j | GPL 2.0 | MySQL 数据库驱动 |

### 前端框架与库

| 项目名称 | 版本 | 来源 | 协议 | 用途 |
|----------|------|------|------|------|
| Vue 3 | ^3.5.34 | https://github.com/vuejs/core | MIT | 前端框架 |
| Element Plus | ^2.12.0 | https://github.com/element-plus/element-plus | MIT | UI 组件库 |
| Pinia | ^3.0.4 | https://github.com/vuejs/pinia | MIT | 状态管理 |
| Vue Router | ^4.5.1 | https://github.com/vuejs/router | MIT | 前端路由 |
| Axios | ^1.12.2 | https://github.com/axios/axios | MIT | HTTP 客户端 |
| Vite | ^8.0.12 | https://github.com/vitejs/vite | MIT | 构建工具 |
| marked | 最新 | https://github.com/markedjs/marked | MIT | Markdown 渲染 |

### AI 模型与工具

| 工具/服务 | 来源 | 协议/条款 | 用途 |
|-----------|------|-----------|------|
| DeepSeek API | https://platform.deepseek.com | DeepSeek 使用条款 | 大语言模型对话生成 |
| LangChain4j Agent/Tool | https://docs.langchain4j.dev | Apache 2.0 | 多智能体协同架构 |

### 基础设施

| 项目名称 | 来源 | 协议 | 用途 |
|----------|------|------|------|
| MySQL 8.0 | https://www.mysql.com | GPL 2.0 | 关系型数据库 |
| Redis 7 | https://redis.io | BSD 3-Clause | 缓存 & 会话存储 |
| Docker | https://www.docker.com | Apache 2.0 | 容器化部署 |

> **注意**：以上所有开源项目均为其各自所有者的财产。本项目遵循 [Apache License 2.0](LICENSE) 协议，使用上述开源项目时已遵守其各自的许可条款。

##  License

[Apache License 2.0](LICENSE)
