# String

## 字符串的不可变性

String在Java中特别常用，而且我们经常要在代码中对字符串进行赋值和改变他的值，但是，为什么我们说字符串是不可变的呢？

首先，我们需要知道什么是不可变对象？

不可变对象是在完全创建后其内部状态保持不变的对象。这意味着，一旦对象被赋值给变量，我们既不能更新引用，也不能通过任何方式改变内部状态。

可是有人会有疑惑，String为什么不可变，我的代码中经常改变String的值啊，如下：

```java
String s = "abcd";
s = s.concat("ef");
```

这样，操作，不就将原本的"abcd"的字符串改变成"abcdef"了么？

但是，虽然字符串内容看上去从"abcd"变成了"abcdef"，但是实际上，我们得到的已经是一个新的字符串了。

![](string-immutable.png)

如上图，在堆中重新创建了一个"abcdef"字符串，和"abcd"并不是同一个对象。

所以，一旦一个string对象在内存(堆)中被创建出来，他就无法被修改。而且，String类的所有方法都没有改变字符串本身的值，都是返回了一个新的对象。

如果我们想要一个可修改的字符串，可以选择StringBuffer 或者 StringBuilder这两个代替String。

### 为什么String要设计成不可变

在知道了"String是不可变"的之后，大家是不是一定都很疑惑：为什么要把String设计成不可变的呢？有什么好处呢？

这个问题，困扰过很多人，甚至有人直接问过Java的创始人James Gosling。

在一次采访中James Gosling被问到什么时候应该使用不可变变量，他给出的回答是:

> I would use an immutable whenever I can.

那么，他给出这个答案背后的原因是什么呢？是基于哪些思考的呢？

其实，主要是从缓存、安全性、线程安全和性能等角度触发的。

Q：缓存、安全性、线程安全和性能？这有都是啥  
A：你别急，听我一个一个给你讲就好了。

#### 缓存

字符串是使用最广泛的数据结构。大量的字符串的创建是非常耗费资源的，所以，Java提供了对字符串的缓存功能，可以大大的节省堆空间。

JVM中专门开辟了一部分空间来存储Java字符串，那就是字符串池。

通过字符串池，两个内容相同的字符串变量，可以从池中指向同一个字符串对象，从而节省了关键的内存资源。

```java
String s = "abcd";
String s2 = s;
```

对于这个例子，s和s2都表示"abcd"，所以他们会指向字符串池中的同一个字符串对象：

![](string-ref.png)

但是，之所以可以这么做，主要是因为字符串的不变性。试想一下，如果字符串是可变的，我们一旦修改了s的内容，那必然导致s2的内容也被动的改变了，这显然不是我们想看到的。

#### 安全性

字符串在Java应用程序中广泛用于存储敏感信息，如用户名、密码、连接url、网络连接等。JVM类加载器在加载类的时也广泛地使用它。

因此，保护String类对于提升整个应用程序的安全性至关重要。

当我们在程序中传递一个字符串的时候，如果这个字符串的内容是不可变的，那么我们就可以相信这个字符串中的内容。

但是，如果是可变的，那么这个字符串内容就可能随时都被修改。那么这个字符串内容就完全不可信了。这样整个系统就没有安全性可言了。

#### 线程安全

不可变会自动使字符串成为线程安全的，因为当从多个线程访问它们时，它们不会被更改。

因此，一般来说，不可变对象可以在同时运行的多个线程之间共享。它们也是线程安全的，因为如果线程更改了值，那么将在字符串池中创建一个新的字符串，而不是修改相同的值。因此，字符串对于多线程来说是安全的。

#### hashcode缓存

由于字符串对象被广泛地用作数据结构，它们也被广泛地用于哈希实现，如HashMap、HashTable、HashSet等。在对这些散列实现进行操作时，经常调用hashCode()
方法。

不可变性保证了字符串的值不会改变。因此，hashCode()方法在String类中被重写，以方便缓存，这样在第一次hashCode()
调用期间计算和缓存散列，并从那时起返回相同的值。

在String类中，有以下代码：

```java
private int hash;//this is used to cache hash code.
```

#### 性能

前面提到了的字符串池、hashcode缓存等，都是提升性能的提现。

因为字符串不可变，所以可以用字符串池缓存，可以大大节省堆内存。而且还可以提前对hashcode进行缓存，更加高效

由于字符串是应用最广泛的数据结构，提高字符串的性能对提高整个应用程序的总体性能有相当大的影响。

### 总结

通过本文，我们可以得出这样的结论：字符串是不可变的，因此它们的引用可以被视为普通变量，可以在方法之间和线程之间传递它们，而不必担心它所指向的实际字符串对象是否会改变。

我们还了解了促使Java语言设计人员将该类设置为不可变类的其他原因。主要考虑的是缓存、安全性、线程安全和性能等方面


## Substring {id="java-substring"}

