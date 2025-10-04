import json


class SearchResponse:
    def __init__(self, response: dict):
        self.status = response["status"]
        self.data = [SearchResponse.SearchData(dict(r)) for r in json.loads(response["data"])]

    class SearchData:
        def __init__(self, data: dict):
            self.id = data["id"]
            self.name = data["name"]
            self.path = data["path"]
            self.score = data["score"]

class NormalResponse:
    def __init__(self, response: dict):
        self.status = response["status"]
        self.data = response["data"]
