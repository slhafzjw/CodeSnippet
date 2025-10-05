import rofi

from helper.api_helper import run_search, run_delete


class DeleteMenu:
    def __init__(self,r: rofi.Rofi):
        self._r = r

    def run(self):
        while True:
            try:
                result = run_search("")
            except Exception as e:
                print(f"Error: {e}")
                self._r.error(str(e))
                return
            if result.status != "SUCCESS":
                self._r.error(result.data)
                return
            if len(result.data) == 0:
                self._r.select("删除", ["未找到Snippet记录"])
                break
            options = [f"{d.name} - {d.path}" for d in result.data]
            index, key = self._r.select("删除", options)
            if key == -1:
                break
            ConfirmMenu(self._r,result.data[index].path).run()


class ConfirmMenu:
    def __init__(self,r:rofi.Rofi, path: str):
        self._r = r
        self.path = path

    def run(self):
        options = ["是", "否"]
        index, key = self._r.select("确定删除?", options)
        if key == -1:
            return
        if index == 0:
            res = run_delete(self.path)
            if res.status != "SUCCESS":
                self._r.error(res.data)
