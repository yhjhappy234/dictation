# 审计日志 API

## 概述

审计日志API用于管理员查询系统操作日志，所有接口需要管理员权限。

## 权限要求

所有审计日志API需要管理员权限，通过 `UserContext.isAdmin()` 校验。
非管理员访问将返回：
```json
{
  "success": false,
  "message": "需要管理员权限",
  "code": 403
}
```

## API 接口

### 1. 获取审计日志列表

```
GET /api/v1/audit-logs
```

**参数**
- `page` (int, optional): 页码，默认0
- `size` (int, optional): 每页数量，默认20

**响应**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  }
}
```

### 2. 搜索审计日志

```
GET /api/v1/audit-logs/search
```

**参数**
- `username` (string, optional): 用户名筛选
- `operation` (string, optional): 操作类型筛选
- `startTime` (string, optional): 开始时间 (格式: yyyy-MM-dd HH:mm:ss)
- `endTime` (string, optional): 结束时间 (格式: yyyy-MM-dd HH:mm:ss)
- `page` (int, optional): 页码，默认0
- `size` (int, optional): 每页数量，默认20

**响应**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 50,
    "totalPages": 3,
    "number": 0,
    "size": 20
  }
}
```

### 3. 获取日志详情

```
GET /api/v1/audit-logs/{id}
```

**响应**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "username": "admin",
    "operation": "LOGIN",
    "method": "AuthController.login",
    "params": "{\"username\":\"admin\"}",
    "result": "{\"success\":true}",
    "ipAddress": "127.0.0.1",
    "timestamp": "2026-04-18T10:30:00",
    "durationMs": 100,
    "success": true,
    "errorMessage": null
  }
}
```

### 4. 获取用户名列表

```
GET /api/v1/audit-logs/usernames
```

用于筛选下拉框的用户名选项。

**响应**
```json
{
  "success": true,
  "data": ["admin", "user1", "user2"]
}
```

### 5. 获取操作类型列表

```
GET /api/v1/audit-logs/operations
```

用于筛选下拉框的操作类型选项。

**响应**
```json
{
  "success": true,
  "data": ["LOGIN", "CREATE_USER", "DELETE_USER", "UPDATE_PASSWORD"]
}
```

### 6. 统计日志数量

```
GET /api/v1/audit-logs/count
```

**参数**
- `startTime` (string, optional): 开始时间
- `endTime` (string, optional): 结束时间

**响应**
```json
{
  "success": true,
  "data": 100
}
```

## 操作类型说明

| 操作类型 | 说明 |
|---------|------|
| LOGIN | 用户登录 |
| LOGOUT | 用户登出 |
| CREATE_USER | 创建用户 |
| UPDATE_USER | 更新用户信息 |
| DELETE_USER | 删除用户 |
| UPDATE_PASSWORD | 修改密码 |
| CREATE_TASK | 创建听写任务 |
| COMPLETE_TASK | 完成听写任务 |
| IMPORT_PRESET | 导入预设内容 |