# 开发环境搭建指南

## 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 21+ |
| Maven | 3.9.x |
| Python | 3.12+ (E2E测试) |

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yhjhappy234/dictation.git
cd dictation
```

### 2. 编译项目

```bash
mvn compile
```

### 3. 运行项目

```bash
mvn spring-boot:run
```

访问 http://localhost:8080

### 4. 运行测试

```bash
# 单元测试
mvn test

# 集成测试 + 覆盖率检查
mvn verify
```

### 5. E2E测试

```bash
cd scripts
pip install -r requirements.txt
BASE_URL=http://localhost:8080 pytest e2e_test.py -v
```

## 默认账户

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | ADMIN |

## 项目结构

```
dictation/
├── src/main/java/com/yhj/dictation/
│   ├── controller/         ← REST API控制器
│   ├── service/            ← 业务逻辑层
│   ├── repository/         ← 数据访问层
│   ├── entity/             ← 实体类
│   ├── dto/                ← 数据传输对象
│   ├── config/             ← 配置类
│   ├── annotation/         ← 自定义注解
│   ├── aspect/             ← AOP切面
│   ├── interceptor/        ← 拦截器
│   └── util/               ← 工具类
├── src/main/resources/
│   ├── templates/          ← Thymeleaf模板
│   ├── static/             ← 静态资源
│   ├── preset-content/     ← 预设内容JSON
│   └── dictation.db        ← SQLite数据库
├── src/test/               ← 测试代码
├── scripts/                ← E2E测试脚本
└── docs/                   ← 文档目录
```

## 常用命令

```bash
# 清理构建
mvn clean

# 打包
mvn package -DskipTests

# 查看覆盖率报告
mvn jacoco:report
# 报告位置: target/site/jacoco/index.html
```