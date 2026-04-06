#!/usr/bin/env python3
"""
测试运行脚本
运行所有E2E测试并生成报告
"""
import os
import sys
import subprocess
from datetime import datetime

def main():
    # 获取脚本目录
    script_dir = os.path.dirname(os.path.abspath(__file__))
    report_dir = os.path.join(os.path.dirname(script_dir), "report")

    # 确保报告目录存在
    os.makedirs(report_dir, exist_ok=True)

    # 设置报告文件名（带时间戳）
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    report_file = os.path.join(report_dir, f"e2e_report_{timestamp}.html")

    print("=" * 60)
    print("小学生听写助手 - E2E测试")
    print("=" * 60)
    print(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"报告目录: {report_dir}")
    print()

    # 检查依赖
    print("检查依赖...")
    try:
        import requests
        import pytest
        print("✓ 依赖已安装")
    except ImportError as e:
        print(f"✗ 缺少依赖: {e}")
        print("请运行: pip install -r requirements.txt")
        sys.exit(1)

    # 运行测试
    print()
    print("运行测试...")
    print("-" * 60)

    test_file = os.path.join(script_dir, "e2e_test.py")
    result = subprocess.run([
        sys.executable, "-m", "pytest",
        test_file,
        "-v",
        f"--html={report_file}",
        "--self-contained-html"
    ])

    print("-" * 60)
    print()

    if result.returncode == 0:
        print("✓ 所有测试通过!")
        print(f"报告已生成: {report_file}")
    else:
        print("✗ 部分测试失败")
        print(f"请查看报告: {report_file}")

    return result.returncode


if __name__ == "__main__":
    sys.exit(main())