"""
E2E测试主程序
测试听写助手的核心功能
"""
import pytest
import sys
import os

# 添加脚本目录到路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from config import BASE_URL, TEST_WORDS
from utils import make_request, assert_response_ok, generate_test_words


class TestHealthCheck:
    """健康检查测试"""

    def test_server_running(self):
        """测试服务器是否运行"""
        response = make_request("GET", "/")
        assert response is not None, "服务器未运行，请先启动Spring Boot应用"
        assert response.status_code == 200


class TestBatchAPI:
    """批次API测试"""

    def test_create_batch(self):
        """测试创建批次"""
        data = {"words": TEST_WORDS}
        response = make_request("POST", "/api/batches", data)
        assert_response_ok(response)
        result = response.json()
        assert result["success"] == True
        assert "data" in result

    def test_get_all_batches(self):
        """测试获取所有批次"""
        response = make_request("GET", "/api/batches")
        assert_response_ok(response)
        result = response.json()
        assert "data" in result

    def test_get_batch_by_id(self):
        """测试获取单个批次"""
        # 先创建一个批次
        create_data = {"words": TEST_WORDS}
        create_response = make_request("POST", "/api/batches", create_data)
        assert_response_ok(create_response)
        batch_id = create_response.json()["data"]["id"]

        # 然后获取这个批次
        response = make_request("GET", f"/api/batches/{batch_id}")
        assert_response_ok(response)


class TestWordAPI:
    """词语API测试"""

    def test_get_words_by_batch(self):
        """测试获取批次词语"""
        # 先创建批次
        create_data = {"words": TEST_WORDS}
        create_response = make_request("POST", "/api/batches", create_data)
        assert_response_ok(create_response)
        batch_id = create_response.json()["data"]["id"]

        # 获取词语列表
        response = make_request("GET", f"/api/words/batch/{batch_id}")
        assert_response_ok(response)


class TestPageRoutes:
    """页面路由测试"""

    def test_index_page(self):
        """测试首页"""
        response = make_request("GET", "/")
        assert_response_ok(response)

    def test_history_page(self):
        """测试历史记录页面"""
        response = make_request("GET", "/history")
        assert_response_ok(response)

    def test_difficult_words_page(self):
        """测试生词本页面"""
        response = make_request("GET", "/difficult-words")
        assert_response_ok(response)

    def test_reports_page(self):
        """测试报表页面"""
        response = make_request("GET", "/reports")
        assert_response_ok(response)


class TestDifficultWordsAPI:
    """生词本API测试"""

    def test_get_difficult_words(self):
        """测试获取生词列表"""
        response = make_request("GET", "/api/difficult-words")
        assert_response_ok(response)


class TestReportsAPI:
    """报表API测试"""

    def test_daily_report(self):
        """测试日报表"""
        response = make_request("GET", "/api/reports/daily")
        assert_response_ok(response)

    def test_weekly_report(self):
        """测试周报表"""
        response = make_request("GET", "/api/reports/weekly")
        assert_response_ok(response)

    def test_monthly_report(self):
        """测试月报表"""
        response = make_request("GET", "/api/reports/monthly")
        assert_response_ok(response)


if __name__ == "__main__":
    pytest.main([__file__, "-v", "--html=../report/e2e_report.html", "--self-contained-html"])