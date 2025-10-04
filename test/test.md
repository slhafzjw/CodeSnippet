## Snippet
```Java
class test {
    while(true)

    {
        //输入层级
        if (esc) {
            break;
        }
        if (enter) {
            //按下enter
            while (true) {
                //展示结果层
                if (未找到) {
                    //无结果
                    if (esc || enter) {
                        break;
                    }
                } else {
                    //有结果
                    if (enter) {
                        //选中结果按下enter
                        while (true) {
                            //展示操作层
                            if (esc) {
                                break;
                            }
                            if (enter) {
                                //执行操作
                                doSomething();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
```

## MetaData
- Language
  - Java
- Tags
  - hello world
  - test
- Description
  - test-description