"""
E2E测试配置文件
"""
import os

# 基础URL配置
BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

# API端点配置
API_ENDPOINTS = {
    # 认证管理
    "auth_login": "/api/v1/auth/login",
    "auth_logout": "/api/v1/auth/logout",
    "auth_current": "/api/v1/auth/current",
    "auth_status": "/api/v1/auth/status",
    "auth_avatars": "/api/v1/auth/avatars",

    # 用户管理
    "users": "/api/v1/users",
    "user_by_id": "/api/v1/users/{id}",
    "user_me_password": "/api/v1/users/me/password",
    "user_me_avatar": "/api/v1/users/me/avatar",
    "user_me_is_admin": "/api/v1/users/me/is-admin",

    # 批次管理
    "batches": "/api/v1/batches",
    "batch_by_id": "/api/v1/batches/{id}",
    "batch_start": "/api/v1/batches/{id}/start",
    "batch_complete": "/api/v1/batches/{id}/complete",
    "batch_cancel": "/api/v1/batches/{id}/cancel",
    "batch_words": "/api/v1/batches/{id}/words",
    "batch_reset": "/api/v1/batches/{id}/reset",
    "batches_today": "/api/v1/batches/today",
    "batches_range": "/api/v1/batches/range",

    # 词语管理
    "words": "/api/v1/words",
    "word_by_id": "/api/v1/words/{id}",
    "word_status": "/api/v1/words/{id}/status",
    "word_pinyin": "/api/v1/words/{id}/pinyin",
    "word_complete": "/api/v1/words/{id}/complete",
    "word_skip": "/api/v1/words/{id}/skip",
    "word_next": "/api/v1/words/batch/{batchId}/next",
    "word_previous": "/api/v1/words/batch/{batchId}/previous",
    "word_first": "/api/v1/words/batch/{batchId}/first",

    # 听写记录
    "record_start": "/api/v1/records/start",
    "records": "/api/v1/records",
    "record_by_id": "/api/v1/records/{id}",
    "record_complete": "/api/v1/records/{id}/complete",
    "record_complete_by_word": "/api/v1/records/complete/{wordId}",
    "record_skip": "/api/v1/records/{id}/skip",
    "record_repeat": "/api/v1/records/{id}/repeat",
    "records_by_batch": "/api/v1/records/batch/{batchId}",
    "records_today": "/api/v1/records/today",
    "records_range": "/api/v1/records/range",
    "report_today": "/api/v1/records/report/today",
    "report_batch": "/api/v1/records/report/batch/{batchId}",
    "finish_batch": "/api/v1/records/batch/{batchId}/finish",
    "end_dictation": "/api/v1/records/end/{batchId}",
    "record_delete": "/api/v1/records/{id}",

    # 任务管理
    "tasks": "/api/v1/tasks",
    "task_by_id": "/api/v1/tasks/{id}",
    "tasks_uncompleted": "/api/v1/tasks/uncompleted",
    "tasks_by_status": "/api/v1/tasks/status/{status}",
    "tasks_favorites": "/api/v1/tasks/favorites",
    "tasks_dictators": "/api/v1/tasks/dictators",
    "task_create": "/api/v1/tasks",
    "task_update": "/api/v1/tasks/{id}",
    "task_delete": "/api/v1/tasks/{id}",
    "task_start": "/api/v1/tasks/{id}/start",
    "task_complete": "/api/v1/tasks/{id}/complete",
    "task_favorite": "/api/v1/tasks/{id}/favorite",
    "task_dictator": "/api/v1/tasks/{id}/dictator",
    "task_reset": "/api/v1/tasks/{id}/reset",
    "task_reset_progress": "/api/v1/tasks/{id}/reset-progress",
    "task_progress": "/api/v1/tasks/{id}/progress",
    "task_record": "/api/v1/tasks/{id}/record",
    "task_dictation": "/api/v1/tasks/{id}/dictation",
    "task_records": "/api/v1/tasks/{id}/records",
    "task_start_word": "/api/v1/tasks/{id}/start-word",
    "task_read_word": "/api/v1/tasks/{id}/read-word",
    "task_complete_word": "/api/v1/tasks/{id}/complete-word",
    "task_status": "/api/v1/tasks/{id}/status",

    # 生词本
    "difficult_words": "/api/v1/difficult-words",
    "difficult_words_difficult": "/api/v1/difficult-words/difficult",
    "difficult_words_recommended": "/api/v1/difficult-words/recommended",
    "difficult_word_mastery": "/api/v1/difficult-words/{id}/mastery",
    "difficult_word_success": "/api/v1/difficult-words/text/{wordText}/success",
    "difficult_word_failure": "/api/v1/difficult-words/text/{wordText}/failure",
    "difficult_word_by_id": "/api/v1/difficult-words/{id}",

    # 建议
    "suggestions": "/api/v1/suggestions",
    "suggestions_by_type": "/api/v1/suggestions/type/{type}",
    "suggestions_by_word": "/api/v1/suggestions/word/{wordId}",
    "suggestions_review": "/api/v1/suggestions/review",
    "suggestions_high_difficulty": "/api/v1/suggestions/high-difficulty",
    "suggestions_frequent_error": "/api/v1/suggestions/frequent-error",
    "suggestion_priority": "/api/v1/suggestions/{id}/priority",

    # 预设内容
    "preset_list": "/api/v1/preset/list",
    "preset_content": "/api/v1/preset/{id}",
    "preset_import": "/api/v1/preset/import/{id}",

    # 页面路由
    "page_index": "/",
    "page_login": "/login",
    "page_history": "/history",
    "page_difficult_words": "/difficult-words",
    "page_reports": "/reports",
    "page_tasks": "/tasks",
    "page_dictators": "/dictators",
    "page_user_management": "/user-management",
}

# 页面路由
PAGE_ROUTES = {
    "index": "/",
    "login": "/login",
    "history": "/history",
    "difficult_words": "/difficult-words",
    "reports": "/reports",
    "tasks": "/tasks",
    "dictators": "/dictators",
    "user_management": "/user-management",
}

# 超时设置（秒）
TIMEOUT = {
    "request": 10,
    "health_check": 30,
    "page_load": 15,
    "selenium": 10,
}

# 测试数据
TEST_DATA = {
    "batch_name": "E2E测试批次",
    "words": "苹果 馝蕉 橘子 葡萄 西瓜",
    "single_word": "测试",
    "updated_pinyin": "ce shi",
    "task_name": "E2E测试任务",
    "username": "admin",
    "password": "123456",
    "new_user_username": "testuser_e2e",
    "new_user_password": "test123",
}

# 报告输出目录
REPORT_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "report")

# 重试配置
RETRY_COUNT = 3
RETRY_DELAY = 1  # 秒