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
│   │   │   ├── annotation/                  # 自定义注解
│   │   │   │   └── AuditLog.java            # 审计日志注解
│   │   │   ├── aspect/                      # AOP切面
│   │   │   │   └── AuditAspect.java         # 审计日志切面
│   │   │   ├── config/                      # 配置类
│   │   │   │   ├── AsyncConfig.java         # 异步配置
│   │   │   │   ├── CorsConfig.java          # 跨域配置
│   │   │   │   ├── DataInitializer.java     # 数据初始化
│   │   │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   │   │   ├── JpaConfig.java           # JPA配置
│   │   │   │   ├── SecurityConfig.java      # 安全配置
│   │   │   │   └── WebConfig.java           # Web配置（拦截器）
│   │   │   ├── controller/                  # 控制器
│   │   │   │   ├── AuthController.java      # 认证控制器
│   │   │   │   ├── UserController.java      # 用户管理控制器
│   │   │   │   ├── DictationBatchController.java
│   │   │   │   ├── DictationTaskController.java  # 听写任务管理
│   │   │   │   ├── DictationRecordController.java
│   │   │   │   ├── DifficultWordController.java  # 生词本管理
│   │   │   │   ├── WordController.java
│   │   │   │   ├── SuggestionController.java
│   │   │   │   ├── PresetContentController.java  # 预设内容导入
│   │   │   │   └── PageController.java       # 页面控制器
│   │   │   ├── dto/                         # 数据传输对象
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── LoginRequest.java        # 登录请求
│   │   │   │   ├── UserInfoDTO.java         # 用户信息
│   │   │   │   ├── UserCreateRequest.java   # 创建用户请求
│   │   │   │   ├── PasswordUpdateRequest.java # 密码更新请求
│   │   │   │   ├── TaskDTO.java
│   │   │   │   ├── TaskProgressRequest.java
│   │   │   │   ├── DifficultWordDTO.java
│   │   │   │   └── ... (其他DTO)
│   │   │   ├── entity/                      # JPA实体类
│   │   │   │   ├── User.java                # 用户实体
│   │   │   │   ├── AuditLogEntity.java      # 审计日志实体
│   │   │   │   ├── DictationTask.java       # 听写任务实体
│   │   │   │   ├── TaskRecord.java          # 任务听写记录
│   │   │   │   ├── DifficultWord.java       # 生词实体
│   │   │   │   └── ... (其他实体)
│   │   │   ├── interceptor/                 # 拦截器
│   │   │   │   └── AuthInterceptor.java     # 认证拦截器
│   │   │   ├── repository/                  # JPA Repository接口
│   │   │   │   ├── UserRepository.java      # 用户Repository
│   │   │   │   ├── AuditLogEntityRepository.java # 审计日志Repository
│   │   │   │   ├── DictationTaskRepository.java
│   │   │   │   ├── TaskRecordRepository.java
│   │   │   │   ├── DifficultWordRepository.java
│   │   │   │   └── ... (其他Repository)
│   │   │   ├── service/                     # 业务服务层
│   │   │   │   ├── UserService.java         # 用户服务
│   │   │   │   ├── AuditLogService.java     # 审计日志服务
│   │   │   │   ├── DictationTaskService.java
│   │   │   │   ├── TaskRecordService.java
│   │   │   │   ├── DifficultWordService.java
│   │   │   │   └── ... (其他Service)
│   │   │   └── util/                        # 工具类
│   │   │       └── UserContext.java         # 用户上下文
│   │   └── resources/
│   │       ├── application.yml              # 应用配置
│   │       ├── preset-content/              # 预设听写内容
│   │       │   ├── common-words-50.json     # 常用词语分类（15个分类）
│   │       │   ├── common-idioms-50.json    # 常用成语分类（8个分类）
│   │       │   ├── common-poems-20.json     # 常用古诗分类（8个分类）
│   │       │   ├── classics-5.json          # 常用古文分类（6个分类）
│   │       │   ├── grade1-up.json           # 一年级上册词语
│   │       │   ├── grade1-down.json         # 一年级下册词语
│   │       │   ├── grade2-up.json           # 二年级上册词语
│   │       │   ├── grade2-down.json         # 二年级下册词语
│   │       │   ├── grade3-up.json           # 三年级上册词语
│   │       │   ├── grade3-down.json         # 三年级下册词语
│   │       │   ├── grade4-up.json           # 四年级上册词语
│   │       │   ├── grade4-down.json         # 四年级下册词语
│   │       │   ├── grade5-up.json           # 五年级上册词语
│   │       │   ├── grade5-down.json         # 五年级下册词语
│   │       │   ├── grade6-up.json           # 六年级上册词语
│   │       │   ├── grade6-down.json         # 六年级下册词语
│   │       │   ├── grade7-up.json           # 七年级上册词语
│   │       │   ├── grade7-down.json         # 七年级下册词语
│   │       │   ├── grade8-up.json           # 八年级上册词语
│   │       │   ├── grade8-down.json         # 八年级下册词语
│   │       │   ├── grade9-up.json           # 九年级上册词语
│   │       │   ├── grade9-down.json         # 九年级下册词语
│   │       │   ├── grade10-up.json          # 高一上册词语
│   │       │   ├── grade10-down.json        # 高一下册词语
│   │       │   ├── grade11-up.json          # 高二上册词语
│   │       │   ├── grade11-down.json        # 高二下册词语
│   │       │   ├── grade12-up.json          # 高三上册词语
│   │       │   └── grade12-down.json        # 高三下册词语
│   │       ├── templates/                   # Thymeleaf模板
│   │       │   ├── login.html               # 登录页面
│   │       │   ├── user-management.html     # 用户管理页面
│   │       │   ├── index.html               # 首页（听写页面）
│   │       │   ├── tasks.html               # 任务管理页面
│   │       │   ├── history.html             # 听写历史页面
│   │       │   ├── difficult-words.html     # 生词本页面
│   │       │   ├── reports.html             # 报表页面
│   │       │   ├── dictators.html           # 听写人管理页面
│   │       │   └── layout.html              # 布局模板
│   │       └── static/                      # 静态资源
│   │           └── images/avatars/          # 用户头像
│   └── test/                                # 测试目录
└── target/                                  # 编译输出目录
```

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 4.0.5
- **Java版本**: JDK 21
- **数据库**: SQLite 3
- **ORM**: Spring Data JPA (Hibernate 6.6.11)
- **模板引擎**: Thymeleaf 3.1
- **安全**: Spring Security (密码加密)
- **JSON处理**: Jackson (com.fasterxml.jackson)
- **构建工具**: Maven 3.11+

### 前端技术栈
- **模板引擎**: Thymeleaf (服务端渲染)
- **JavaScript**: 原生 JavaScript (ES6+)
- **样式**: CSS3 + Bootstrap 5
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
│  │              拦截器层 (AuthInterceptor)                   │  │
│  │  ┌─────────────────────────────────────────────────┐     │  │
│  │  │  登录认证拦截 - 保护所有API和页面请求            │     │  │
│  │  └─────────────────────────────────────────────────┘     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Controller 层                          │  │
│  │  ┌─────────────────┐  ┌─────────────────────────────┐   │  │
│  │  │  PageController │  │    REST API Controllers     │   │  │
│  │  │   (页面路由)    │  │  (Auth/User/Batch/Word...) │   │  │
│  │  └─────────────────┘  └─────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                AOP切面 (AuditAspect)                      │  │
│  │  ┌─────────────────────────────────────────────────┐     │  │
│  │  │  @AuditLog注解 - 异步记录操作日志               │     │  │
│  │  └─────────────────────────────────────────────────┘     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Service 业务层                         │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │  │
│  │  │ UserService │  │ BatchService│  │ WordService │      │  │
│  │  │AuditService │  │RecordService│  │ ...更多...  │      │  │
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
│  │  │ user | audit_log | dictation_batch | word   │        │  │
│  │  │ dictation_task | task_record | difficult_word│       │  │
│  │  └─────────────────────────────────────────────┘        │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 核心功能

### 用户认证功能
- **登录认证**：所有操作需要登录后才能进行
- **用户角色**：支持管理员(ADMIN)和普通用户(USER)两种角色
- **用户管理**：管理员可以添加、删除、修改用户
- **密码管理**：使用BCrypt加密，普通用户可修改自己的密码
- **头像系统**：20个内置头像，用户可以更换头像
- **Session管理**：登录有效期2小时

### 审计日志功能
- **操作记录**：记录所有关键操作行为
- **AOP异步处理**：使用@AuditLog注解，不阻塞主流程
- **审计信息**：包含用户、操作类型、时间、IP地址、执行时长等

### 1. 听写任务管理
- 提前创建听写任务，保存词语列表
- 任务状态管理：未开始、进行中、已完成
- 收藏功能：标记重要任务
- 首页只显示未完成的任务供选择

### 2. 词语录入
- 支持空格分隔批量输入词语
- 自动创建听写批次
- 词语状态管理（待听写/进行中/已完成）

### 3. 语音听写流程
```
开始听写 → 播放词语(语音) → 停顿 → 开启麦克风监听
    ↓
