# 听写小助手

一个帮助小学生进行词语听写练习的 Web 应用。

## 开发团队

YHJ-TECH 结合 AI Coding 模式开发

## 功能特性

### 听写功能
- 支持创建听写任务，自定义词语列表
- 语音朗读词语，可调节朗读速度和音色
- 键盘快捷键支持（↑再次朗读、←上一个、→下一个、↓显示答案）
- 实时记录听写结果（正确/错误）
- 支持中途暂停和继续听写

### 生词本
- 自动收集听写错误的词语
- 记录错误次数和听写人信息
- 支持掌握度评级（1-5星）
- 针对性练习功能

### 任务管理
- 任务状态管理（未开始/进行中/已完成）
- 任务收藏功能
- 进度保存和恢复

### 数据统计
- 听写历史记录
- 正确率统计
- 耗时分析
- 听写人统计报表

## 技术栈

- **后端**: Spring Boot 3.x + Java 17
- **前端**: Thymeleaf + Bootstrap 5
- **数据库**: H2 Database（开发）/ MySQL（生产）
- **语音**: Web Speech API

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+

### 运行项目

```bash
# 克隆项目
git clone https://github.com/yhjhappy234/dictation.git

# 进入项目目录
cd dictation

# 构建并运行
mvn spring-boot:run
```

访问 http://localhost:8080 开始使用。

## 项目结构

```
src/
├── main/
│   ├── java/com/yhj/dictation/
│   │   ├── controller/     # 控制器
│   │   ├── service/        # 服务层
│   │   ├── entity/         # 实体类
│   │   ├── repository/     # 数据访问
│   │   └── dto/            # 数据传输对象
│   └── resources/
│       ├── templates/      # HTML模板
│       └── application.properties
└── test/                   # 测试代码
```

## 许可证

MIT License