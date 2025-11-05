use ai_code;
ALTER TABLE chat_history
    MODIFY COLUMN message LONGTEXT COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息';