监听结果判断：
  ├─ 识别到"好了/下一个" → 播放下一个词语
  ├─ 超时5秒无响应 → 提示"下一个听写词语：**"
  └─ 所有词语完成 → 播放"所有听写均已完成"
```

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

### 预设内容导入
- 一键导入预设内容，支持多级分类选择：
  - **教材类**：小学/初中/高中语文教科书（按年级、单元分类）
  - **常用词语**：15个分类（水果、天气、颜色、动物、蔬菜、食物等）
  - **成语**：8个分类（数字成语、自然景色、人物描写、学习勤奋等）
  - **古诗**：8个分类（春季、夏季、秋季、山水、咏物、情感等）
  - **古文**：6个分类（修身篇、学习篇、自然篇、道德篇等）
- 快速开始试听听写，无需手动输入

### 自动听写功能
- 定时自动执行下一个词语
- 可设置自动间隔（1-60秒）
- 快捷键A启动/停止自动听写
- 实时统计显示：耗时、正确/错误数、进度百分比

## API接口文档

### 认证接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/logout` | 用户登出 |
| GET | `/api/auth/current` | 获取当前用户信息 |
| GET | `/api/auth/status` | 检查登录状态 |
| GET | `/api/auth/avatars` | 获取头像列表 |

### 用户管理接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/users` | 获取所有用户（管理员） |
| POST | `/api/users` | 创建用户（管理员） |
| PUT | `/api/users/{id}` | 更新用户（管理员） |
| DELETE | `/api/users/{id}` | 删除用户（管理员） |
| POST | `/api/users/me/password` | 修改密码（所有用户） |
| POST | `/api/users/me/avatar` | 修改头像（所有用户） |
| GET | `/api/users/me/is-admin` | 检查是否管理员 |

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

