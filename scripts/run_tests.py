#!/usr/bin/env python3
"""
E2E测试运行脚本
启动测试并生成HTML报告
"""
import os
import sys
import argparse
import subprocess
from datetime import datetime
from pathlib import Path

# 添加当前目录到Python路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from config import REPORT_DIR, BASE_URL


def ensure_report_dir():
    """确保报告目录存在"""
    report_path = Path(REPORT_DIR)
    report_path.mkdir(parents=True, exist_ok=True)
    return report_path


def install_dependencies():
    """安装依赖"""
    requirements_path = os.path.join(os.path.dirname(__file__), "requirements.txt")
    print("正在安装依赖...")
    subprocess.run(
        [sys.executable, "-m", "pip", "install", "-r", requirements_path],
        check=True
    )
    print("依赖安装完成")


def run_tests(
    verbose: bool = False,
    html_report: bool = True,
    coverage: bool = False,
    markers: str = None,
    test_file: str = None
):
    """运行测试"""
    report_path = ensure_report_dir()
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

    # 构建pytest命令
    pytest_cmd = [sys.executable, "-m", "pytest"]

    # 添加测试文件或目录
    if test_file:
        pytest_cmd.append(os.path.join(os.path.dirname(__file__), test_file))
    else:
        pytest_cmd.append(os.path.join(os.path.dirname(__file__), "e2e_test.py"))

    # 添加详细输出
    if verbose:
        pytest_cmd.append("-v")

    # 添加HTML报告
    if html_report:
        html_report_path = report_path / f"e2e_report_{timestamp}.html"
        pytest_cmd.extend([
            f"--html={html_report_path}",
            "--self-contained-html"
        ])

    # 添加覆盖率报告
    if coverage:
        coverage_report_path = report_path / f"coverage_{timestamp}"
        pytest_cmd.extend([
            f"--cov-report=html:{coverage_report_path}",
            "--cov-report=term"
        ])

    # 添加标记过滤
    if markers:
        pytest_cmd.extend(["-m", markers])

    # 添加其他选项
    pytest_cmd.extend([
        "--tb=short",
        "-W", "ignore::DeprecationWarning"
    ])

    print(f"测试服务器地址: {BASE_URL}")
    print(f"报告输出目录: {report_path}")
    print(f"运行命令: {' '.join(pytest_cmd)}")
    print("-" * 60)

    # 运行测试
    result = subprocess.run(pytest_cmd)

    print("-" * 60)
    if html_report:
        print(f"HTML报告: {html_report_path}")
    if coverage:
        print(f"覆盖率报告: {coverage_report_path}")

    return result.returncode


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description="E2E测试运行脚本")
    parser.add_argument(
        "-v", "--verbose",
        action="store_true",
        help="详细输出"
    )
    parser.add_argument(
        "--no-html",
        action="store_true",
        help="不生成HTML报告"
    )
    parser.add_argument(
        "--coverage",
        action="store_true",
        help="生成覆盖率报告"
    )
    parser.add_argument(
        "-m", "--markers",
        type=str,
        help="运行指定标记的测试"
    )
    parser.add_argument(
        "-f", "--file",
        type=str,
        help="运行指定测试文件"
    )
    parser.add_argument(
        "--install-deps",
        action="store_true",
        help="安装依赖"
    )
    parser.add_argument(
        "--url",
        type=str,
        help="测试服务器URL (覆盖环境变量)"
    )

    args = parser.parse_args()

    # 设置服务器URL
    if args.url:
        os.environ["BASE_URL"] = args.url

    # 安装依赖
    if args.install_deps:
        install_dependencies()

    # 运行测试
    exit_code = run_tests(
        verbose=args.verbose,
        html_report=not args.no_html,
        coverage=args.coverage,
        markers=args.markers,
        test_file=args.file
    )

    sys.exit(exit_code)


if __name__ == "__main__":
    main()