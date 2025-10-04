class ActionResult:
    def __init__(self, ok: bool, message: str):
        self.ok = ok
        self.message = message