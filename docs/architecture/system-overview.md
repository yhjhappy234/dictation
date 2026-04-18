# 系统概述

## 项目定位

小学生听写助手是一个面向小学生语文听写练习的教育辅助系统，帮助学生进行词语听写训练，提高汉字书写能力。

## 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                    dictation (单体应用)                       │
├─────────────────────────────────────────────────────────────┤
│  Controller层                                                │
│  ├── AuthController        ← 认证管理                        │
│  ├── UserController        ← 用户管理                        │
│  ├── DictationTaskController ← 任务管理                      │
│  ├── DictationBatchController ← 批次管理                     │
│  ├── WordController        ← 词语管理                        │
│  ├── DifficultWordController ← 生词本                        │
│  ├── PresetContentController ← 预设内容                      │
│  ├── SuggestionController  ← 建议管理                        │
│  └── DictationRecordController ← 记录管理                    │
├─────────────────────────────────────────────────────────────┤
│  Service层                                                   │
│  ├── UserService            ← 用户服务                       │
│  ├── DictationTaskService   ← 任务服务                       │
│  ├── DictationBatchService  ← 批次服务                       │
│  ├── WordService            ← 词语服务                       │
│  ├── DifficultWordService   ← 生词服务                       │
│  ├── SuggestionService      ← 建议服务                       │
│  └── TaskRecordService      ← 记录服务                       │
├─────────────────────────────────────────────────────────────┤
│  Repository层 (Spring Data JPA)                              │
│  ├── UserRepository                                         │
│  ├── DictationTaskRepository                                │
│  ├── DictationBatchRepository                               │
│  ├── WordRepository                                         │
│  ├── DifficultWordRepository                                │
│  ├── SuggestionRepository                                   │
│  └── TaskRecordRepository                                   │
├─────────────────────────────────────────────────────────────┤
│  数据层 (SQLite)                                             │
│  └── dictation.db                                           │
└─────────────────────────────────────────────────────────────┘
```

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 21 | 使用虚拟线程 |
| Spring Boot | 4.0.5 | Web框架 |
| SQLite | 3.x | 嵌入式数据库 |
| Spring Data JPA | 3.x | 数据访问 |
| Thymeleaf | 3.x | 模板引擎 |
| Lombok | 1.18.x | 减少样板代码 |

## 模块职责

| 模块 | 职责 |
|------|------|
| 认证模块 | 用户登录、登出、Session管理 |
| 用户模块 | 用户CRUD、角色权限、头像密码管理 |
| 任务模块 | 听写任务创建、进度管理、状态控制 |
| 批次模块 | 听写批次管理、词语分组 |
| 词语模块 | 词语CRUD、拼音管理、状态更新 |
| 生词本 | 困难词语记录、错词追踪 |
| 预设内容 | 教材词语预设导入 |
| 建议模块 | 学习建议生成 |
| 记录模块 | 听写过程记录、结果统计 |