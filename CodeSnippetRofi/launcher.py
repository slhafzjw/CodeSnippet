from common import rofi
from common.constant import code_snippet_rofi
from menu.MainMenu import MainMenu

r = rofi.Rofi(code_snippet_rofi)
MainMenu(r).run()
