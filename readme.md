# 小学生听写助手 Web应用

一款专为小学生设计的智能听写辅助Web应用，通过语音交互帮助学生完成词语听写练习，记录学习数据，生成学习报告，提高学习效率。

## 项目结构

```
dictation/
├── prd.md                    # 产品需求文档
├── readme.md                 # 项目说明文档
├── pom.xml                   # Maven配置文件
├── src/
│   ├── main/
│   │   ├── java/com/yhj/dictation/
│   │   │   ├── DictationApplication.java    # 主启动类
│   │   │   ├── config/                      # 配置类
│   │   │   │   ├── CorsConfig.java          # 跨域配置
│   │   │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   │   │   └── JpaConfig.java           # JPA配置
│   │   │   ├── controller/                  # 控制器
│   │   │   │   ├── DictationBatchController.java
│   │   │   │   ├── WordController.java
│   │   │   │   ├── DictationRecordController.java
│   │   │   │   ├── DifficultWordController.java
│   │   │   │   ├── SuggestionController.java
│   │   │   │   └── PageController.java       # 页面控制器
│   │   │   ├── dto/                         # 数据传输对象
│   │   │   ├── entity/                      # JPA实体类
│   │   │   │   ├── DictationBatch.java      # 听写批次
│   │   │   │   ├── Word.java                # 词语
│   │   │   │   ├── DictationRecord.java     # 听写记录
│   │   │   │   ├── DifficultWord.java       # 生词本
│   │   │   │   └── Suggestion.java          # 学习建议
│   │   │   ├── repository/                  # JPA Repository接口
│   │   │   │   ├── DictationBatchRepository.java
│   │   │   │   ├── WordRepository.java
│   │   │   │   ├── DictationRecordRepository.java
│   │   │   │   ├── DifficultWordRepository.java
│   │   │   │   └── SuggestionRepository.java
│   │   │   └── service/                     # 业务服务层
│   │   │       ├── DictationBatchService.java
│   │   │       ├── WordService.java
│   │   │       ├── DictationRecordService.java
│   │   │       ├── DifficultWordService.java
│   │   │       └── SuggestionService.java
│   │   └── resources/
│   │       ├── application.yml              # 应用配置
│   │       ├── templates/                   # Thymeleaf模板
│   │       │   ├── index.html               # 主页面（听写）
│   │       │   ├── history.html             # 历史记录页面
│   │       │   ├── difficult-words.html     # 生词本页面
│   │       │   └── reports.html             # 学习报表页面
│   │       └── static/                      # 静态资源
│   │           ├── css/
│   │           │   └── style.css            # 样式文件
│   │           └── js/
│   │               ├── app.js               # 主应用逻辑
│   │               ├── speech.js            # 语音播报/识别
│   │               └── api.js               # API调用封装
│   └── test/                                # 测试目录
└── target/                                  # 编译输出目录
```

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.2.5
- **Java版本**: JDK 21
- **数据库**: SQLite 3
- **ORM**: Spring Data JPA (Hibernate)
- **模板引擎**: Thymeleaf 3.1
- **构建工具**: Maven 3.11+

### 前端技术栈
- **模板引擎**: Thymeleaf (服务端渲染)
- **JavaScript**: 原生 JavaScript (ES6+)
- **样式**: CSS3
- **图表库**: Chart.js 4.4

### 语音交互
- **语音播报**: Web Speech API - SpeechSynthesis
- **语音识别**: Web Speech API - SpeechRecognition

## 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot 单体应用                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Thymeleaf 模板层                       │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │  │
│  │  │  HTML页面   │  │  Web Speech │  │   原生JS    │      │  │
│  │  │  (Thymeleaf)│  │     API     │  │   交互逻辑  │      │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Controller 层                          │  │
│  │  ┌─────────────────┐  ┌─────────────────────────────┐   │  │
│  │  │  PageController │  │    REST API Controllers     │   │  │
│  │  │   (页面路由)    │  │  (Batch/Word/Record/...)   │   │  │
│  │  └─────────────────┘  └─────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Service 业务层                         │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │  │
│  │  │ BatchService│  │ WordService │  │RecordService│      │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  Repository 数据层                        │  │
│  │  ┌─────────────────────────────────────────────────┐     │  │
│  │  │        Spring Data JPA (Hibernate)             │     │  │
│  │  └─────────────────────────────────────────────────┘     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   SQLite Database                        │  │
│  │  ┌─────────────────────────────────────────────┐        │  │
│  │  │ dictation_batch │ word │ dictation_record  │        │  │
│  │  │ difficult_word │ suggestion              │        │  │
│  │  └─────────────────────────────────────────────┘        │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

单体架构优势：
  - 简化部署：一个应用包含所有功能，一次启动即可运行
  - 统一管理：前后端代码在同一项目中，便于维护
  - 减少复杂度：无需前后端分离的额外配置和依赖
  - 开发效率高：Thymeleaf模板热更新，快速迭代
```

## 核心功能

### 1. 词语录入
- 支持空格分隔批量输入词语
- 自动创建听写批次
- 词语状态管理（待听写/进行中/已完成）

### 2. 语音听写流程
```
开始听写 → 播放词语(语音) → 停顿 → 开启麦克风监听
    ↓
