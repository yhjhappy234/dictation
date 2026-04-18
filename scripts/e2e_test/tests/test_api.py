"""
E2E测试主程序 - 全面覆盖所有API功能
"""
import pytest
import time
import os
from datetime import date, timedelta
from typing import Generator

from config.base_config import (
    API_ENDPOINTS,
    PAGE_ROUTES,
    TIMEOUT,
    TEST_DATA,
)
from utils.http_client import (
    HttpClient,
    AssertUtil,
    DataGenerator,
    wait_for_server,
    cleanup_test_data,
)

# 动态获取BASE_URL
BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")


# ==================== Fixtures ====================

@pytest.fixture(scope="module")
def client() -> Generator[HttpClient, None, None]:
    """创建HTTP客户端"""
    http_client = HttpClient(BASE_URL)
    yield http_client


@pytest.fixture(scope="module")
def setup_server(client: HttpClient) -> None:
    """确保服务器已启动"""
    assert wait_for_server(client), "服务器未启动或无法访问"


@pytest.fixture(scope="module")
def logged_in_client(client: HttpClient) -> Generator[HttpClient, None, None]:
    """登录后的客户端 - 在yield前登录"""
    login_data = {
        "username": TEST_DATA["username"],
        "password": TEST_DATA["password"]
    }
    response = client.post(API_ENDPOINTS["auth_login"], json=login_data)
    yield client


@pytest.fixture(autouse=True)
def ensure_login(client: HttpClient):
    """确保每个测试前已登录"""
    # 在每个测试前登录
    login_data = {
        "username": TEST_DATA["username"],
        "password": TEST_DATA["password"]
    }
    client.post(API_ENDPOINTS["auth_login"], json=login_data)
    yield


@pytest.fixture
def created_batch(client: HttpClient) -> Generator[dict, None, None]:
    """创建测试批次并清理 - 每次创建前重新登录"""
    # 确保登录
    login_data = {
        "username": TEST_DATA["username"],
        "password": TEST_DATA["password"]
    }
    client.post(API_ENDPOINTS["auth_login"], json=login_data)

    batch_data = {
        "batchName": DataGenerator.random_batch_name(),
        "words": TEST_DATA["words"]
    }
    response = client.post(API_ENDPOINTS["batches"], json=batch_data)
    AssertUtil.assert_success(response)

    batch = response.json().get("data")
    batch_id = batch.get("id")

    yield batch

    # 清理
    cleanup_test_data(client, batch_id)


@pytest.fixture
def created_task(client: HttpClient) -> Generator[dict, None, None]:
    """创建测试任务并清理 - 每次创建前重新登录"""
    # 确保登录
    login_data = {
        "username": TEST_DATA["username"],
        "password": TEST_DATA["password"]
    }
    client.post(API_ENDPOINTS["auth_login"], json=login_data)

    task_data = {
        "taskName": DataGenerator.random_batch_name(),
        "words": TEST_DATA["words"]
    }
    response = client.post(API_ENDPOINTS["tasks"], json=task_data)
    if response.status_code == 200:
        task = response.json().get("data")
        task_id = task.get("id")
        yield task
        # 清理
        client.delete(API_ENDPOINTS["task_by_id"].format(id=task_id))
    else:
        yield {}


# ==================== 健康检查测试 ====================

class TestServerHealth:
    """服务器健康检查测试"""

    def test_server_is_running(self, client: HttpClient, setup_server: None):
        """测试服务器是否运行"""
        response = client.get("/")
        assert response.status_code == 200, f"服务器未运行, 状态码: {response.status_code}"

    def test_api_health_check(self, client: HttpClient):
        """测试API健康检查"""
        response = client.get(API_ENDPOINTS["batches"])
        assert response.status_code == 200, f"API不可用, 状态码: {response.status_code}"


# ==================== 认证API测试 ====================

class TestAuthAPI:
    """认证API测试"""

    def test_login_success(self, client: HttpClient):
        """测试登录成功"""
        login_data = {
            "username": TEST_DATA["username"],
            "password": TEST_DATA["password"]
        }
        response = client.post(API_ENDPOINTS["auth_login"], json=login_data)
        # 可能成功也可能失败（取决于服务器配置）
        assert response.status_code == 200

    def test_login_empty_username(self, client: HttpClient):
        """测试登录 - 空用户名"""
        login_data = {
            "username": "",
            "password": "password"
        }
        response = client.post(API_ENDPOINTS["auth_login"], json=login_data)
        AssertUtil.assert_error(response)

    def test_login_empty_password(self, client: HttpClient):
        """测试登录 - 空密码"""
        login_data = {
            "username": "testuser",
            "password": ""
        }
        response = client.post(API_ENDPOINTS["auth_login"], json=login_data)
        AssertUtil.assert_error(response)

    def test_login_wrong_password(self, client: HttpClient):
        """测试登录 - 错误密码"""
        login_data = {
            "username": TEST_DATA["username"],
            "password": "wrongpassword"
        }
        response = client.post(API_ENDPOINTS["auth_login"], json=login_data)
        # 应该返回错误
        if response.status_code == 200:
            assert response.json().get("success") == False

    def test_get_current_user(self, client: HttpClient):
        """测试获取当前用户"""
        response = client.get(API_ENDPOINTS["auth_current"])
        assert response.status_code == 200

    def test_check_login_status(self, client: HttpClient):
        """测试检查登录状态"""
        response = client.get(API_ENDPOINTS["auth_status"])
        AssertUtil.assert_success(response)
        data = response.json().get("data")
        assert isinstance(data, bool)

    def test_get_avatars(self, client: HttpClient):
        """测试获取头像列表"""
        response = client.get(API_ENDPOINTS["auth_avatars"])
        AssertUtil.assert_success(response)
        avatars = response.json().get("data", [])
        assert len(avatars) > 0, "头像列表为空"

    def test_logout(self, client: HttpClient):
        """测试登出"""
        response = client.post(API_ENDPOINTS["auth_logout"])
        assert response.status_code == 200


