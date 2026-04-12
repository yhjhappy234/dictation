"""
E2E测试配置文件
"""
import os

# 基础URL配置
BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

# API端点配置
API_ENDPOINTS = {
    # 批次管理
    "batches": "/api/batches",
    "batch_by_id": "/api/batches/{id}",
    "batch_start": "/api/batches/{id}/start",
    "batch_complete": "/api/batches/{id}/complete",
    "batch_cancel": "/api/batches/{id}/cancel",
    "batch_words": "/api/batches/{id}/words",
    "batch_reset": "/api/batches/{id}/reset",

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
    "record_skip": "/api/records/{id}/skip",
    "record_repeat": "/api/records/{id}/repeat",
    "records_by_batch": "/api/records/batch/{batchId}",
    "records_today": "/api/records/today",
    "records_range": "/api/records/range",
    "report_today": "/api/records/report/today",
    "report_batch": "/api/records/report/batch/{batchId}",
    "finish_batch": "/api/records/batch/{batchId}/finish",

    # 生词本
    "difficult_words": "/api/difficult-words",
    "difficult_words_difficult": "/api/difficult-words/difficult",
    "difficult_words_recommended": "/api/difficult-words/recommended",
    "difficult_word_mastery": "/api/difficult-words/{id}/mastery",
    "difficult_word_success": "/api/difficult-words/{wordId}/success",
    "difficult_word_failure": "/api/difficult-words/{wordId}/failure",

    # 建议
    "suggestions": "/api/suggestions",
    "suggestions_by_type": "/api/suggestions/type/{type}",
    "suggestions_by_word": "/api/suggestions/word/{wordId}",
    "suggestions_review": "/api/suggestions/review",
    "suggestions_high_difficulty": "/api/suggestions/high-difficulty",
    "suggestions_frequent_error": "/api/suggestions/frequent-error",
    "suggestion_priority": "/api/suggestions/{id}/priority",

    # 页面路由
    "page_index": "/",
    "page_history": "/history",
    "page_difficult_words": "/difficult-words",
    "page_reports": "/reports",
}

# 页面路由
PAGE_ROUTES = {
    "index": "/",
    "history": "/history",
    "difficult_words": "/difficult-words",
    "reports": "/reports",
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
    "words": "苹果 香蕉 橘子 葡萄 西瓜",
    "single_word": "测试",
    "updated_pinyin": "ce shi",
}

# 报告输出目录
REPORT_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "report")

# 重试配置
RETRY_COUNT = 3
RETRY_DELAY = 1  # 秒