import os

from common.constant import code_snippet_rofi
from common import rofi
from menu.MainMenu import MainMenu

if code_snippet_rofi is None:
    r = rofi.Rofi()
else:
    r = rofi.Rofi(code_snippet_rofi)
MainMenu(r).run()