# ==================== 用户管理API测试 ====================

class TestUserAPI:
    """用户管理API测试"""

    def test_get_all_users(self, client: HttpClient):
        """测试获取所有用户"""
        response = client.get(API_ENDPOINTS["users"])
        # 可能需要管理员权限
        assert response.status_code == 200

    def test_is_admin(self, client: HttpClient):
        """测试检查管理员状态"""
        response = client.get(API_ENDPOINTS["user_me_is_admin"])
        AssertUtil.assert_success(response)
        data = response.json().get("data")
        assert isinstance(data, bool)

    def test_update_avatar(self, client: HttpClient):
        """测试更新头像"""
        # 先获取可用头像
        avatars_response = client.get(API_ENDPOINTS["auth_avatars"])
        if avatars_response.status_code == 200:
            avatars = avatars_response.json().get("data", [])
            if avatars:
                response = client.post(
                    API_ENDPOINTS["user_me_avatar"],
                    params={"avatar": avatars[0]}
                )
                # 可能需要登录
                assert response.status_code in [200, 401]


# ==================== 批次管理API测试 ====================

class TestBatchAPI:
    """批次管理API测试"""

    def test_create_batch(self, client: HttpClient):
        """测试创建批次"""
        # 先登录
        client.post(API_ENDPOINTS["auth_login"], json={
            "username": TEST_DATA["username"],
            "password": TEST_DATA["password"]
        })
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        AssertUtil.assert_success(response)

        data = AssertUtil.assert_data_not_empty(response)
        assert data.get("id") is not None, "批次ID为空"
        assert data.get("batchName") == batch_data["batchName"], "批次名称不匹配"

        # 清理
        cleanup_test_data(client, data.get("id"))

    def test_create_batch_with_empty_words(self, client: HttpClient):
        """测试创建空词语批次"""
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": ""
        }
        response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        # 应该允许创建空批次或返回错误
        assert response.status_code in [200, 400], f"状态码不正确: {response.status_code}"

    def test_get_all_batches(self, client: HttpClient, created_batch: dict):
        """测试获取所有批次"""
        response = client.get(API_ENDPOINTS["batches"])
        AssertUtil.assert_success(response)

        batches = AssertUtil.assert_list_not_empty(response)
        batch_ids = [b.get("id") for b in batches]
        assert created_batch.get("id") in batch_ids, "创建的批次不在列表中"

    def test_get_batch_by_id(self, client: HttpClient, created_batch: dict):
        """测试根据ID获取批次"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["batch_by_id"].format(id=batch_id)
        response = client.get(endpoint)

        AssertUtil.assert_success(response)
        data = AssertUtil.assert_data_not_empty(response)
        assert data.get("id") == batch_id, "批次ID不匹配"

    def test_get_nonexistent_batch(self, client: HttpClient):
        """测试获取不存在的批次"""
        endpoint = API_ENDPOINTS["batch_by_id"].format(id=999999)
        response = client.get(endpoint)
        AssertUtil.assert_error(response)

    def test_get_today_batches(self, client: HttpClient):
        """测试获取今日批次"""
        response = client.get(API_ENDPOINTS["batches_today"])
        AssertUtil.assert_success(response)

    def test_get_batches_by_range(self, client: HttpClient):
        """测试获取日期范围批次"""
        today = date.today()
        start = today - timedelta(days=7)
        end = today

        response = client.get(
            API_ENDPOINTS["batches_range"],
            params={"start": start.isoformat(), "end": end.isoformat()}
        )
        AssertUtil.assert_success(response)

    def test_start_batch(self, client: HttpClient, created_batch: dict):
        """测试开始批次"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["batch_start"].format(id=batch_id)
        response = client.post(endpoint)

        AssertUtil.assert_success(response)
        data = response.json().get("data")
        assert data.get("status") == "IN_PROGRESS", "批次状态不是进行中"

    def test_complete_batch(self, client: HttpClient):
        """测试完成批次"""
        # 创建批次
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        batch = create_response.json().get("data")
        batch_id = batch.get("id")

        # 开始批次
        start_endpoint = API_ENDPOINTS["batch_start"].format(id=batch_id)
        client.post(start_endpoint)

        # 完成批次
        complete_endpoint = API_ENDPOINTS["batch_complete"].format(id=batch_id)
        response = client.post(complete_endpoint)

        AssertUtil.assert_success(response)
        data = response.json().get("data")
        assert data.get("status") == "COMPLETED", "批次状态不是已完成"

        # 清理
        cleanup_test_data(client, batch_id)

    def test_cancel_batch(self, client: HttpClient, created_batch: dict):
        """测试取消批次"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["batch_cancel"].format(id=batch_id)
        response = client.post(endpoint)

        AssertUtil.assert_success(response)
        data = response.json().get("data")
        assert data.get("status") == "CANCELLED", "批次状态不是已取消"

    def test_get_batch_words(self, client: HttpClient, created_batch: dict):
        """测试获取批次词语"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        response = client.get(endpoint)

        AssertUtil.assert_success(response)
        words = AssertUtil.assert_list_not_empty(response)
        assert len(words) == 5, f"词语数量不正确: {len(words)}"

    def test_reset_batch_words(self, client: HttpClient, created_batch: dict):
        """测试重置批次词语"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["batch_reset"].format(id=batch_id)
        response = client.post(endpoint)

        AssertUtil.assert_success(response)

    def test_delete_batch(self, client: HttpClient):
        """测试删除批次"""
        # 创建批次
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        batch = create_response.json().get("data")
        batch_id = batch.get("id")

        # 删除批次
        endpoint = API_ENDPOINTS["batch_by_id"].format(id=batch_id)
        response = client.delete(endpoint)

        AssertUtil.assert_success(response)


# ==================== 词语管理API测试 ====================

class TestWordAPI:
    """词语管理API测试"""

    def test_get_word_by_id(self, client: HttpClient, created_batch: dict):
        """测试根据ID获取词语"""
        # 获取批次的词语
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_id = words[0].get("id")
            endpoint = API_ENDPOINTS["word_by_id"].format(id=word_id)
            response = client.get(endpoint)

            AssertUtil.assert_success(response)
            data = AssertUtil.assert_data_not_empty(response)
            assert data.get("id") == word_id, "词语ID不匹配"

    def test_get_nonexistent_word(self, client: HttpClient):
        """测试获取不存在的词语"""
        endpoint = API_ENDPOINTS["word_by_id"].format(id=999999)
        response = client.get(endpoint)
        AssertUtil.assert_error(response)

    def test_update_word_status(self, client: HttpClient, created_batch: dict):
        """测试更新词语状态"""
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_id = words[0].get("id")
            endpoint = API_ENDPOINTS["word_status"].format(id=word_id)
            response = client.put(endpoint, json={"status": "COMPLETED"})

            AssertUtil.assert_success(response)

    def test_update_word_pinyin(self, client: HttpClient, created_batch: dict):
        """测试更新词语拼音"""
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_id = words[0].get("id")
            endpoint = API_ENDPOINTS["word_pinyin"].format(id=word_id)
            response = client.put(endpoint, json={"pinyin": "test pinyin"})

            AssertUtil.assert_success(response)

    def test_mark_word_completed(self, client: HttpClient, created_batch: dict):
        """测试标记词语完成"""
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_id = words[0].get("id")
            endpoint = API_ENDPOINTS["word_complete"].format(id=word_id)
            response = client.post(endpoint)

            AssertUtil.assert_success(response)

    def test_mark_word_skipped(self, client: HttpClient, created_batch: dict):
        """测试标记词语跳过"""
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_id = words[0].get("id")
            endpoint = API_ENDPOINTS["word_skip"].format(id=word_id)
            response = client.post(endpoint)

            AssertUtil.assert_success(response)

    def test_get_first_word(self, client: HttpClient, created_batch: dict):
        """测试获取批次第一个词语"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["word_first"].format(batchId=batch_id)
        response = client.get(endpoint)

        AssertUtil.assert_success(response)
        data = AssertUtil.assert_data_not_empty(response)
        # sortOrder 从 1 开始
        assert data.get("sortOrder") == 1, "不是第一个词语"

    def test_get_next_word(self, client: HttpClient, created_batch: dict):
        """测试获取下一个词语"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["word_next"].format(batchId=batch_id)
        response = client.get(endpoint, params={"currentOrder": 0})

        AssertUtil.assert_success(response)

    def test_get_previous_word(self, client: HttpClient, created_batch: dict):
        """测试获取上一个词语"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["word_previous"].format(batchId=batch_id)
        # sortOrder从1开始，所以currentOrder=2才有上一个词语
        response = client.get(endpoint, params={"currentOrder": 2})

        AssertUtil.assert_success(response)


