# 数据库设计

## 数据库概述

使用 SQLite 嵌入式数据库，文件位于 `src/main/resources/dictation.db`。

## 表结构

### user 表 - 用户信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| username | VARCHAR(255) | 用户名（唯一） |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| role | VARCHAR(255) | 角色（ADMIN/USER） |
| status | VARCHAR(255) | 状态（ACTIVE/DISABLED） |
| avatar | VARCHAR(255) | 头像文件名 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### dictation_task 表 - 听写任务

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| task_name | VARCHAR(255) | 任务名称 |
| words | VARCHAR(255) | 词语列表（逗号分隔） |
| word_count | INTEGER | 词语数量 |
| status | VARCHAR(20) | 状态（NOT_STARTED/IN_PROGRESS/COMPLETED） |
| current_index | INTEGER | 当前词语索引 |
| correct_count | INTEGER | 正确数量 |
| wrong_count | INTEGER | 错误数量 |
| dictator | VARCHAR(50) | 听写人 |
| is_favorite | BOOLEAN | 是否收藏 |
| created_at | TIMESTAMP | 创建时间 |

### dictation_batch 表 - 听写批次

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| batch_name | VARCHAR(255) | 批次名称 |
| total_words | INTEGER | 总词语数 |
| completed_words | INTEGER | 已完成词语数 |
| status | VARCHAR(255) | 状态 |
| created_at | TIMESTAMP | 创建时间 |

### word 表 - 词语

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| word_text | VARCHAR(255) | 词语文本 |
| pinyin | VARCHAR(255) | 拼音 |
| sort_order | INTEGER | 排序 |
| status | VARCHAR(255) | 状态 |
| batch_id | INTEGER | 所属批次ID |
| created_at | TIMESTAMP | 创建时间 |

### difficult_word 表 - 生词本

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| word_text | TEXT | 词语文本 |
| error_count | INTEGER | 错误次数 |
| dictator | TEXT | 听写人 |
| avg_duration_seconds | INTEGER | 平均听写时间 |
| mastery_level | INTEGER | 掌握程度 |
| last_practice_date | TIMESTAMP | 最后练习时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### task_record 表 - 任务记录

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| task_id | INTEGER | 任务ID |
| word | VARCHAR(255) | 词语 |
| is_correct | BOOLEAN | 是否正确 |
| error_count | INTEGER | 错误次数 |
| dictator | VARCHAR(50) | 听写人 |
| start_time | TIMESTAMP | 开始时间 |
| end_time | TIMESTAMP | 结束时间 |
| read_count | INTEGER | 朗读次数 |
| created_at | TIMESTAMP | 创建时间 |

### suggestion 表 - 建议

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| word_id | INTEGER | 词语ID |
| suggestion_type | VARCHAR(255) | 建议类型 |
| message | VARCHAR(255) | 建议内容 |
| priority | INTEGER | 优先级 |
| created_at | TIMESTAMP | 创建时间 |

### audit_log 表 - 审计日志

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| user_id | INTEGER | 用户ID |
| username | VARCHAR(255) | 用户名 |
| operation | VARCHAR(255) | 操作类型 |
| params | VARCHAR(2000) | 操作参数 |
| result | VARCHAR(2000) | 操作结果 |
| success | BOOLEAN | 是否成功 |
| error_message | VARCHAR(1000) | 错误信息 |
| ip_address | VARCHAR(255) | IP地址 |
| duration_ms | INTEGER | 耗时 |
| timestamp | TIMESTAMP | 时间戳 |