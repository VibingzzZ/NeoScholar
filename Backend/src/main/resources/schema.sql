-- CI测试环境数据库初始化脚本
-- 表结构定义基于实体类自动推导

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS student_profile (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    major_or_field  VARCHAR(255) COMMENT '专业或领域',
    learning_goal   TEXT         COMMENT '学习目标',
    knowledge_base  TEXT         COMMENT '知识库',
    cognitive_style VARCHAR(50)  COMMENT '思维风格',
    common_mistakes TEXT         COMMENT '常见错误(JSON)',
    interaction_preference VARCHAR(50) COMMENT '交互偏好',
    is_active       TINYINT(1)   DEFAULT 0 COMMENT '是否活跃画像(同一用户仅一条)',
    source          VARCHAR(20)  DEFAULT 'manual' COMMENT '画像来源: manual/ai_chat/merge',
    summary         VARCHAR(255) COMMENT 'AI生成的画像一句话摘要',
    update_at       DATETIME     COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS learning_paths (
    id                 BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT       NOT NULL COMMENT '用户ID',
    profile_id         BIGINT       COMMENT '关联画像ID',
    path_name          VARCHAR(255) COMMENT '路径名称',
    nodes_json         TEXT         COMMENT '路径节点(JSON)',
    current_node_index INT          DEFAULT 0 COMMENT '当前节点索引',
    status             INT          DEFAULT 0 COMMENT '状态: 0-进行中, 1-已完成',
    updated_at         DATETIME     COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS learning_resources (
    id                 BIGINT       AUTO_INCREMENT PRIMARY KEY,
    path_id            BIGINT       NOT NULL COMMENT '学习路径ID',
    node_index         INT          COMMENT '节点序号',
    title              VARCHAR(255) COMMENT '资源标题',
    content_text       TEXT         COMMENT '资源内容',
    file_path          VARCHAR(500) COMMENT '文件存储路径',
    resource_type      VARCHAR(50)  COMMENT '资源类型(ppt/quiz等)',
    generated_by_agent VARCHAR(50)  COMMENT '生成代理名称',
    created_at         DATETIME     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS activity_log (
    id            BIGINT  AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT  NOT NULL COMMENT '用户ID',
    activity_date DATE    NOT NULL COMMENT '活动日期',
    count         INT     DEFAULT 1 COMMENT '当日活动次数',
    UNIQUE KEY uk_user_date (user_id, activity_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_messages (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    role        VARCHAR(20)  COMMENT '角色(user/assistant)',
    content     TEXT         COMMENT '消息内容',
    metadata    TEXT         COMMENT '元数据(JSON)',
    created_at  DATETIME     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS profile_merge_history (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    original_id     BIGINT       NOT NULL COMMENT '原始画像ID',
    target_id       BIGINT       NOT NULL COMMENT '目标画像ID',
    result_summary  VARCHAR(500) COMMENT '合并结果摘要',
    status          VARCHAR(20)  DEFAULT 'success' COMMENT '合并状态: success/fail',
    merged_at       DATETIME     COMMENT '合并时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;