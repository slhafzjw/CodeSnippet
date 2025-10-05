#!/usr/bin/env python3
import hashlib
import json
import os
import re
import shutil
import subprocess
import tempfile
from pathlib import Path

from CodeSnippetRofi.common.constant import editor_class, code_snippet_editor

from common.constant import template_add, template_edit
from entity.result import ActionResult
from helper.api_helper import run_edit, run_add


def _parse_add_text(text: str) -> str:
    # 提取第一个代码块（包含可选的 ```[Lang] 或 ```Lang）
    fence_re = re.search(r"```(?:\[(?P<fence_lang>[^\]]*)\]|(?P<fence_lang2>[A-Za-z0-9_+\-]+))?\s*\r?\n(?P<code>[\s\S]*?)```", text)
    fence_lang = ''
    content = ''
    if fence_re:
        fence_lang = (fence_re.group('fence_lang') or fence_re.group('fence_lang2') or '') or ''
        content = (fence_re.group('code') or '').strip()
        # 把模板占位符当做空值处理
        if fence_lang.strip().lower() == 'language' or fence_lang.strip() == '':
            fence_lang = ''
        else:
            fence_lang = fence_lang.strip()

    # 定位 MetaData 区块（从"## MetaData"到文档末尾或下一个"## "）
    meta_match = re.search(r"##\s*MetaData\s*(?P<meta>[\s\S]*)", text, flags=re.I)
    meta = meta_match.group('meta') if meta_match else ''
    meta_lines = meta.splitlines()

    def extract_list_value(field_name: str) -> str:
        field_name_re = re.compile(rf"^\s*-\s*{re.escape(field_name)}\s*$", flags=re.I)
        list_item_re = re.compile(r"^\s*-\s*(.*)$")
        for i, line in enumerate(meta_lines):
            if field_name_re.match(line):
                # 找下一条非空行作为子项
                j = i + 1
                while j < len(meta_lines) and meta_lines[j].strip() == "":
                    j += 1
                if j < len(meta_lines):
                    m = list_item_re.match(meta_lines[j])
                    if m:
                        val = m.group(1).strip()
                        # 把模板占位符当做空值处理
                        if val.lower() == field_name.lower() or val.lower() == 'language' and field_name.lower() == 'language' and val == '':
                            return ''
                        return val
                return ""
        return ""

    name = extract_list_value("Name")
    language_meta = extract_list_value("Language")

    # 优先使用 MetaData 下方的 Language，若无再使用代码块 fence 的语言
    language = language_meta or fence_lang or ""

    entity = {
        "name": name or "",
        "language": language or "",
        "content": content or "",
    }
    return json.dumps(entity, ensure_ascii=False)

# 包装成读取文件的版本（保留原函数签名）
def _parse_add(file_path: str) -> str:
    text = Path(file_path).read_text(encoding='utf-8')
    return _parse_add_text(text)