### 听写任务管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/tasks` | 创建听写任务 |
| GET | `/api/tasks` | 获取所有任务列表 |
| GET | `/api/tasks/uncompleted` | 获取未完成的任务（首页下拉） |
| GET | `/api/tasks/{id}` | 获取任务详情 |
| PUT | `/api/tasks/{id}` | 更新任务 |
| DELETE | `/api/tasks/{id}` | 删除任务 |
| PUT | `/api/tasks/{id}/status` | 更新任务状态 |
| POST | `/api/tasks/{id}/start` | 开始任务 |
| POST | `/api/tasks/{id}/complete` | 完成任务 |
| POST | `/api/tasks/{id}/reset` | 重置任务 |
| POST | `/api/tasks/{id}/dictation` | 从任务开始听写 |
| POST | `/api/tasks/{id}/favorite` | 设置/取消收藏 |

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

### 默认用户配置
首次启动时会自动创建默认管理员用户。用户名和密码可通过环境变量设置：
- `APP_DEFAULT_USERNAME`: 用户名
- `APP_DEFAULT_PASSWORD`: 密码

### 测试
```bash
# 运行测试
mvn test

# 生成覆盖率报告
mvn test jacoco:report
```

覆盖率报告位于 `target/site/jacoco/index.html`

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

YHJ-TECH 结合 AI Coding 模式开发

