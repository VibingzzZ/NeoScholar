# ============================================================
# NeoScholar Docker 镜像
# 多阶段构建：前端 Vite 构建 → 后端 Spring Boot 打包 → 合并镜像
# ============================================================

# --- Stage 1: 前端构建 ---
FROM node:22-alpine AS frontend-builder
WORKDIR /app/frontend
COPY Frontend/package*.json ./
RUN npm ci
COPY Frontend/ ./
RUN npm run build && npm cache clean --force

# --- Stage 2: 后端构建（使用 Maven Wrapper，无需完整 Maven 镜像）---
FROM eclipse-temurin:21-jdk-alpine AS backend-builder
WORKDIR /app/backend
# 复制 Maven Wrapper 和 pom.xml（利用 Docker 层缓存）
COPY Backend/mvnw Backend/mvnw.cmd ./
COPY Backend/.mvn .mvn
COPY Backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY Backend/src ./src
RUN ./mvnw package -DskipTests -B \
    && rm -rf /root/.m2/repository

# --- Stage 3: 运行镜像 ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 安装 curl（用于健康检查）和 mysql-client（用于启动前等待 MySQL ready）
RUN apk add --no-cache curl mysql-client

# 创建非 root 用户（安全加固）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 复制后端 JAR（精确匹配，避免多个 JAR 导致 COPY 报错）
COPY --from=backend-builder /app/backend/target/Backend-*.jar app.jar

# 复制前端静态资源到 Spring Boot 静态资源目录
COPY --from=frontend-builder /app/frontend/dist /app/static

# 启动脚本：等待 MySQL 真正可用后再启动应用
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'echo "Waiting for MySQL to be ready..."' >> /app/entrypoint.sh && \
    echo 'until mysqladmin ping -h "${DB_HOST:-mysql}" -P "${DB_PORT:-3306}" -u "${DB_USER:-root}" -p"${DB_PASSWORD:-123456}" --ssl=0 --silent 2>/dev/null; do' >> /app/entrypoint.sh && \
    echo '  echo "MySQL not ready yet, retrying in 3s..."' >> /app/entrypoint.sh && \
    echo '  sleep 3' >> /app/entrypoint.sh && \
    echo 'done' >> /app/entrypoint.sh && \
    echo 'echo "MySQL is ready, starting application..."' >> /app/entrypoint.sh && \
    echo 'exec java -jar /app/app.jar' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

# 切换为非 root 用户
USER appuser

EXPOSE 8080

# 添加健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
