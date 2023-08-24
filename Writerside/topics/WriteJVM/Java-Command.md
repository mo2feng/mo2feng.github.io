# Java命令

## 概述

Java虚拟机的工作是运行Java应用程序。和其他类型的应用程
序一样，Java应用程序也需要一个入口点，这个入口点就是我们熟
知的main（）方法。如果一个类包含main（）方法，这个类就可以用来
启动Java应用程序，我们把这个类叫作主类。最简单的Java程序是
只有一个main（）方法的类，如著名的HelloWorld程序。

```java 
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
```

那么Java虚拟机如何知道我们要从哪个类启动应用程序呢？
对此，Java虚拟机规范没有明确规定。也就是说，是由虚拟机实现
自行决定的。比如Oracle的Java虚拟机实现是通过java命令来启动
的，主类名由命令行参数指定。java命令有如下4种形式：

```shell 
java [-options] class [args]
java [-options] -jar jarfile [args]
javaw [-options] class [args]
javaw [-options] -jar jarfile [args]
```

可以向java命令传递三组参数：选项、主类名（或者JAR文件名）和`main（）`方法参数。
选项由减号（`–`）开头。
通常，第一个非选项参数给出主类的完全限定名（fully qualified class name）。
但是如果用户提供了–jar选项，则第一个非选项参数表示JAR文件名，java命令必须从这个JAR文件中寻找主类。
javaw命令和java命令几乎一样，唯 一的差别在于，javaw命令不显示命令行窗口，因此特别适合用于启动GUI（图形用户界面）应用程序。

选项可以分为两类：标准选项和非标准选项。标准选项比较稳定，不会轻易变动。
非标准选项以-X开头，很有可能会在未来的版本中变化。
非标准选项中有一部分是高级选项，以-XX开头。

| 选项 |     描述      |
|:------------------:|:-----------:|
|    `-version `     |  显示版本信息并退出  |
|     `-?/-help`     |  显示帮助信息并退出  |
|  `-cp/-classpath`  |    指定类路径    |
| `-Dproperty=value` | 设置Java系统属性  |
|    `-Xms<size>`    |  设置初始堆空间大小  |
|    `-Xmx<size>`    |  指定最大堆空间大小  |
|   `-Xss<size> `    |  设置线程栈空间大小  |
