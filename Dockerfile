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
RUN npm run build

# --- Stage 2: 后端构建 ---
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app/backend
COPY Backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY Backend/src ./src
RUN mvn package -DskipTests -B

# --- Stage 3: 运行镜像 ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 复制后端 JAR
COPY --from=backend-builder /app/backend/target/*.jar app.jar

# 复制前端静态资源到 Spring Boot 静态资源目录
COPY --from=frontend-builder /app/frontend/dist /app/static

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
