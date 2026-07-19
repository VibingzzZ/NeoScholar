package com.javaee.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 自动数据库迁移工具。
 * <p>
 * 在应用启动时检查并添加缺失的列/表，避免因 schema.sql 中
 * CREATE TABLE IF NOT EXISTS 无法给已存在的表新增列而导致的
 * "Unknown column" 错误。
 */
@Slf4j
@Component
@Order(1)
public class SchemaMigrationRunner implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection()) {
            log.info("开始数据库 schema 自动迁移...");

            // 1. student_profile 新增列
            addColumnIfNotExists(conn, "student_profile",
                    "is_active", "TINYINT(1) DEFAULT 0 COMMENT '是否活跃画像'");
            addColumnIfNotExists(conn, "student_profile",
                    "source", "VARCHAR(20) DEFAULT 'manual' COMMENT '画像来源'");
            addColumnIfNotExists(conn, "student_profile",
                    "summary", "VARCHAR(255) COMMENT 'AI画像摘要'");

            // 2. learning_paths 新增列
            addColumnIfNotExists(conn, "learning_paths",
                    "profile_id", "BIGINT COMMENT '关联画像ID'");

            // 3. 创建 profile_merge_history 表
            createTableIfNotExists(conn, "profile_merge_history",
                    "CREATE TABLE profile_merge_history (" +
                    "  id              BIGINT       AUTO_INCREMENT PRIMARY KEY," +
                    "  user_id         BIGINT       NOT NULL COMMENT '用户ID'," +
                    "  original_id     BIGINT       NOT NULL COMMENT '原始画像ID'," +
                    "  target_id       BIGINT       NOT NULL COMMENT '目标画像ID'," +
                    "  result_summary  VARCHAR(500) COMMENT '合并结果摘要'," +
                    "  status          VARCHAR(20)  DEFAULT 'success' COMMENT '状态'," +
                    "  merged_at       DATETIME     COMMENT '合并时间'" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            log.info("数据库 schema 自动迁移完成");
        } catch (Exception e) {
            log.error("数据库迁移失败: {}", e.getMessage(), e);
        }
    }

    private void addColumnIfNotExists(Connection conn, String table, String column, String definition) {
        try {
            if (!columnExists(conn, table, column)) {
                String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    log.info("已添加列: {}.{} ({})", table, column, definition);
                }
            } else {
                log.debug("列已存在，跳过: {}.{}", table, column);
            }
        } catch (Exception e) {
            log.warn("添加列失败 (表={}, 列={}): {}", table, column, e.getMessage());
        }
    }

    private void createTableIfNotExists(Connection conn, String tableName, String createSql) {
        try {
            if (!tableExists(conn, tableName)) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createSql);
                    log.info("已创建表: {}", tableName);
                }
            } else {
                log.debug("表已存在，跳过: {}", tableName);
            }
        } catch (Exception e) {
            log.warn("创建表失败 (表={}): {}", tableName, e.getMessage());
        }
    }

    private boolean columnExists(Connection conn, String table, String column) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        // MySQL: catalog=null, schema=当前DB, 实际上 catalog 填数据库名更可靠
        String catalog = conn.getCatalog();
        try (ResultSet rs = meta.getColumns(catalog, null, table, column)) {
            return rs.next();
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        String catalog = conn.getCatalog();
        try (ResultSet rs = meta.getTables(catalog, null, tableName, null)) {
            return rs.next();
        }
    }
}
