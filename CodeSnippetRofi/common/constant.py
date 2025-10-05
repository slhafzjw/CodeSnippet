import os

code_snippet_dir = os.getenv("CODE_SNIPPET_DIR")
code_snippet_port = os.getenv("CODE_SNIPPET_PORT")
code_snippet_rofi = os.getenv("CODE_SNIPPET_ROFI", "rofi")
code_snippet_editor = os.getenv("CODE_SNIPPET_EDITOR","nvim")

action_list = "LIST"
action_add = "ADD"
action_edit = "EDIT"
action_delete = "DELETE"

editor_class = "code_snippet_editor"

template_add = """
## Snippet

```[Language]

```

## MetaData

- Name
  - 
- Language
  - 
"""
template_edit = """
## Snippet

```[Language]

```

## MetaData

- Tags
  - 
  -     
- Description
  - 
"""