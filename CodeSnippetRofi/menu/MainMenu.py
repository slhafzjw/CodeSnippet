import rofi

from menu.AddMenu import AddMenu
from menu.DeleteMenu import DeleteMenu
from menu.EditMenu import EditMenu
from menu.SearchMenu import SearchMenu


class MainMenu:
    def __init__(self,r: rofi.Rofi):
        self._r = r

    def run(self):
        while True:
            options = ["搜索", "编辑", "添加", "删除"]
            index, key = self._r.select("Code Snippet", options)
            print(f"index: {index}")
            print(f"key: {key}")
            if key == -1:
                break
            match index:
                case 0:
                    SearchMenu(self._r).run()
                case 1:
                    EditMenu(self._r).run()
                case 2:
                    AddMenu(self._r).run()
                case 3:
                    DeleteMenu(self._r).run()
