"""
E2E测试主程序
"""
import pytest
import time
from datetime import date, timedelta
from typing import Generator

from config import (
    BASE_URL,
    API_ENDPOINTS,
    PAGE_ROUTES,
    TIMEOUT,
    TEST_DATA,
)
from utils import (
    HttpClient,
    AssertUtil,
    DataGenerator,
    wait_for_server,
    cleanup_test_data,
)


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


@pytest.fixture
def created_batch(client: HttpClient) -> Generator[dict, None, None]:
    """创建测试批次并清理"""
    batch_data = {
        "batchName": DataGenerator.random_batch_name(),
        "words": TEST_DATA["words"]
    }
    response = client.post(API_ENDPOINTS["batches"], json=batch_data)
    AssertUtil.assert_success(response, "创建成功")

    batch = response.json().get("data")
    batch_id = batch.get("id")

    yield batch

    # 清理
    cleanup_test_data(client, batch_id)


# ==================== 健康检查测试 ====================

class TestServerHealth:
    """服务器健康检查测试"""

    def test_server_is_running(self, client: HttpClient, setup_server: None):
        """测试服务器是否运行"""
        response = client.get("/")
        assert response.status_code == 200, f"服务器未运行, 状态码: {response.status_code}"

    def test_api_health_check(self, client: HttpClient, setup_server: None):
        """测试API健康检查"""
        response = client.get(API_ENDPOINTS["batches"])
        assert response.status_code == 200, f"API不可用, 状态码: {response.status_code}"


# ==================== 批次管理API测试 ====================

class TestBatchAPI:
    """批次管理API测试"""

    def test_create_batch(self, client: HttpClient):
        """测试创建批次"""
        batch_data = {
            "batchName": DataGenerator.random_batch_name(),
            "words": TEST_DATA["words"]
        }
        response = client.post(API_ENDPOINTS["batches"], json=batch_data)
        AssertUtil.assert_success(response, "创建成功")

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

    def test_start_batch(self, client: HttpClient, created_batch: dict):
        """测试开始批次"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["batch_start"].format(id=batch_id)
        response = client.post(endpoint)

        AssertUtil.assert_success(response, "开始成功")
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

        AssertUtil.assert_success(response, "完成")
        data = response.json().get("data")
        assert data.get("status") == "COMPLETED", "批次状态不是已完成"

        # 清理
        cleanup_test_data(client, batch_id)

    def test_cancel_batch(self, client: HttpClient, created_batch: dict):
        """测试取消批次"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["batch_cancel"].format(id=batch_id)
        response = client.post(endpoint)

        AssertUtil.assert_success(response, "取消")
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

        AssertUtil.assert_success(response, "重置")

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

        AssertUtil.assert_success(response, "删除成功")


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

            AssertUtil.assert_success(response, "更新成功")

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

            AssertUtil.assert_success(response, "更新成功")

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

            AssertUtil.assert_success(response, "完成")

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

            AssertUtil.assert_success(response, "跳过")

    def test_get_first_word(self, client: HttpClient, created_batch: dict):
        """测试获取批次第一个词语"""
        batch_id = created_batch.get("id")
        endpoint = API_ENDPOINTS["word_first"].format(batchId=batch_id)
        response = client.get(endpoint)

        AssertUtil.assert_success(response)
        data = AssertUtil.assert_data_not_empty(response)
        assert data.get("sortOrder") == 0, "不是第一个词语"

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
        response = client.get(endpoint, params={"currentOrder": 1})

        AssertUtil.assert_success(response)


# ==================== 听写流程API测试 ====================

class TestDictationAPI:
    """听写流程API测试"""

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
            AssertUtil.assert_success(response, "开始")

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

            AssertUtil.assert_success(response, "完成")

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

            AssertUtil.assert_success(response, "跳过")

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

            AssertUtil.assert_success(response, "重复")

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


# ==================== 页面访问测试 ====================

class TestPageAccess:
    """页面访问测试"""

    def test_index_page(self, client: HttpClient):
        """测试首页访问"""
        response = client.get(PAGE_ROUTES["index"])
        assert response.status_code == 200, f"首页访问失败: {response.status_code}"

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
                delete_endpoint = API_ENDPOINTS["difficult_words"] + f"/{difficult_id}"
                client.delete(delete_endpoint)

        # 6. 清理
        cleanup_test_data(client, batch_id)