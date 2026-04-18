# E2E 测试说明

## 目录结构

```
e2e_test/
├── conftest.py          ← pytest 全局配置与 fixture
├── requirements.txt     ← Python 依赖
├── config/
│   └── base_config.py   ← 基础配置（BASE_URL 等）
├── utils/
│   └── http_client.py   ← HTTP 请求客户端
└── tests/
    └── test_api.py      ← API 测试用例
```

## 运行测试

```bash
# 安装依赖
pip install -r requirements.txt

# 运行全部测试
BASE_URL=http://localhost:8080 pytest tests/ -v

# 生成 HTML 报告
pytest tests/ -v --html=reports/e2e_report.html
```