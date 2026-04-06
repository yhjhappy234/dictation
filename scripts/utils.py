"""
E2E测试工具函数
"""
import requests
from config import BASE_URL, TIMEOUT


def make_request(method, endpoint, data=None):
    """发送HTTP请求"""
    url = BASE_URL + endpoint
    try:
        if method == "GET":
            response = requests.get(url, timeout=TIMEOUT)
        elif method == "POST":
            response = requests.post(url, json=data, timeout=TIMEOUT)
        elif method == "DELETE":
            response = requests.delete(url, timeout=TIMEOUT)
        else:
            raise ValueError(f"不支持的请求方法: {method}")
        return response
    except requests.exceptions.RequestException as e:
        return None


def assert_response_ok(response):
    """断言响应成功"""
    assert response is not None, "请求失败，无响应"
    assert response.status_code == 200, f"请求失败，状态码: {response.status_code}"


def generate_test_words(count=5):
    """生成测试词语"""
    base_words = ["苹果", "香蕉", "橘子", "西瓜", "葡萄", "草莓", "樱桃", "桃子", "梨子", "荔枝"]
    return base_words[:count]