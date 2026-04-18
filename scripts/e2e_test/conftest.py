"""
pytest 全局配置与 fixture
"""
import pytest
import os
import sys

# 添加项目路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from config.base_config import BASE_URL
from utils.http_client import HttpClient


@pytest.fixture(scope="session")
def base_url():
    return os.getenv("BASE_URL", BASE_URL)


@pytest.fixture(scope="session")
def api_client(base_url):
    return HttpClient(base_url=base_url, timeout=30)


@pytest.fixture(scope="session", autouse=True)
def check_service_health(api_client):
    """测试前检查服务是否正常"""
    try:
        response = api_client.get("/actuator/health")
        assert response.status_code == 200, "服务未启动或不健康"
    except Exception as e:
        pytest.fail(f"服务连接失败: {e}")