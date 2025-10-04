# CodeSnippetDaemon

使用 Rofi 前端 + Java 守护进程管理代码片段，支持 Markdown 存储、跨平台同步和快速索引。
## 项目概述
- 使用`Rofi`前端+Java守护进程的代码片段管理工具。代码片段以Markdown形式存储在指定目录中，Java守护进程负责维护索引，Rofi前端负责与守护进程进行交互，并调用其他工具完成交互操作。
- 该项目的设计目的在于，简化‘在线预览前端’的编写，可以通过其他工具查看实际存储的代码片段内容，同时又能够做到跨平台同步。
- 因此，选择使用`Markdown`格式存储代码片段，使用SQLite维护目录索引，前端可以使用该项目列出的`Rofi`启动，也可以根据下文列出的请求格式自行编写前端。
- 此外，由于守护进程体量确实不大，故未采用http服务器的形式，而是选择使用普通的TCP交互（即守护进程使用SocketServer,前端使用netcat）。

## 依赖(Rofi前端)
- rofi
- nc
- nvim
  - 推荐装有 Markdown 相关插件
- python3
- python-rofi

## 工作流程

### 守护进程
- 实际代码片段内容存储在对应 Markdown 文件中
- 守护进程通过 SQLite 数据库维护目录索引，提供异常处理机制维护索引一致性
- 使用 SocketServer 创建 TCP 服务器，可由前端连接
- 添加片段时使用 AI 提取提供的代码片段的部分信息（标签、描述等），省却部分手动操作，也可以在添加完毕后手动进行编辑

### 前端
- 默认使用 Rofi 作为启动器，已涵盖后端所需的所有操作类型
- 借助 Python 完成复杂操作
- 涉及到代码片段的编辑行为时，将通过调用 nvim 来编辑临时文件，编辑完毕后将发送请求至守护进程

## 使用方法
1. 设置环境变量
   - CODE_SNIPPET_CONF
     - 配置文件目录
   - CODE_SNIPPET_DIR
     - 文件存储目录
   - CODE_SNIPPET_PORT
     - TCP服务器端口
   - CODE_SNIPPET_API_KEY
     - AI服务商提供的的 api_key
   - CODE_SNIPPET_BASE_URL
     - AI服务商提供的 base_url
   - CODE_SNIPPET_MODEL
     - 所用模型名称
   - CODE_SNIPPET_ROFI (可选，若未指定则使用默认 rofi)
     - 指定的 rofi 脚本，可以添加参数，比如: `rofi -theme ~/.config/rofi/launchers/type-4/style-1.rasi`

2. 启动 Java 守护进程
3. 启动 rofi 脚本


## 说明
### rofi 菜单说明
- 搜索
  - 编辑
  - 编辑并复制
  - 删除
- 添加
- 编辑
- 删除

### 接口说明
详见: [接口说明](doc/接口说明.md)

### 模板说明
对文件做出修改、添加文件时，看到的文档内容、可填写的内容都以模板文档为基础。

详见: [模板说明](doc/模板说明.md)

## 许可
本项目使用了修改版的 python-rofi（原项目 [python-rofi](https://github.com/bcbnz/python-rofi)，MIT License）。

对其源码进行了少量修改，以支持自定义 rofi 命令和样式。

本项目整体遵循 MIT 协议，详见 LICENSE 文件。