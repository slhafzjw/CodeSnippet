package work.slhaf.snippet.service;

import cn.hutool.json.JSONUtil;
import work.slhaf.snippet.common.Constant;
import work.slhaf.snippet.common.chat.ChatClient;
import work.slhaf.snippet.common.chat.constant.ChatConstant;
import work.slhaf.snippet.common.chat.pojo.ChatResponse;
import work.slhaf.snippet.common.chat.pojo.Message;
import work.slhaf.snippet.entity.file.AddEntity;
import work.slhaf.snippet.entity.file.MetaDataEntity;

import java.util.ArrayList;
import java.util.List;

import static work.slhaf.snippet.common.SnippetUtil.extractJson;

public class MetaDataExtractor {
    private final ChatClient chatClient = new ChatClient(
            System.getenv(Constant.Property.BASE_URL),
            System.getenv(Constant.Property.API_KEY),
            System.getenv(Constant.Property.MODEL)
    );
    private final List<Message> messages = List.of(
            new Message(ChatConstant.Character.SYSTEM, """
                    你需要对接下来发送的代码相关内容进行提取，提取出‘描述’、‘标签’等内容。
                    
                    输入格式示例:
                    ```json
                    {
                      "name": "ByteBuddy用法示例", //代码片段名称
                      "language": "Java", //代码片段所属语言
                      "content": "Class<? extends Module> clazz = module.getClazz();\\nClass<? extends Module> proxyClass = new ByteBuddy()\\n        .subclass(clazz)\\n        .method(ElementMatchers.isOverriddenFrom(overrideSource))\\n        .intercept(MethodDelegation.to(new ModuleProxyInterceptor(record.post, record.pre)))\\n        .make()\\n        .load(ModuleProxyFactory.class.getClassLoader())\\n        .getLoaded();" //代码片段内容
                    }
                    ```
                    
                    输出格式示例
                    ```json
                    {
                      "tags": [
                        "ByteBuddy",
                        "动态代理"
                      ],
                      "description": "通过ByteBuddy创建动态代理类的示例"
                    }
                    ```
                    """)
    );

    public MetaDataEntity extract(AddEntity entity) {
        List<Message> temp = new ArrayList<>(messages);
        temp.add(new Message(ChatConstant.Character.USER, JSONUtil.toJsonStr(entity)));
        ChatResponse response = chatClient.runChat(temp);
        return JSONUtil.toBean(extractJson(response.getMessage()), MetaDataEntity.class);
    }
}
