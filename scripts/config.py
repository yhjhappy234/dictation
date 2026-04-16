"""
E2E测试配置文件
"""
import os

# 基础URL配置
BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

# API端点配置
API_ENDPOINTS = {
    # 认证管理
    "auth_login": "/api/auth/login",
    "auth_logout": "/api/auth/logout",
    "auth_current": "/api/auth/current",
    "auth_status": "/api/auth/status",
    "auth_avatars": "/api/auth/avatars",

    # 用户管理
    "users": "/api/users",
    "user_by_id": "/api/users/{id}",
    "user_me_password": "/api/users/me/password",
    "user_me_avatar": "/api/users/me/avatar",
    "user_me_is_admin": "/api/users/me/is-admin",

    # 批次管理
    "batches": "/api/batches",
    "batch_by_id": "/api/batches/{id}",
    "batch_start": "/api/batches/{id}/start",
    "batch_complete": "/api/batches/{id}/complete",
    "batch_cancel": "/api/batches/{id}/cancel",
    "batch_words": "/api/batches/{id}/words",
    "batch_reset": "/api/batches/{id}/reset",
    "batches_today": "/api/batches/today",
    "batches_range": "/api/batches/range",

    # 词语管理
    "words": "/api/words",
    "word_by_id": "/api/words/{id}",
    "word_status": "/api/words/{id}/status",
    "word_pinyin": "/api/words/{id}/pinyin",
    "word_complete": "/api/words/{id}/complete",
    "word_skip": "/api/words/{id}/skip",
    "word_next": "/api/words/batch/{batchId}/next",
    "word_previous": "/api/words/batch/{batchId}/previous",
    "word_first": "/api/words/batch/{batchId}/first",

    # 听写记录
    "record_start": "/api/records/start",
    "records": "/api/records",
    "record_by_id": "/api/records/{id}",
    "record_complete": "/api/records/{id}/complete",
    "record_complete_by_word": "/api/records/complete/{wordId}",
    "record_skip": "/api/records/{id}/skip",
    "record_repeat": "/api/records/{id}/repeat",
    "records_by_batch": "/api/records/batch/{batchId}",
    "records_today": "/api/records/today",
    "records_range": "/api/records/range",
    "report_today": "/api/records/report/today",
    "report_batch": "/api/records/report/batch/{batchId}",
    "finish_batch": "/api/records/batch/{batchId}/finish",
    "end_dictation": "/api/records/end/{batchId}",
    "record_delete": "/api/records/{id}",

    # 任务管理
    "tasks": "/api/tasks",
    "task_by_id": "/api/tasks/{id}",
    "tasks_uncompleted": "/api/tasks/uncompleted",
    "tasks_by_status": "/api/tasks/status/{status}",
    "tasks_favorites": "/api/tasks/favorites",
    "tasks_dictators": "/api/tasks/dictators",
    "task_create": "/api/tasks",
    "task_update": "/api/tasks/{id}",
    "task_delete": "/api/tasks/{id}",
    "task_start": "/api/tasks/{id}/start",
    "task_complete": "/api/tasks/{id}/complete",
    "task_favorite": "/api/tasks/{id}/favorite",
    "task_dictator": "/api/tasks/{id}/dictator",
    "task_reset": "/api/tasks/{id}/reset",
    "task_reset_progress": "/api/tasks/{id}/reset-progress",
    "task_progress": "/api/tasks/{id}/progress",
    "task_record": "/api/tasks/{id}/record",
    "task_dictation": "/api/tasks/{id}/dictation",
    "task_records": "/api/tasks/{id}/records",
    "task_start_word": "/api/tasks/{id}/start-word",
    "task_read_word": "/api/tasks/{id}/read-word",
    "task_complete_word": "/api/tasks/{id}/complete-word",
    "task_status": "/api/tasks/{id}/status",

    # 生词本
    "difficult_words": "/api/difficult-words",
    "difficult_words_difficult": "/api/difficult-words/difficult",
    "difficult_words_recommended": "/api/difficult-words/recommended",
    "difficult_word_mastery": "/api/difficult-words/{id}/mastery",
    "difficult_word_success": "/api/difficult-words/text/{wordText}/success",
    "difficult_word_failure": "/api/difficult-words/text/{wordText}/failure",
    "difficult_word_by_id": "/api/difficult-words/{id}",

    # 建议
    "suggestions": "/api/suggestions",
    "suggestions_by_type": "/api/suggestions/type/{type}",
    "suggestions_by_word": "/api/suggestions/word/{wordId}",
    "suggestions_review": "/api/suggestions/review",
    "suggestions_high_difficulty": "/api/suggestions/high-difficulty",
    "suggestions_frequent_error": "/api/suggestions/frequent-error",
    "suggestion_priority": "/api/suggestions/{id}/priority",

    # 预设内容
    "preset_list": "/api/preset/list",
    "preset_content": "/api/preset/{id}",
    "preset_import": "/api/preset/import/{id}",

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