def _prefill_edit(file_path: str, source_path: str, _id: str) -> None:
    # 将原始文件内容与元信息写入模板占位
    # 读取原文件
    src = Path(source_path).read_text(encoding='utf-8')

    # 提取代码块（仅代码，不带多余模板）与语言
    code_block = re.search(r"```(?:\[([^\]]*)\]|([A-Za-z0-9_+\-]+))?\s*\r?\n([\s\S]*?)```", src)
    language = ''
    code_only = ''
    if code_block:
        language = (code_block.group(1) or code_block.group(2) or '').strip()
        code_only = (code_block.group(3) or '').strip()
    # 若未检测到代码块则回退为全文
    if not code_only:
        code_only = src.strip()

    # 从源文档的 MetaData 段提取 tags 与 description
    tags: list[str] = []
    desc = ''
    # 定位 MetaData 段
    meta_match = re.search(r"##\s*MetaData([\s\S]*)", src)
    meta = meta_match.group(1) if meta_match else ''
    if meta:
        # Tags 段
        tags_section = re.search(r"-\s*Tags\s*([\s\S]*?)(?:\n-\s*Description|\Z)", meta)
        if tags_section:
            for line in tags_section.group(1).splitlines():
                m = re.match(r"^\s*-\s*(.+)$", line)
                if m:
                    val = m.group(1).strip()
                    if val:
                        tags.append(val)
        # Description 段
        desc_section = re.search(r"-\s*Description\s*\n\s*-\s*(.+)", meta)
        if desc_section:
            desc = desc_section.group(1).strip()

    # 若语言未能从代码块检测，则用路径上级名兜底
    if not language:
        language = Path(source_path).parent.name

    # 重建模板（覆盖写入）
    rebuilt_lines = []
    rebuilt_lines.append("## Snippet\n\n")
    if language:
        rebuilt_lines.append(f"```{language}\n")
    else:
        rebuilt_lines.append("```\n")
    rebuilt_lines.append(code_only)
    rebuilt_lines.append("\n```\n\n")
    rebuilt_lines.append("## MetaData\n\n")
    rebuilt_lines.append("- Tags\n")
    if tags:
        for t in tags:
            rebuilt_lines.append(f"  - {t}\n")
    else:
        rebuilt_lines.append("  - \n  - \n")
    rebuilt_lines.append("- Description\n")
    rebuilt_lines.append(f"  - {desc}\n")

    Path(file_path).write_text(''.join(rebuilt_lines), encoding='utf-8')


def _parse_edit(file_path: str, _id: str, source_path: str) -> str:
    text = Path(file_path).read_text(encoding='utf-8')
    # 解析语言（不强制需要）
    # 解析代码块内容
    code_match = re.search(r"```(?:\[[^\]]*\]|[A-Za-z0-9_+\-]*)\s*\r?\n([\s\S]*?)```", text)
    content = (code_match.group(1) if code_match else '').strip()
    # tags
    tag_lines = re.findall(r"^-\s+(.*)$", text, flags=re.M)
    # 在 '- Tags' 之后的两级缩进项
    tags_section = False
    tags = []
    for line in text.splitlines():
        if re.match(r"^-\s*Tags\s*$", line):
            tags_section = True
            continue
        if tags_section:
            m = re.match(r"^\s*-\s*(.*)$", line)
            if m:
                val = m.group(1).strip()
                if val:
                    tags.append(val)
            else:
                # 离开 tags 段
                tags_section = False
        # 到 Description 再退出
        if re.match(r"^-\s*Description\s*$", line):
            tags_section = False

    desc_match = re.search(r"-\s*Description\s*\n\s*-\s*(.*)", text)
    description = (desc_match.group(1) if desc_match else '').strip()

    entity = {
        "id": _id,
        "path": source_path,
        "tags": tags,
        "description": description,
        "content": content,
    }
    return json.dumps(entity, ensure_ascii=False)


def _create_tmp(content: str) -> str:
    # 创建临时文件，并写入template_path对应的文件中的内容，最终返回临时文件的路径
    # 临时文件路径固定为/tmp/<随机字符串>.md
    with tempfile.NamedTemporaryFile(prefix='', suffix='.md', dir='/tmp/', delete=False) as tmp_file:
        temp_path = tmp_file.name

    # 将模板文件内容复制到临时文件
    Path(temp_path).write_text(content, encoding='utf-8')

    return temp_path


def _open_with_nvim(file_path: str) -> None:
    # 获取终端环境变量，默认为xterm
    term = os.environ.get('TERMINAL', '')
    if not term:
        term = 'xterm'

    # 获取终端名称
    name = os.path.basename(term)

    # 根据不同终端类型构建命令
    cmd = [term]

    # 根据终端类型添加特定参数
    if name == 'alacritty':
        cmd.extend(['--class', editor_class, '-e', code_snippet_editor, file_path])
    elif name == 'kitty':
        cmd.extend(['--class', editor_class, code_snippet_editor, file_path])
    elif name == 'foot':
        cmd.extend(['-a', editor_class, code_snippet_editor, file_path])
    elif name == 'wezterm':
        cmd.extend(['start', '--class', editor_class, '--', code_snippet_editor, file_path])
    elif name.startswith('gnome-terminal'):
        cmd.extend([f'--class={editor_class}', '--', code_snippet_editor, file_path])
    elif name == 'konsole':
        cmd.extend(['--class', editor_class, '-e', code_snippet_editor, file_path])
    elif name == 'urxvt':
        cmd.extend(['-name', editor_class, '-e', code_snippet_editor, file_path])
    elif name == 'xterm':
        cmd.extend(['-class', editor_class, '-e', code_snippet_editor, file_path])
    else:
        cmd.extend(['-e', code_snippet_editor, file_path])

    # 阻塞执行命令，直到进程结束
    subprocess.run(cmd)


