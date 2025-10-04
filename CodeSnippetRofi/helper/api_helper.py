import json
import subprocess

from common.constant import code_snippet_port, action_list, action_edit, action_delete, action_add
from entity.response import SearchResponse, NormalResponse


def _run_api(action: str, data: str) -> dict:
    action = action.upper()
    if action not in ["LIST", "ADD", "EDIT", "DELETE"]:
        raise ValueError("Invalid action")

    message = {
        "action": f"{action}",
        "data": f"{data}"
    }

    # 使用 Popen 创建管道
    echo_process = subprocess.Popen(
        ["echo", json.dumps(message)],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )

    nc_process = subprocess.Popen(
        ["nc", "-q", "0", "127.0.0.1", code_snippet_port],
        stdin=echo_process.stdout,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )

    # 关闭 echo 的输出，避免资源泄漏
    echo_process.stdout.close()

    try:
        # 获取 nc 的输出
        nc_stdout, nc_stderr = nc_process.communicate()
        return dict(json.loads(nc_stdout))
    except Exception:
        raise ConnectionError(f"后端交互失败!检查守护进程是否运行")


def run_search(key: str) -> SearchResponse:
    return SearchResponse(_run_api(action_list, key))


def run_edit(data: str) -> NormalResponse:
    return NormalResponse(_run_api(action_edit, data))


def run_delete(path: str) -> NormalResponse:
    return NormalResponse(_run_api(action_delete, path))


def run_add(data: str) -> NormalResponse:
    return NormalResponse(_run_api(action_add, data))