## 版本历史

- v1.0.0 (2026-04-06): 初始版本发布
  - 基础听写功能
  - 语音播报和识别
  - 历史记录和生词本
  - 学习报表

- v1.1.0 (2026-04-12): 功能更新
  - 项目结构优化，去除backend目录层
  - Spring Boot升级至3.4.4
  - Hibernate升级至6.6.11
  - 新增预设内容导入功能（常用词/成语/古诗/古文）
  - 修复听写记录数据持久化问题
  - 数据库文件移至resources目录

- v1.2.0 (2026-04-12): 框架升级
  - Spring Boot升级至4.0.5（最新稳定版）
  - Hibernate升级至6.6.11
  - 测试框架适配Spring Boot 4.0
  - 230个单元测试全部通过
  - 从git中移除编译产物（target/目录）

- v1.3.0 (2026-04-12): 任务管理模块
  - 新增听写任务实体，支持任务状态管理
  - 任务状态：未开始、进行中、已完成
  - 新增任务管理页面，可编辑/删除/修改状态
  - 首页重构：只能选择未完成任务进行听写
  - 预设内容导入填充到新任务输入区
  - 收藏功能支持

- v1.4.0 (2026-04-13): 用户认证与审计日志
  - 用户认证模块：登录/登出、Session管理（2小时有效期）
  - 用户角色：管理员和普通用户
  - 用户管理：管理员可CRUD用户
  - 头像系统：20个内置头像，用户可更换
  - 审计日志模块：@AuditLog注解、AOP异步处理
  - 单元测试：新增认证和审计相关测试
  - 密码加密：BCrypt加密存储

- v1.5.0 (2026-04-16): Spring Boot 4.0.5适配与测试完善
  - 修复Spring Boot 4.0.5兼容性问题：
    - `spring-boot-starter-aop` → `spring-boot-starter-aspectj`
    - 添加显式`ObjectMapper` bean配置
    - 修复SecurityConfig session管理配置
  - 新增批次API端点：`/api/batches/today`、`/api/batches/range`
  - E2E测试完善：108个测试全部通过
  - 单元测试覆盖率：90%以上（行覆盖率和分支覆盖率）
  - 时区配置：默认东八区（GMT+8）
  - 测试数据自动清理机制

- v1.5.1 (2026-04-17): 预设内容导入功能修复
  - 修复导入预设内容时文字未填充到输入框的问题
  - 导入前自动打开创建任务面板
  - 添加数据类型检查确保格式正确
  - 改进提示语句，明确告知下一步操作

- v1.6.0 (2026-04-18): 自动听写与教材扩展
  - 自动听写功能：定时自动执行，可设置间隔(1-60秒)，快捷键A
  - 实时统计显示：耗时、正确/错误数、进度百分比
  - 预设内容分类优化：
    - 常用词语15个分类（水果、天气、颜色等）
    - 成语8个分类（数字成语、自然景色等）
    - 古诗8个分类（春季、夏季、山水等）
    - 古文6个分类（修身篇、学习篇等）
  - 教材扩展：初中(七-九年级)、高中(高一-高三)
  - Flyway数据库版本管理
  - 任务管理优化：进行中任务显示进度

## 许可证

MIT License