def _copy(tmp_path: str) -> None:
    # 读取临时文件内容并提取代码块
    text = Path(tmp_path).read_text(encoding='utf-8')
    m = re.search(r"```(?:\[[^\]]*\]|[A-Za-z0-9_+\-]*)\s*\r?\n([\s\S]*?)```", text)
    if not m:
        code = ""
    else:
        code = m.group(1).strip()

        # 检查是否有内容
    if not code:
        Path(tmp_path).unlink(missing_ok=True)
        return

        # 检查是否有 wl-copy 命令
    if not shutil.which('wl-copy'):
        print("Error: 未安装 wl-copy")
        Path(tmp_path).unlink(missing_ok=True)
        return

        # 复制到剪贴板
    process = subprocess.Popen(['wl-copy'], stdin=subprocess.PIPE, text=True)
    process.communicate(input=code)


def _calculate_file_sha256(file_path):
    """
    计算文件的SHA256哈希值

    Args:
        file_path (str): 文件路径

    Returns:
        str: 文件的SHA256哈希值（十六进制字符串）
    """
    # 创建SHA256哈希对象
    sha256_hash = hashlib.sha256()

    # 以二进制模式打开文件
    with open(file_path, 'rb') as file:
        # 分块读取文件，避免内存问题
        for byte_block in iter(lambda: file.read(4096), b""):
            sha256_hash.update(byte_block)

    # 返回十六进制格式的哈希值
    return sha256_hash.hexdigest()

def edit_and_copy(path: str, id: str) -> ActionResult:
    tmp_path = _create_tmp(template_edit)
    try:
        _prefill_edit(tmp_path, path, id)
        _open_with_nvim(tmp_path)
        _copy(tmp_path)
        # 清理临时文件
        return ActionResult(True, "")
    except Exception as e:
        print(f"Error: {e}")
        return ActionResult(False, str(e))
    finally:
        Path(tmp_path).unlink(missing_ok=True)

def edit(path: str, id: str) -> ActionResult:
    tmp_path = _create_tmp(template_edit)
    primary_hash = _calculate_file_sha256(path)
    try:
        _prefill_edit(tmp_path, path, id)
        _open_with_nvim(tmp_path)
        new_hash = _calculate_file_sha256(path)
        if primary_hash == new_hash:
            return ActionResult(True, "文件未修改")
        edit_json = _parse_edit(tmp_path, id, path)
        response = run_edit(edit_json)
        if response.status == "SUCCESS":
            return ActionResult(True, response.data)
        else:
            return ActionResult(False, response.data)
    except Exception as e:
        print(f"Error: {e}")
        return ActionResult(False, str(e))
    finally:
        Path(tmp_path).unlink(missing_ok=True)


def add() -> ActionResult:
    tmp_path = _create_tmp(template_add)
    primary_hash = _calculate_file_sha256(tmp_path)
    try:
        _open_with_nvim(tmp_path)
        new_hash = _calculate_file_sha256(tmp_path)
        if primary_hash == new_hash:
            return ActionResult(True, "内容为空")
        add_json = _parse_add(tmp_path)
        response = run_add(add_json)
        if response.status == "SUCCESS":
            return ActionResult(True, response.data)
        else:
            return ActionResult(False, response.data)
    except Exception as e:
        print(f"Error: {e}")
        return ActionResult(False, str(e))
    finally:
        Path(tmp_path).unlink(missing_ok=True)


if __name__ == '__main__':
    tmp_path = _create_tmp(template_add)
    _open_with_nvim(tmp_path)
    add_json = _parse_add(tmp_path)
    print(add_json)
