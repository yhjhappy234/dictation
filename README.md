# 小学生听写助手

一个帮助小学生进行听写练习的后端服务系统。

## 功能特性

### 核心功能
- 听写任务管理：创建、编辑、删除听写任务
- 听写进度追踪：记录听写进度和结果
- 生词本管理：自动收集错误词语，支持复习功能
- 历史记录：查看历史听写记录和统计报表
- 语音朗读：使用 Web Speech API 进行词语朗读

### 用户认证功能（新增）
- **登录认证**：所有操作需要登录后才能进行
- **用户角色**：支持管理员和普通用户两种角色
- **用户管理**：管理员可以添加、删除、修改用户
- **密码修改**：普通用户可以修改自己的密码
- **头像系统**：20个内置头像，用户可以更换头像
- **Session 管理**：登录有效期2小时

### 审计日志功能（新增）
- **操作记录**：记录所有关键操作行为
- **AOP异步处理**：使用注解方式，不阻塞主流程
- **审计信息**：包含用户、操作类型、时间、IP等信息

## 技术栈

- **Java 21**
- **Spring Boot 4.0.5**
- **SQLite** (生产环境)
- **H2** (测试环境)
- **JPA/Hibernate**
- **Thymeleaf** (前端模板)
- **Bootstrap 5** (UI框架)
- **Spring Security** (密码加密)

## 快速开始

### 环境要求
- Java 21+
- Maven 3.6+

### 运行步骤

```bash
# 克隆项目
git clone <repository-url>
cd dictation

# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

应用启动后访问 http://localhost:8080

### 默认用户

首次启动时会自动创建默认管理员用户：
- 用户名和密码可通过环境变量设置
- 设置环境变量：`APP_DEFAULT_USERNAME` 和 `APP_DEFAULT_PASSWORD`

## API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/current` - 获取当前用户信息
- `GET /api/auth/avatars` - 获取头像列表

### 用户管理接口（管理员）
- `GET /api/users` - 获取所有用户
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

### 用户自助接口
- `POST /api/users/me/password` - 修改密码
- `POST /api/users/me/avatar` - 修改头像
- `GET /api/users/me/is-admin` - 检查是否是管理员

### 听写任务接口
- `GET /api/tasks` - 获取所有任务
- `POST /api/tasks` - 创建任务
- `PUT /api/tasks/{id}` - 更新任务
- `DELETE /api/tasks/{id}` - 删除任务
- `POST /api/tasks/{id}/dictation` - 开始听写

### 词语接口
- `GET /api/words/{id}` - 获取词语
- `PUT /api/words/{id}/status` - 更新状态
- `POST /api/words/{id}/complete` - 标记完成

### 生词本接口
- `GET /api/difficult-words` - 获取生词列表
- `POST /api/difficult-words` - 添加生词
- `DELETE /api/difficult-words/{id}` - 移除生词

## 测试

```bash
# 运行测试
mvn test

# 生成覆盖率报告
mvn test jacoco:report
```

覆盖率报告位于 `target/site/jacoco/index.html`

## 目录结构

```
src/
├── main/
│   ├── java/com/yhj/dictation/
│   │   ├── annotation/     # 自定义注解
│   │   ├── aspect/         # AOP切面
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── entity/         # 实体类
│   │   ├── interceptor/    # 拦截器
│   │   ├── repository/     # 数据访问层
│   │   ├── service/        # 服务层
│   │   └── util/           # 工具类
│   └── resources/
│       ├── templates/      # Thymeleaf模板
│       ├── static/         # 静态资源
│       └── application.yml # 配置文件
└── test/
    └── java/com/yhj/dictation/
        ├── controller/     # 控制器测试
        ├── service/        # 服务测试
        └── interceptor/    # 拦截器测试
```

## 审计日志注解使用

在需要审计的方法上添加 `@AuditLog` 注解：

```java
@PostMapping("/api/users")
@AuditLog(operation = "创建用户", level = AuditLog.LogLevel.IMPORTANT, recordParams = true)
public ApiResponse<UserInfoDTO> createUser(@RequestBody UserCreateRequest request) {
    // ...
}
```

注解参数：
- `operation`：操作描述
- `level`：日志级别（NORMAL, IMPORTANT, SENSITIVE）
- `recordParams`：是否记录参数
- `recordResult`：是否记录结果

## License

MIT License