import rofi

from helper.file_helper import add


class AddMenu:
    def __init__(self,r:rofi.Rofi):
        self._r = r

    def run(self):
        result = add()
        if not result.ok:
            self._r.error(result.message)