String是Java中一个比较基础的类，每一个开发人员都会经常接触到。而且，String也是面试中经常会考的知识点。

String有很多方法，有些方法比较常用，有些方法不太常用。今天要介绍的substring就是一个比较常用的方法，而且围绕substring也有很多面试题。

`substring(int beginIndex, int endIndex)`
方法在不同版本的JDK中的实现是不同的。了解他们的区别可以帮助你更好的使用他。为简单起见，后文中用`substring()`
代表`substring(int beginIndex, int endIndex)`方法。

### substring()的作用 {id="substring-usage"}

`substring(int beginIndex, int endIndex)`方法截取字符串并返回其[beginIndex,endIndex-1]范围内的内容。

    String x = "abcdef";
    x = x.substring(1,3);
    System.out.println(x);

输出内容：

    bc

### 调用substring()时发生了什么？ {id="what-happens-when-calling-substring"}

你可能知道，因为x是不可变的，当使用`x.substring(1,3)`对x赋值的时候，它会指向一个全新的字符串：

![string-immutability1](string-immutability1.png)

然而，这个图不是完全正确的表示堆中发生的事情。因为在jdk6 和 jdk7中调用substring时发生的事情并不一样。

### JDK6中的substring {id="jdk6-substring"}

String是通过字符数组实现的。在jdk 6 中，String类包含三个成员变量：`char value[]`， `int offset`，`int count`
。他们分别用来存储真正的字符数组，数组的第一个位置索引以及字符串中包含的字符个数。

当调用substring方法的时候，会创建一个新的string对象，但是这个string的值仍然指向堆中的同一个字符数组。这两个对象中只有count和offset
的值是不同的。

![string-substring-jdk6](string-substring-jdk6.png)

下面是证明上说观点的Java源码中的关键代码：

    //JDK 6
    String(int offset, int count, char value[]) {
        this.value = value;
        this.offset = offset;
        this.count = count;
    }
    
    public String substring(int beginIndex, int endIndex) {
        //check boundary
        return  new String(offset + beginIndex, endIndex - beginIndex, value);
    }

#### JDK6中的substring导致的问题 {id="jdk6-substring-problem"}

如果你有一个很长很长的字符串，但是当你使用substring进行切割的时候你只需要很短的一段。这可能导致性能问题，因为你需要的只是一小段字符序列，但是你却引用了整个字符串（因为这个非常长的字符数组一直在被引用，所以无法被回收，就可能导致内存泄露）。在JDK
6中，一般用以下方式来解决该问题，原理其实就是生成一个新的字符串并引用他。

    x = x.substring(x, y) + ""

关于JDK 6中subString的使用不当会导致内存系列已经被官方记录在Java Bug Database中：

![substring-memory-leak](jdk6-substring-memory-leak.png)

> 内存泄露：在计算机科学中，内存泄漏指由于疏忽或错误造成程序未能释放已经不再使用的内存。
> 内存泄漏并非指内存在物理上的消失，而是应用程序分配某段内存后，由于设计错误，导致在释放该段内存之前就失去了对该段内存的控制，从而造成了内存的浪费。

### JDK7 中的substring {id="jdk7-substring"}

上面提到的问题，在jdk 7中得到解决。在jdk 7 中，substring方法会在堆内存中创建一个新的数组。

![string-substring-jdk7](string-substring-jdk7.png)

Java源码中关于这部分的主要代码如下：
```
    //JDK 7
    public String(char value[], int offset, int count) {
        this.value = Arrays.copyOfRange(value, offset, offset + count);
    }
    
    public String substring(int beginIndex, int endIndex) {
        //check boundary
        return new String(value, beginIndex, subLen);
    }
```


以上是JDK 7中的subString方法，其使用`new String`创建了一个新字符串，避免对老字符串的引用。从而解决了内存泄露问题。

所以，如果你的生产环境中使用的JDK版本小于1.7，当你使用String的subString方法时一定要注意，避免内存泄露。

## replaceFirst、replaceAll、replace区别 {id="java-replace"}

`replace`、`replaceAll`和`replaceFirst`是Java中常用的替换字符的方法,它们的方法定义是：

`replace(CharSequence target, CharSequence replacement) `，用replacement替换所有的target，两个参数都是字符串。

`replaceAll(String regex, String replacement) `，用replacement替换所有的regex匹配项，regex很明显是个正则表达式，replacement是字符串。

`replaceFirst(String regex, String replacement) `，基本和replaceAll相同，区别是只替换第一个匹配项。

可以看到，其中replaceAll以及replaceFirst是和正则表达式有关的，而replace和正则表达式无关。

replaceAll和replaceFirst的区别主要是替换的内容不同，replaceAll是替换所有匹配的字符，而replaceFirst()仅替换第一次出现的字符

### 用法例子
```java
```
{src="Replace.Java" validate="true" }
    