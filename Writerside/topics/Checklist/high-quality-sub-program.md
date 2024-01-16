# 检查清单：高质量的子程序

## 主体问题
+ 是否有创建该子程序的充足理由？
+ 在该子程序中，那些更适合抽出来放入单独子程序的部分是否都已经放入可单独的子程序中？
+ 关于该子程序的命名，是否使用了一个清晰的强势动词加对象的动宾结构来作为一个过程的名称，或者使用了对返回值的描述来作为一个函数的名称？
+ 该子程序的名称是否准确地描述了子程序所做的一切事情？
+ 是否为一些常用操作建立了命名规范？
+ 该子程序是否具有强大的能功能内聚性，即做且只做一件事，并且完成得很好？
+ 该子程序有松散的耦合性吗？该子程序与其他子程序的关联式简单的、专用的、可见的和灵活的吗？
+ 该子程序的长度是否由它的功能和逻辑自然决定，而不是由人为执行的编码标准决定的？

## 参数传递为题
+ 该子程序的参数列表作为一个整体而言，是否呈现了一致的接口抽象？
+ 该子程序的参数是否以一个合理的顺序进行了排列，该排列是否与其他类似子程序的参数顺序一致？
+ 对于接口假设是否由文档化记录？
+ 该子程序中是否使用了每个输入参数
+ 该子程序中是否使用了每个输出参数？
+ 该子程序中是否避免了把输入参数作为工作变量使用？
+ 如果该子程序是一个函数，它是否在所有可能的情况下都返回了一个有效值？