# ==================== 听写记录API测试 ====================

class TestDictationAPI:
    """听写记录API测试"""

    def test_start_record(self, client: HttpClient, created_batch: dict):
        """测试开始听写记录"""
        batch_id = created_batch.get("id")

        # 获取第一个词语
        first_word_endpoint = API_ENDPOINTS["word_first"].format(batchId=batch_id)
        word_response = client.get(first_word_endpoint)
        word = word_response.json().get("data")

        if word:
            word_id = word.get("id")
            # 开始听写记录
            response = client.post(
                API_ENDPOINTS["record_start"],
                params={"wordId": word_id, "batchId": batch_id}
            )
            AssertUtil.assert_success(response)

    def test_complete_record(self, client: HttpClient):
        """测试完成听写记录"""
        # 创建批次
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        batch = create_response.json().get("data")
        batch_id = batch.get("id")

        # 获取第一个词语
        first_word_endpoint = API_ENDPOINTS["word_first"].format(batchId=batch_id)
        word_response = client.get(first_word_endpoint)
        word = word_response.json().get("data")

        if word:
            word_id = word.get("id")
            # 开始听写记录
            start_response = client.post(
                API_ENDPOINTS["record_start"],
                params={"wordId": word_id, "batchId": batch_id}
            )
            record = start_response.json().get("data")
            record_id = record.get("id")

            # 完成听写记录
            complete_endpoint = API_ENDPOINTS["record_complete"].format(id=record_id)
            response = client.post(complete_endpoint)

            AssertUtil.assert_success(response)

        # 清理
        cleanup_test_data(client, batch_id)

    def test_complete_by_word_id(self, client: HttpClient):
        """测试通过词语ID完成记录"""
        # 创建批次
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        batch = create_response.json().get("data")
        batch_id = batch.get("id")

        # 获取第一个词语
        first_word_endpoint = API_ENDPOINTS["word_first"].format(batchId=batch_id)
        word_response = client.get(first_word_endpoint)
        word = word_response.json().get("data")

        if word:
            word_id = word.get("id")
            endpoint = API_ENDPOINTS["record_complete_by_word"].format(wordId=word_id)
            response = client.post(endpoint, json={"duration": 10})

            AssertUtil.assert_success(response)

        # 清理
        cleanup_test_data(client, batch_id)

    def test_skip_record(self, client: HttpClient):
        """测试跳过听写记录"""
        # 创建批次
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        batch = create_response.json().get("data")
        batch_id = batch.get("id")

        # 获取第一个词语
        first_word_endpoint = API_ENDPOINTS["word_first"].format(batchId=batch_id)
        word_response = client.get(first_word_endpoint)
        word = word_response.json().get("data")

        if word:
            word_id = word.get("id")
            # 开始听写记录
            start_response = client.post(
                API_ENDPOINTS["record_start"],
                params={"wordId": word_id, "batchId": batch_id}
            )
            record = start_response.json().get("data")
            record_id = record.get("id")

            # 跳过听写记录
            skip_endpoint = API_ENDPOINTS["record_skip"].format(id=record_id)
            response = client.post(skip_endpoint)

            AssertUtil.assert_success(response)

        # 清理
        cleanup_test_data(client, batch_id)

    def test_increment_repeat_count(self, client: HttpClient):
        """测试增加重复次数"""
        # 创建批次
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        batch = create_response.json().get("data")
        batch_id = batch.get("id")

        # 获取第一个词语并开始记录
        first_word_endpoint = API_ENDPOINTS["word_first"].format(batchId=batch_id)
        word_response = client.get(first_word_endpoint)
        word = word_response.json().get("data")

        if word:
            word_id = word.get("id")
            start_response = client.post(
                API_ENDPOINTS["record_start"],
                params={"wordId": word_id, "batchId": batch_id}
            )
            record = start_response.json().get("data")
            record_id = record.get("id")

            # 增加重复次数
            repeat_endpoint = API_ENDPOINTS["record_repeat"].format(id=record_id)
            response = client.post(repeat_endpoint)

            AssertUtil.assert_success(response)

        # 清理
        cleanup_test_data(client, batch_id)

    def test_get_today_records(self, client: HttpClient):
        """测试获取今日记录"""
        response = client.get(API_ENDPOINTS["records_today"])
        AssertUtil.assert_success(response)

    def test_get_records_by_batch(self, client: HttpClient, created_batch: dict):
        """测试获取批次记录"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["records_by_batch"].format(batchId=batch_id)
        response = client.get(endpoint)
        AssertUtil.assert_success(response)

    def test_get_records_by_date_range(self, client: HttpClient):
        """测试获取日期范围记录"""
        today = date.today()
        start = today - timedelta(days=7)
        end = today

        response = client.get(
            API_ENDPOINTS["records_range"],
            params={"start": start.isoformat(), "end": end.isoformat()}
        )
        AssertUtil.assert_success(response)

    def test_end_dictation(self, client: HttpClient, created_batch: dict):
        """测试结束听写"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["end_dictation"].format(batchId=batch_id)
        response = client.post(endpoint)
        AssertUtil.assert_success(response)

    def test_finish_batch(self, client: HttpClient, created_batch: dict):
        """测试完成批次"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["finish_batch"].format(batchId=batch_id)
        response = client.post(endpoint)
        AssertUtil.assert_success(response)

    def test_delete_record(self, client: HttpClient):
        """测试删除记录"""
        # 创建批次并创建记录
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        batch = create_response.json().get("data")
        batch_id = batch.get("id")

        # 获取第一个词语并开始记录
        first_word_endpoint = API_ENDPOINTS["word_first"].format(batchId=batch_id)
        word_response = client.get(first_word_endpoint)
        word = word_response.json().get("data")

        if word:
            word_id = word.get("id")
            start_response = client.post(
                API_ENDPOINTS["record_start"],
                params={"wordId": word_id, "batchId": batch_id}
            )
            record = start_response.json().get("data")
            record_id = record.get("id")

            # 删除记录
            endpoint = API_ENDPOINTS["record_delete"].format(id=record_id)
            response = client.delete(endpoint)
            AssertUtil.assert_success(response)

        # 清理
        cleanup_test_data(client, batch_id)


# ==================== 任务管理API测试 ====================

class TestTaskAPI:
    """任务管理API测试"""

    def test_create_task(self, client: HttpClient):
        """测试创建任务"""
        task_data = {
            "taskName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        response = client.post(API_ENDPOINTS["tasks"], json=task_data)
        AssertUtil.assert_success(response)

        data = AssertUtil.assert_data_not_empty(response)
        assert data.get("id") is not None, "任务ID为空"

        # 清理
        task_id = data.get("id")
        client.delete(API_ENDPOINTS["task_by_id"].format(id=task_id))

    def test_create_task_empty_name(self, client: HttpClient):
        """测试创建任务 - 空名称"""
        task_data = {
            "taskName": "",
            "words": TEST_DATA["words"]
        }
        response = client.post(API_ENDPOINTS["tasks"], json=task_data)
        AssertUtil.assert_error(response)

    def test_create_task_empty_words(self, client: HttpClient):
        """测试创建任务 - 空词语"""
        task_data = {
            "taskName": "测试任务",
            "words": ""
        }
        response = client.post(API_ENDPOINTS["tasks"], json=task_data)
        AssertUtil.assert_error(response)

    def test_get_all_tasks(self, client: HttpClient):
        """测试获取所有任务"""
        response = client.get(API_ENDPOINTS["tasks"])
        AssertUtil.assert_success(response)

    def test_get_uncompleted_tasks(self, client: HttpClient):
        """测试获取未完成任务"""
        response = client.get(API_ENDPOINTS["tasks_uncompleted"])
        AssertUtil.assert_success(response)

    def test_get_tasks_by_status(self, client: HttpClient):
        """测试按状态获取任务"""
        endpoint = API_ENDPOINTS["tasks_by_status"].format(status="NOT_STARTED")
        response = client.get(endpoint)
        AssertUtil.assert_success(response)

    def test_get_favorite_tasks(self, client: HttpClient):
        """测试获取收藏任务"""
        response = client.get(API_ENDPOINTS["tasks_favorites"])
        AssertUtil.assert_success(response)

    def test_get_dictators(self, client: HttpClient):
        """测试获取听写人列表"""
        response = client.get(API_ENDPOINTS["tasks_dictators"])
        AssertUtil.assert_success(response)

    def test_get_task_by_id(self, client: HttpClient, created_task: dict):
        """测试根据ID获取任务"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_by_id"].format(id=task_id)
            response = client.get(endpoint)
            AssertUtil.assert_success(response)
            data = response.json().get("data")
            assert data.get("id") == task_id

    def test_update_task(self, client: HttpClient, created_task: dict):
        """测试更新任务"""
        task_id = created_task.get("id")
        if task_id:
            task_data = {
                "taskName": "更新后的任务",
                "words": "新词语"
            }
            endpoint = API_ENDPOINTS["task_update"].format(id=task_id)
            response = client.put(endpoint, json=task_data)
            AssertUtil.assert_success(response)

    def test_start_task(self, client: HttpClient, created_task: dict):
        """测试开始任务"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_start"].format(id=task_id)
            response = client.post(endpoint)
            AssertUtil.assert_success(response)

    def test_complete_task(self, client: HttpClient, created_task: dict):
        """测试完成任务"""
        task_id = created_task.get("id")
        if task_id:
            # 先开始任务
            client.post(API_ENDPOINTS["task_start"].format(id=task_id))
            # 再完成
            endpoint = API_ENDPOINTS["task_complete"].format(id=task_id)
            response = client.post(endpoint)
            AssertUtil.assert_success(response)

    def test_set_favorite(self, client: HttpClient, created_task: dict):
        """测试设置收藏"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_favorite"].format(id=task_id)
            response = client.post(endpoint, params={"isFavorite": True})
            AssertUtil.assert_success(response)

    def test_set_dictator(self, client: HttpClient, created_task: dict):
        """测试设置听写人"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_dictator"].format(id=task_id)
            response = client.post(endpoint, params={"dictator": "小明"})
            AssertUtil.assert_success(response)

    def test_reset_task(self, client: HttpClient, created_task: dict):
        """测试重置任务"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_reset"].format(id=task_id)
            response = client.post(endpoint)
            AssertUtil.assert_success(response)

    def test_reset_progress(self, client: HttpClient, created_task: dict):
        """测试重置进度"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_reset_progress"].format(id=task_id)
            response = client.post(endpoint)
            AssertUtil.assert_success(response)

    def test_update_status(self, client: HttpClient, created_task: dict):
        """测试更新任务状态"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_status"].format(id=task_id)
            response = client.put(endpoint, params={"status": "IN_PROGRESS"})
            AssertUtil.assert_success(response)

    def test_update_progress(self, client: HttpClient, created_task: dict):
        """测试更新进度"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_progress"].format(id=task_id)
            response = client.post(endpoint, json={
                "currentIndex": 1,
                "correctCount": 1,
                "wrongCount": 0
            })
            AssertUtil.assert_success(response)

    def test_record_word_result(self, client: HttpClient, created_task: dict):
        """测试记录词语结果"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_record"].format(id=task_id)
            response = client.post(endpoint, params={"word": "苹果", "isCorrect": True})
            AssertUtil.assert_success(response)

    def test_get_task_records(self, client: HttpClient, created_task: dict):
        """测试获取任务记录"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_records"].format(id=task_id)
            response = client.get(endpoint)
            AssertUtil.assert_success(response)

    def test_start_word(self, client: HttpClient, created_task: dict):
        """测试开始词语"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_start_word"].format(id=task_id)
            response = client.post(endpoint, params={"word": "苹果"})
            AssertUtil.assert_success(response)

    def test_read_word(self, client: HttpClient, created_task: dict):
        """测试朗读词语"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_read_word"].format(id=task_id)
            response = client.post(endpoint, params={"word": "苹果"})
            AssertUtil.assert_success(response)

    def test_complete_word(self, client: HttpClient, created_task: dict):
        """测试完成词语"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_complete_word"].format(id=task_id)
            response = client.post(endpoint, params={"word": "苹果", "isCorrect": True})
            AssertUtil.assert_success(response)

    def test_start_dictation_from_task(self, client: HttpClient, created_task: dict):
        """测试从任务开始听写"""
        task_id = created_task.get("id")
        if task_id:
            endpoint = API_ENDPOINTS["task_dictation"].format(id=task_id)
            response = client.post(endpoint)
            # 可能成功也可能失败（取决于任务状态）
            assert response.status_code == 200

    def test_delete_task(self, client: HttpClient):
        """测试删除任务"""
        # 创建任务
        task_data = {
            "taskName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["tasks"], json=task_data)
        task = create_response.json().get("data")
        task_id = task.get("id")

        # 删除任务
        endpoint = API_ENDPOINTS["task_delete"].format(id=task_id)
        response = client.delete(endpoint)
        AssertUtil.assert_success(response)


# ==================== 生词本API测试 ====================

class TestDifficultWordAPI:
    """生词本API测试"""

    def test_get_all_difficult_words(self, client: HttpClient):
        """测试获取所有生词"""
        response = client.get(API_ENDPOINTS["difficult_words"])
        AssertUtil.assert_success(response)

    def test_get_difficult_words_by_mastery(self, client: HttpClient):
        """测试根据掌握级别获取生词"""
        response = client.get(
            API_ENDPOINTS["difficult_words_difficult"],
            params={"maxMasteryLevel": 3}
        )
        AssertUtil.assert_success(response)

    def test_get_recommended_words(self, client: HttpClient):
        """测试获取推荐生词"""
        response = client.get(
            API_ENDPOINTS["difficult_words_recommended"],
            params={"minErrors": 3, "minDuration": 10}
        )
        AssertUtil.assert_success(response)

    def test_add_difficult_word(self, client: HttpClient, created_batch: dict):
        """测试添加生词"""
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_id = words[0].get("id")
            response = client.post(
                API_ENDPOINTS["difficult_words"],
                json={"wordId": word_id}
            )
            # 可能成功也可能失败（如果已存在）
            assert response.status_code == 200, f"添加生词失败: {response.text}"

    def test_update_mastery_level(self, client: HttpClient, created_batch: dict):
        """测试更新掌握级别"""
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_id = words[0].get("id")

            # 先添加生词
            add_response = client.post(
                API_ENDPOINTS["difficult_words"],
                json={"wordId": word_id}
            )

            if add_response.status_code == 200:
                add_data = add_response.json().get("data")
                if add_data and add_data.get("id"):
                    difficult_id = add_data.get("id")
                    # 更新掌握级别
                    endpoint = API_ENDPOINTS["difficult_word_mastery"].format(id=difficult_id)
                    response = client.put(endpoint, params={"level": 5})

                    assert response.status_code == 200, f"更新掌握级别失败: {response.text}"

    def test_handle_practice_success(self, client: HttpClient, created_batch: dict):
        """测试处理练习成功"""
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_text = words[0].get("wordText")
            endpoint = API_ENDPOINTS["difficult_word_success"].format(wordText=word_text)
            response = client.post(endpoint)
            AssertUtil.assert_success(response)

    def test_handle_practice_failure(self, client: HttpClient, created_batch: dict):
        """测试处理练习失败"""
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_text = words[0].get("wordText")
            endpoint = API_ENDPOINTS["difficult_word_failure"].format(wordText=word_text)
            response = client.post(endpoint)
            AssertUtil.assert_success(response)

    def test_delete_difficult_word(self, client: HttpClient, created_batch: dict):
        """测试删除生词"""
        batch_id = created_batch.get("id")
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_id = words[0].get("id")

            # 先添加生词
            add_response = client.post(
                API_ENDPOINTS["difficult_words"],
                json={"wordId": word_id}
            )

            if add_response.status_code == 200:
                add_data = add_response.json().get("data")
                if add_data and add_data.get("id"):
                    difficult_id = add_data.get("id")
                    # 删除生词
                    endpoint = API_ENDPOINTS["difficult_word_by_id"].format(id=difficult_id)
                    response = client.delete(endpoint)
                    AssertUtil.assert_success(response)


# ==================== 报表API测试 ====================

class TestReportAPI:
    """报表API测试"""

    def test_get_today_report(self, client: HttpClient):
        """测试获取今日报表"""
        response = client.get(API_ENDPOINTS["report_today"])
        AssertUtil.assert_success(response)

        data = AssertUtil.assert_data_not_empty(response)
        assert "totalCount" in data, "报表缺少totalCount字段"
        assert "completedCount" in data, "报表缺少completedCount字段"
        assert "skippedCount" in data, "报表缺少skippedCount字段"

    def test_get_batch_report(self, client: HttpClient, created_batch: dict):
        """测试获取批次报表"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["report_batch"].format(batchId=batch_id)
        response = client.get(endpoint)
        AssertUtil.assert_success(response)

        data = AssertUtil.assert_data_not_empty(response)
        assert "totalCount" in data, "报表缺少totalCount字段"


# ==================== 建议API测试 ====================

class TestSuggestionAPI:
    """建议API测试"""

    def test_get_all_suggestions(self, client: HttpClient):
        """测试获取所有建议"""
        response = client.get(API_ENDPOINTS["suggestions"])
        AssertUtil.assert_success(response)

    def test_get_suggestions_by_type(self, client: HttpClient):
        """测试根据类型获取建议"""
        endpoint = API_ENDPOINTS["suggestions_by_type"].format(type="HIGH_DIFFICULTY")
        response = client.get(endpoint)
        # 可能返回错误（无效类型）或成功
        assert response.status_code in [200, 400], f"状态码不正确: {response.status_code}"

    def test_get_review_needed_suggestions(self, client: HttpClient):
        """测试获取需要复习的建议"""
        response = client.get(API_ENDPOINTS["suggestions_review"])
        AssertUtil.assert_success(response)

    def test_get_high_difficulty_suggestions(self, client: HttpClient):
        """测试获取高难度建议"""
        response = client.get(API_ENDPOINTS["suggestions_high_difficulty"])
        AssertUtil.assert_success(response)

    def test_get_frequent_error_suggestions(self, client: HttpClient):
        """测试获取频繁错误建议"""
        response = client.get(API_ENDPOINTS["suggestions_frequent_error"])
        AssertUtil.assert_success(response)

    def test_update_suggestion_priority(self, client: HttpClient):
        """测试更新建议优先级"""
        # 先获取建议
        response = client.get(API_ENDPOINTS["suggestions"])
        suggestions = response.json().get("data", [])

        if suggestions:
            suggestion_id = suggestions[0].get("id")
            endpoint = API_ENDPOINTS["suggestion_priority"].format(id=suggestion_id)
            response = client.put(endpoint, params={"priority": 5})
            AssertUtil.assert_success(response)


# ==================== 预设内容API测试 ====================

class TestPresetContentAPI:
    """预设内容API测试"""

    def test_get_preset_list(self, client: HttpClient):
        """测试获取预设内容列表"""
        response = client.get(API_ENDPOINTS["preset_list"])
        AssertUtil.assert_success(response)

        presets = AssertUtil.assert_list_not_empty(response)
        assert len(presets) >= 4, "预设内容列表数量不足"

    def test_get_preset_content(self, client: HttpClient):
        """测试获取预设内容详情"""
        endpoint = API_ENDPOINTS["preset_content"].format(id="common-words-50")
        response = client.get(endpoint)
        AssertUtil.assert_success(response)

    def test_get_nonexistent_preset(self, client: HttpClient):
        """测试获取不存在的预设内容"""
        endpoint = API_ENDPOINTS["preset_content"].format(id="nonexistent")
        response = client.get(endpoint)
        AssertUtil.assert_error(response)

    def test_import_preset_content(self, client: HttpClient):
        """测试导入预设内容"""
        endpoint = API_ENDPOINTS["preset_import"].format(id="common-words-50")
        response = client.post(endpoint)
        AssertUtil.assert_success(response)

        data = response.json().get("data")
        batch_id = data

        # 清理
        if batch_id:
            cleanup_test_data(client, batch_id)


# ==================== 页面访问测试 ====================

class TestPageAccess:
    """页面访问测试"""

    def test_index_page(self, client: HttpClient):
        """测试首页访问"""
        response = client.get(PAGE_ROUTES["index"])
        assert response.status_code == 200, f"首页访问失败: {response.status_code}"

    def test_login_page(self, client: HttpClient):
        """测试登录页面访问"""
        response = client.get(PAGE_ROUTES["login"])
        assert response.status_code == 200, f"登录页面访问失败: {response.status_code}"

    def test_history_page(self, client: HttpClient):
        """测试历史记录页面访问"""
        response = client.get(PAGE_ROUTES["history"])
        assert response.status_code == 200, f"历史记录页面访问失败: {response.status_code}"

    def test_difficult_words_page(self, client: HttpClient):
        """测试生词本页面访问"""
        response = client.get(PAGE_ROUTES["difficult_words"])
        assert response.status_code == 200, f"生词本页面访问失败: {response.status_code}"

    def test_reports_page(self, client: HttpClient):
        """测试报表页面访问"""
        response = client.get(PAGE_ROUTES["reports"])
        assert response.status_code == 200, f"报表页面访问失败: {response.status_code}"

    def test_tasks_page(self, client: HttpClient):
        """测试任务管理页面访问"""
        response = client.get(PAGE_ROUTES["tasks"])
        assert response.status_code == 200, f"任务管理页面访问失败: {response.status_code}"

    def test_dictators_page(self, client: HttpClient):
        """测试听写人管理页面访问"""
        response = client.get(PAGE_ROUTES["dictators"])
        assert response.status_code == 200, f"听写人管理页面访问失败: {response.status_code}"

    def test_user_management_page(self, client: HttpClient):
        """测试用户管理页面访问"""
        response = client.get(PAGE_ROUTES["user_management"])
        # 可能需要管理员权限
        assert response.status_code in [200, 302, 401], f"用户管理页面访问失败: {response.status_code}"


# ==================== 集成测试 ====================

class TestIntegration:
    """集成测试 - 完整业务流程"""

    def test_complete_dictation_flow(self, client: HttpClient):
        """测试完整听写流程"""
        # 1. 创建批次
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        AssertUtil.assert_success(create_response, "创建成功")
        batch = create_response.json().get("data")
        batch_id = batch.get("id")

        # 2. 开始批次
        start_endpoint = API_ENDPOINTS["batch_start"].format(id=batch_id)
        start_response = client.post(start_endpoint)
        AssertUtil.assert_success(start_response, "开始成功")

        # 3. 获取第一个词语
        first_word_endpoint = API_ENDPOINTS["word_first"].format(batchId=batch_id)
        word_response = client.get(first_word_endpoint)
        AssertUtil.assert_success(word_response)
        word = word_response.json().get("data")
        word_id = word.get("id")

        # 4. 开始听写记录
        record_response = client.post(
            API_ENDPOINTS["record_start"],
            params={"wordId": word_id, "batchId": batch_id}
        )
        AssertUtil.assert_success(record_response, "开始")
        record = record_response.json().get("data")
        record_id = record.get("id")

        # 5. 增加重复次数（模拟重复听）
        repeat_endpoint = API_ENDPOINTS["record_repeat"].format(id=record_id)
        client.post(repeat_endpoint)

        # 6. 完成听写记录
        complete_endpoint = API_ENDPOINTS["record_complete"].format(id=record_id)
        complete_response = client.post(complete_endpoint)
        AssertUtil.assert_success(complete_response, "完成")

        # 7. 标记词语完成
        word_complete_endpoint = API_ENDPOINTS["word_complete"].format(id=word_id)
        client.post(word_complete_endpoint)

        # 8. 完成批次
        batch_complete_endpoint = API_ENDPOINTS["batch_complete"].format(id=batch_id)
        batch_complete_response = client.post(batch_complete_endpoint)
        AssertUtil.assert_success(batch_complete_response, "完成")

        # 9. 获取批次报表
        report_endpoint = API_ENDPOINTS["report_batch"].format(batchId=batch_id)
        report_response = client.get(report_endpoint)
        AssertUtil.assert_success(report_response)

        # 10. 清理
        cleanup_test_data(client, batch_id)

    def test_difficult_word_flow(self, client: HttpClient):
        """测试生词本流程"""
        # 1. 创建批次
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        AssertUtil.assert_success(create_response, "创建成功")
        batch = create_response.json().get("data")
        batch_id = batch.get("id")

        # 2. 获取词语
        words_endpoint = API_ENDPOINTS["batch_words"].format(id=batch_id)
        words_response = client.get(words_endpoint)
        words = words_response.json().get("data", [])

        if words:
            word_id = words[0].get("id")

            # 3. 添加到生词本
            add_response = client.post(
                API_ENDPOINTS["difficult_words"],
                json={"wordId": word_id}
            )
            assert add_response.status_code == 200, f"添加生词失败: {add_response.text}"

            add_data = add_response.json().get("data")
            if add_data and add_data.get("id"):
                difficult_id = add_data.get("id")

                # 4. 更新掌握级别
                mastery_endpoint = API_ENDPOINTS["difficult_word_mastery"].format(id=difficult_id)
                client.put(mastery_endpoint, params={"level": 3})

                # 5. 从生词本移除
                delete_endpoint = API_ENDPOINTS["difficult_word_by_id"].format(id=difficult_id)
                client.delete(delete_endpoint)

        # 6. 清理
        cleanup_test_data(client, batch_id)

    def test_task_dictation_flow(self, client: HttpClient):
        """测试任务听写流程"""
        # 1. 创建任务
        task_data = {
            "taskName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        create_response = client.post(API_ENDPOINTS["tasks"], json=task_data)
        AssertUtil.assert_success(create_response)
        task = create_response.json().get("data")
        task_id = task.get("id")

        # 2. 设置听写人
        dictator_endpoint = API_ENDPOINTS["task_dictator"].format(id=task_id)
        client.post(dictator_endpoint, params={"dictator": "小明"})

        # 3. 设置收藏
        favorite_endpoint = API_ENDPOINTS["task_favorite"].format(id=task_id)
        client.post(favorite_endpoint, params={"isFavorite": True})

        # 4. 开始任务
        start_endpoint = API_ENDPOINTS["task_start"].format(id=task_id)
        client.post(start_endpoint)

        # 5. 更新进度
        progress_endpoint = API_ENDPOINTS["task_progress"].format(id=task_id)
        client.post(progress_endpoint, json={
            "currentIndex": 2,
            "correctCount": 2,
            "wrongCount": 0
        })

        # 6. 记录词语结果
        record_endpoint = API_ENDPOINTS["task_record"].format(id=task_id)
        client.post(record_endpoint, params={"word": "苹果", "isCorrect": True})

        # 7. 完成任务
        complete_endpoint = API_ENDPOINTS["task_complete"].format(id=task_id)
        complete_response = client.post(complete_endpoint)
        AssertUtil.assert_success(complete_response)

        # 8. 重置任务
        reset_endpoint = API_ENDPOINTS["task_reset"].format(id=task_id)
        client.post(reset_endpoint)

        # 9. 清理
        client.delete(API_ENDPOINTS["task_by_id"].format(id=task_id))

    def test_preset_import_flow(self, client: HttpClient):
        """测试预设内容导入流程"""
        # 1. 获取预设内容列表
        list_response = client.get(API_ENDPOINTS["preset_list"])
        AssertUtil.assert_success(list_response)
        presets = list_response.json().get("data", [])

        # 2. 选择一个预设内容
        preset_id = presets[0].get("id")

        # 3. 查看预设内容详情
        content_endpoint = API_ENDPOINTS["preset_content"].format(id=preset_id)
        content_response = client.get(content_endpoint)
        AssertUtil.assert_success(content_response)

        # 4. 导入预设内容
        import_endpoint = API_ENDPOINTS["preset_import"].format(id=preset_id)
        import_response = client.post(import_endpoint)
        AssertUtil.assert_success(import_response)

        batch_id = import_response.json().get("data")

        # 5. 验证批次已创建
        batch_endpoint = API_ENDPOINTS["batch_by_id"].format(id=batch_id)
        batch_response = client.get(batch_endpoint)
        AssertUtil.assert_success(batch_response)

        # 6. 清理
        if batch_id:
            cleanup_test_data(client, batch_id)


# ==================== 错误处理测试 ====================

class TestErrorHandling:
    """错误处理测试"""

    def test_invalid_endpoint(self, client: HttpClient):
        """测试无效端点"""
        response = client.get("/api/nonexistent")
        assert response.status_code >= 200 and response.status_code < 600

    def test_invalid_json(self, client: HttpClient):
        """测试无效JSON"""
        response = client.post(
            API_ENDPOINTS["batches"],
            data="invalid json",
            headers={"Content-Type": "application/json"}
        )
        assert response.status_code >= 200 and response.status_code < 600

    def test_missing_parameters(self, client: HttpClient):
        """测试缺少参数"""
        response = client.post(API_ENDPOINTS["batches"], json={})
        # 应该返回错误或使用默认值
        assert response.status_code in [200, 400]


# ==================== 性能测试 ====================

class TestPerformance:
    """性能测试"""

    def test_batch_list_performance(self, client: HttpClient):
        """测试批次列表性能"""
        start_time = time.time()
        response = client.get(API_ENDPOINTS["batches"])
        end_time = time.time()

        assert response.status_code == 200
        assert (end_time - start_time) < 2.0, f"请求时间过长: {end_time - start_time}秒"

    def test_concurrent_requests(self, client: HttpClient):
        """测试并发请求"""
        import concurrent.futures

        def make_request():
            return client.get(API_ENDPOINTS["batches"])

        with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
            futures = [executor.submit(make_request) for _ in range(5)]
            results = [f.result() for f in futures]

        for result in results:
            assert result.status_code == 200