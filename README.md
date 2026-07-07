# NeoScholar

基于 AI 大模型的智能学习辅导平台，提供个性化学习路径规划、学习资源生成和智能问答服务。

## 项目结构

```
NeoScholar/
├── Backend/              # 后端服务
│   ├── AIService/        # AI Agent（PPT生成、出题、路径规划、画像合并、对话）
│   ├── config/           # Spring 配置
│   ├── controller/       # REST 接口
│   ├── entity/           # 数据实体
│   ├── mapper/           # MyBatis Plus Mapper
│   ├── service/          # 业务逻辑
│   └── tools/            # LangChain4j Tool 定义
├── Frontend/             # 前端 SPA
│   ├── views/            # 页面（Dashboard、AIConsultant 等）
│   ├── router/           # Vue Router
│   ├── stores/           # Pinia 状态管理
│   └── api/              # Axios 请求封装
└── .github/workflows/    # CI/CD
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.5.14, Java 21 |
| ORM | MyBatis Plus 3.5.10 |
| AI | LangChain4j 1.0.1-beta6 + 通义千问 |
| 数据库 | MySQL 8.0, Redis 7 |
| 前端 | Vue 3.5, Vite 8, Element Plus 2.12, Pinia |

## 快速开始

### 后端

```bash
cd Backend

# 设置环境变量
export QWEN_API_KEY=sk-your-api-key

# 确保 MySQL 和 Redis 运行中，然后启动
./mvnw spring-boot:run
```

### 前端

```bash
cd Frontend
npm install
npm run dev
```

## CI/CD

通过 GitHub Actions 自动化。

| 工作流 | 触发分支 | 内容 |
|--------|----------|------|
| `develop.yml` | `develop`, `feature/CI_CD` | 编译 + 非 AI 单测 |
| `main.yml` | `main` | 编译 + 全部测试 + 打包 |

AI 测试（需要调用大模型）类名以 `AIIT` 结尾而非 `Test`，CI 中不会被 Surefire 自动扫描运行。本地运行：

```bash
mvn test -Dtest='*AIIT'
```

## feature/CI_CD 调试记录

### 问题 1: 编译错误

`ProfileMergeServiceImpl.java` 文件末尾缺少类闭合 `}`，导致 `reached end of file while parsing`，所有测试无法执行。

### 问题 2: Surefire 无法排除 AI 测试

AI 测试依赖大模型 API，CI 环境无有效 Key 必须跳过。先后尝试了五种方式：

| 方式 | 结果 |
|------|------|
| `-Dsurefire.excludes="**/*AITest.java"` | 不生效 |
| `-Dtest='!*AITest'` | 不生效 |
| pom.xml `<excludes>` | 不生效 |
| `@Tag("ai")` + `<excludedGroups>` | 不生效 |
| `@BeforeEach assumeTrue` 检查环境变量 | 被 dummy 值绕过 |

**最终方案**: 将 AI 测试类名从 `*AITest` 改为 `*AIIT`。Surefire 默认扫描 `*Test` 模式的类名，`AIIT` 不匹配任何模式，不会被自动运行。
