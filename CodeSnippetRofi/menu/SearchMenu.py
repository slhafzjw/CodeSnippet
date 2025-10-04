import rofi

from entity.response import SearchResponse
from helper.api_helper import run_search, run_delete
from helper.file_helper import edit, edit_and_copy


class SearchMenu:
    def __init__(self, r: rofi.Rofi):
        self._r = r

    def run(self):
        while True:
            key = self._r.text_entry("搜索", allow_blank=True)
            if key is None:
                break
            print(key)
            try:
                res = run_search(key)
            except Exception as e:
                print(f"Error: {e}")
                self._r.error(str(e))
                return
            if res.status != "SUCCESS":
                self._r.error(res.data)
                return
            SearchResultMenu(self._r, res.data).run()


class SearchResultMenu:
    def __init__(self, r: rofi.Rofi, data: list[SearchResponse.SearchData]):
        self._r = r
        self._data = data

    def run(self):
        while True:
            if len(self._data) == 0:
                self._r.select("搜索结果", ["未找到Snippet记录"])
                break
            else:
                # 将data按照索引和path换成dict，前者为键
                options = [f"{d.name} - {d.path}" for d in self._data]
                index, key = self._r.select("搜索结果", options)
                if key == -1:
                    break
                SearchResultActionMenu(self._r, self._data[index]).run()


class SearchResultActionMenu:
    def __init__(self, r: rofi.Rofi, data: SearchResponse.SearchData):
        self._r = r
        self.data = data

    def run(self):
        while True:
            index, key = self._r.select("操作", ["编辑", "编辑并复制", "删除"])
            if key == -1:
                break
            match index:
                case 0:
                    result = edit(self.data.path, self.data.id)
                    if not result.ok:
                        self._r.error(result.message)
                case 1:
                    result = edit_and_copy(self.data.path, self.data.id)
                    if not result.ok:
                        self._r.error(result.message)
                    exit()
                case 2:
                    run_delete(self.data.path)
            break
