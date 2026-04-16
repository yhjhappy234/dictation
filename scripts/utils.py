"""
工具函数模块
"""
import time
import random
import string
from typing import Any, Dict, List, Optional

import requests

from config import BASE_URL, TIMEOUT, RETRY_COUNT, RETRY_DELAY


class HttpClient:
    """HTTP请求封装类"""

    def __init__(self, base_url: str = BASE_URL):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()

    def _request(self, method: str, endpoint: str, **kwargs) -> requests.Response:
        """发送HTTP请求"""
        url = f"{self.base_url}{endpoint}"
        kwargs.setdefault("timeout", TIMEOUT["request"])

        for attempt in range(RETRY_COUNT):
            try:
                response = self.session.request(method, url, **kwargs)
                return response
            except requests.exceptions.RequestException as e:
                if attempt == RETRY_COUNT - 1:
                    raise
                time.sleep(RETRY_DELAY)

    def get(self, endpoint: str, **kwargs) -> requests.Response:
        """GET请求"""
        return self._request("GET", endpoint, **kwargs)

    def post(self, endpoint: str, **kwargs) -> requests.Response:
        """POST请求"""
        return self._request("POST", endpoint, **kwargs)

    def put(self, endpoint: str, **kwargs) -> requests.Response:
        """PUT请求"""
        return self._request("PUT", endpoint, **kwargs)

    def delete(self, endpoint: str, **kwargs) -> requests.Response:
        """DELETE请求"""
        return self._request("DELETE", endpoint, **kwargs)


class AssertUtil:
    """断言工具类"""

    @staticmethod
    def assert_success(response: requests.Response, message: str = None) -> None:
        """断言响应成功"""
        assert response.status_code == 200, f"请求失败, 状态码: {response.status_code}"
        data = response.json()
        assert data.get("success") is True, f"操作失败: {data.get('message')}"
        if message is not None:
            assert message in data.get("message", ""), f"消息不匹配: {data.get('message')}"

    @staticmethod
    def assert_error(response: requests.Response, expected_status: int = None) -> None:
        """断言响应失败"""
        if expected_status:
            assert response.status_code == expected_status, f"状态码不匹配: {response.status_code}"
        data = response.json()
        assert data.get("success") is False, f"操作应该失败但成功了: {data}"

    @staticmethod
    def assert_data_not_empty(response: requests.Response) -> Any:
        """断言数据不为空"""
        data = response.json()
        assert data.get("data") is not None, "返回数据为空"
        return data.get("data")

    @staticmethod
    def assert_list_not_empty(response: requests.Response) -> List:
        """断言列表不为空"""
        data = response.json()
        items = data.get("data", [])
        assert isinstance(items, list), f"返回数据不是列表: {type(items)}"
        return items

    @staticmethod
    def assert_field_equal(response: requests.Response, field: str, expected_value: Any) -> None:
        """断言字段值相等"""
        data = response.json()
        actual_value = data.get("data", {}).get(field)
        assert actual_value == expected_value, f"字段 {field} 值不匹配: {actual_value} != {expected_value}"


class DataGenerator:
    """数据生成工具类"""

    @staticmethod
    def random_string(length: int = 8) -> str:
        """生成随机字符串"""
        return "".join(random.choices(string.ascii_lowercase, k=length))

    @staticmethod
    def random_batch_name() -> str:
        """生成随机批次名称"""
        return f"测试批次_{DataGenerator.random_string(6)}"

    @staticmethod
    def random_words(count: int = 5) -> str:
        """生成随机词语列表"""
        words = [f"词{DataGenerator.random_string(2)}" for _ in range(count)]
        return " ".join(words)

    @staticmethod
    def random_pinyin() -> str:
        """生成随机拼音"""
        return f"{DataGenerator.random_string(4)} {DataGenerator.random_string(4)}"


def wait_for_server(client: HttpClient, max_wait: int = None) -> bool:
    """等待服务器启动"""
    max_wait = max_wait or TIMEOUT["health_check"]
    start_time = time.time()

    while time.time() - start_time < max_wait:
        try:
            response = client.get("/")
            if response.status_code == 200:
                return True
        except requests.exceptions.RequestException:
            pass
        time.sleep(1)

    return False


def cleanup_test_data(client: HttpClient, batch_id: Optional[int] = None) -> None:
    """清理测试数据"""
    if batch_id:
        try:
            client.delete(f"/api/batches/{batch_id}")
        except Exception:
            pass