监听结果判断：
  ├─ 识别到"好了/下一个" → 播放下一个词语
  ├─ 超时5秒无响应 → 提示"下一个听写词语：**"
  └─ 所有词语完成 → 播放"所有听写均已完成"
```

### 3. 控制按钮
- **再次读取**: 重复播放当前词语
- **上一个**: 返回上一个词语
- **下一个**: 跳到下一个词语
- **开始听写**: 启动听写流程

### 4. 学习统计
- 实时统计：当前词语、已完成数、剩余数
- 单词语耗时记录
- 批次总耗时统计

### 5. 生词本
- 自动识别困难词语（重复播放次数多/耗时长）
- 手动添加生词
- 掌握度星级评分

### 6. 学习报表
- 日报表：当日听写词语数、平均耗时
- 周报表：本周累计、易错词语Top10
- 月报表：月度趋势图表

## API接口文档

### 批次管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/batches` | 创建新批次 |
| GET | `/api/batches` | 获取批次列表 |
| GET | `/api/batches/{id}` | 获取批次详情 |
| DELETE | `/api/batches/{id}` | 删除批次 |

### 词语管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/words` | 添加词语到批次 |
| GET | `/api/words/batch/{id}` | 获取批次词语列表 |
| PUT | `/api/words/{id}/status` | 更新词语状态 |

### 听写操作
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/records/start/{batchId}` | 开始听写 |
| POST | `/api/records/next/{batchId}` | 下一个词语 |
| POST | `/api/records/previous/{batchId}` | 上一个词语 |
| POST | `/api/records/complete/{wordId}` | 完成词语听写 |
| POST | `/api/records/end/{batchId}` | 结束听写 |

### 生词本管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/difficult-words` | 获取生词本列表 |
| POST | `/api/difficult-words` | 添加生词 |
| DELETE | `/api/difficult-words/{id}` | 移除生词 |

### 报表统计
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/reports/daily` | 日报表 |
| GET | `/api/reports/weekly` | 周报表 |
| GET | `/api/reports/monthly` | 月报表 |

### 学习建议
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/suggestions` | 获取听写建议 |

## 运行指南

### 环境要求
- JDK 21+
- Maven 3.11+

### 启动应用
```bash
# 编译项目
mvn clean install

# 启动Spring Boot应用
mvn spring-boot:run

# 或直接运行jar包
java --enable-preview -jar target/dictation-1.0.0.jar
```

应用启动后访问 `http://localhost:8080`

### 开发模式
Thymeleaf模板支持热更新，修改HTML模板后刷新浏览器即可看到效果。

### 生产部署
```bash
# 打包应用
mvn clean package -DskipTests

# 生成的jar包在 target/ 目录
# 包含内嵌Tomcat服务器，直接运行即可
java --enable-preview -jar target/dictation-1.0.0.jar
```

## 数据库设计

### 实体关系图
```
┌─────────────────┐     ┌─────────────────┐
│ dictation_batch │────▶│      word       │
│                 │ 1:N │                 │
│ - id            │     │ - id            │
│ - batch_name    │     │ - word_text     │
│ - total_words   │     │ - batch_id (FK) │
│ - completed     │     │ - sort_order    │
│ - status        │     │ - status        │
│ - created_at    │     │ - created_at    │
└─────────────────┘     └─────────────────┘
                              │
                              │ 1:N
                              ▼
        ┌─────────────────┐     ┌─────────────────┐
        │ dictation_record│     │ difficult_word  │
        │                 │     │                 │
        │ - id            │     │ - id            │
        │ - word_id (FK)  │     │ - word_id (FK)  │
        │ - batch_id (FK) │     │ - error_count   │
        │ - start_time    │     │ - avg_duration  │
        │ - end_time      │     │ - mastery_level │
        │ - duration      │     │ - last_practice │
        │ - repeat_count  │     └─────────────────┘
        └─────────────────┘
```

## 特色功能

### 语音交互优化
- 慢速语音播放（rate=0.8），适合小学生理解
- 中文语音识别，支持关键词检测
- 5秒超时自动提示机制

### 响应式设计
- 支持PC端和移动端访问
- 大按钮、大字体设计，方便儿童操作
- 鲜艳活泼的色彩主题

### 学习数据分析
- 基于历史数据生成学习建议
- 自动识别薄弱词语
- 学习进度可视化

## 浏览器兼容性

| 浏览器 | 版本 | 语音播报 | 语音识别 |
|--------|------|----------|----------|
| Chrome | 90+ | ✓ | ✓ |
| Edge | 90+ | ✓ | ✓ |
| Safari | 14+ | ✓ | 部分 |
| Firefox | 90+ | ✓ | ✗ |

**推荐使用 Chrome 浏览器获得最佳体验**

## 开发团队

基于 Claude Code multi-agent 模式开发

## 版本历史

- v1.0.0 (2026-04-06): 初始版本发布
  - 基础听写功能
  - 语音播报和识别
  - 历史记录和生词本
  - 学习报表

## 许可证

MIT License