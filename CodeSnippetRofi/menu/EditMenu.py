import rofi

from helper.api_helper import run_search
from helper.file_helper import edit


class EditMenu:
    def __init__(self,r: rofi.Rofi):
        self._r = r

    def run(self):
        try:
            result = run_search("")
        except Exception as e:
            print(f"Error: {e}")
            self._r.error(str(e))
            return
        if result.status != "SUCCESS":
            self._r.error(result.data)
            return
        while True:
            if len(result.data) == 0:
                self._r.select("编辑", ["未找到Snippet记录"])
                break
            options = [f"{d.name} - {d.path}" for d in result.data]
            index, key = self._r.select("编辑", options)
            if key == -1:
                break
            data = result.data[index]
            res = edit(data.path, data.id)
            if not res.ok:
                self._r.error(res.message)
