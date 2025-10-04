import os

code_snippet_dir = os.getenv("CODE_SNIPPET_DIR")
code_snippet_port = os.getenv("CODE_SNIPPET_PORT")
code_snippet_rofi = os.getenv("CODE_SNIPPET_ROFI")

action_list = "LIST"
action_add = "ADD"
action_edit = "EDIT"
action_delete = "DELETE"

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