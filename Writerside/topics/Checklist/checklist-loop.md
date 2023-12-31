# 检查清单：循环

## 循环的选择和创建
- 在合适的时候采用while 循环取代for循环了吗？
- 循环是由内向外创建的吗？

## 进入循环
- 是从顶部进入循环的吗？
- 初始化代码直接放在了循环前面了吗？
- 如果是无限循环或事件循环，其结构是否清晰，而不是采用类似`for i= 1 to 9999`这样蹩脚的代码？
- 如果循环属于C++、C或Java 的for循环，循环控制代码都放在循环头部了吗？

## 循环内部

- 是否使用 `{` 和 `}` 或其等价形式来封闭循环体以免修改不当而出错？
- 循环体里面由内容吗？它是非空的吗？
- 内务处理代码集中存放在循环开始或者循环结束的位置了吗？
- 循环是否就像定义良好的子程序那样只执行一种功能？
- 循环是否短到足以让人一目了然？
- 循环的嵌套层数控制在三层以内了吗？
- 长循环的内容转移到相应的子程序中了吗？
- 如果循环很长，是不是特别清晰？

## 循环索引
- `for` 循环体内的代码有没有随意改动循环索引值？
- 是否专门用变量保存重要的循环索引值，而不是在循环体外部使用循环索引？
- 循环索引是否是整数类型或枚举类型而不是浮点类型？
- 循环索引的名称有意义吗？
- 循环是否避免了索引串扰的问题？

## 退出循环
- 循环在所有可能的情况下都能终止吗？
- 如果已规定安全计数器标准，循环是否使用了安全计数器？
- 循环的终止条件是否显而易见的？
- 如果用到了`break`或者`continue`，它们的用法是否正确？