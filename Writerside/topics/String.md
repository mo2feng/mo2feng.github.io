# String

## 字符串的不可变性

String在Java中特别常用，而且我们经常要在代码中对字符串进行赋值和改变他的值，但是，为什么我们说字符串是不可变的呢？

首先，我们需要知道什么是不可变对象？

不可变对象是在完全创建后其内部状态保持不变的对象。这意味着，一旦对象被赋值给变量，我们既不能更新引用，也不能通过任何方式改变内部状态。

可是有人会有疑惑，String为什么不可变，我的代码中经常改变String的值啊，如下：

```java
String s="abcd";
        s=s.concat("ef");
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
String s="abcd";
        String s2=s;
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

{src="Replace.Java" validate="true"}

## "+"的重载 {id="plus-overload"}
Java中，想要拼接字符串，最简单的方式就是通过"+"连接两个字符串。

有人把Java中使用+拼接字符串的功能理解为运算符重载。其实并不是，Java是不支持运算符重载的。这其实只是Java提供的一个语法糖。

>运算符重载：在计算机程序设计中，运算符重载（英语：operator overloading）是多态的一种。运算符重载，就是对已有的运算符重新进行定义，赋予其另一种功能，以适应不同的数据类型。

>语法糖：语法糖（Syntactic sugar），也译为糖衣语法，是由英国计算机科学家彼得·兰丁发明的一个术语，指计算机语言中添加的某种语法，这种语法对语言的功能没有影响，但是更方便程序员使用。语法糖让程序更加简洁，有更高的可读性。

前面提到过，使用+拼接字符串，其实只是Java提供的一个语法糖， 那么，我们就来解一解这个语法糖，看看他的内部原理到底是如何实现的。

还是这样一段代码。我们把他生成的字节码进行反编译，看看结果。

    String wechat = "Hollis";
    String introduce = "每日更新Java相关技术文章";
    String hollis = wechat + "," + introduce;

反编译后的内容如下，反编译工具为jad。

    String wechat = "Hollis";
    String introduce = "\u6BCF\u65E5\u66F4\u65B0Java\u76F8\u5173\u6280\u672F\u6587\u7AE0";//每日更新Java相关技术文章
    String hollis = (new StringBuilder()).append(wechat).append(",").append(introduce).toString();

通过查看反编译以后的代码，我们可以发现，原来字符串常量在拼接过程中，是将String转成了StringBuilder后，使用其append方法进行处理的。

那么也就是说，Java中的+对字符串的拼接，其实现原理是使用StringBuilder.append。

但是，String的使用+字符串拼接也不全都是基于StringBuilder.append，还有种特殊情况，那就是如果是两个固定的字面量拼接，如：

    String s = "a" + "b"

编译器会进行常量折叠(因为两个都是编译期常量，编译期可知)，直接变成 String s = "ab"。

## 字符串拼接 {id="string-plus"}
字符串，是Java中最常用的一个数据类型了。

本文，也是对于Java中字符串相关知识的一个补充，主要来介绍一下字符串拼接相关的知识。本文基于jdk1.8.0_181。

### 字符串拼接

字符串拼接是我们在Java代码中比较经常要做的事情，就是把多个字符串拼接到一起。

我们都知道，**String是Java中一个不可变的类**，所以他一旦被实例化就无法被修改。

> 不可变类的实例一旦创建，其成员变量的值就不能被修改。这样设计有很多好处，比如可以缓存hashcode、使用更加便利以及更加安全等。

但是，既然字符串是不可变的，那么字符串拼接又是怎么回事呢？

**字符串不变性与字符串拼接**

其实，所有的所谓字符串拼接，都是重新生成了一个新的字符串。下面一段字符串拼接代码：

    String s = "abcd";
    s = s.concat("ef");

其实最后我们得到的s已经是一个新的字符串了。如下图

![][8]￼

s中保存的是一个重新创建出来的String对象的引用。

那么，在Java中，到底如何进行字符串拼接呢？字符串拼接有很多种方式，这里简单介绍几种比较常用的。

**使用`+`拼接字符串**

在Java中，拼接字符串最简单的方式就是直接使用符号`+`来拼接。如：

    String wechat = "Hollis";
    String introduce = "每日更新Java相关技术文章";
    String hollis = wechat + "," + introduce;

**concat**  
除了使用`+`拼接字符串之外，还可以使用String类中的方法concat方法来拼接字符串。如：

    String wechat = "Hollis";
    String introduce = "每日更新Java相关技术文章";
    String hollis = wechat.concat(",").concat(introduce);


**StringBuffer**

关于字符串，Java中除了定义了一个可以用来定义**字符串常量**的`String`类以外，还提供了可以用来定义**字符串变量**的`StringBuffer`类，它的对象是可以扩充和修改的。

使用`StringBuffer`可以方便的对字符串进行拼接。如：

    StringBuffer wechat = new StringBuffer("Hollis");
    String introduce = "每日更新Java相关技术文章";
    StringBuffer hollis = wechat.append(",").append(introduce);


**StringBuilder**  
除了`StringBuffer`以外，还有一个类`StringBuilder`也可以使用，其用法和`StringBuffer`类似。如：

    StringBuilder wechat = new StringBuilder("Hollis");
    String introduce = "每日更新Java相关技术文章";
    StringBuilder hollis = wechat.append(",").append(introduce);

**StringUtils.join**  
除了JDK中内置的字符串拼接方法，还可以使用一些开源类库中提供的字符串拼接方法名，如`apache.commons中`提供的`StringUtils`类，其中的`join`方法可以拼接字符串。

    String wechat = "Hollis";
    String introduce = "每日更新Java相关技术文章";
    System.out.println(StringUtils.join(wechat, ",", introduce));


这里简单说一下，StringUtils中提供的join方法，最主要的功能是：将数组或集合以某拼接符拼接到一起形成新的字符串，如：

    String []list  ={"Hollis","每日更新Java相关技术文章"};
    String result= StringUtils.join(list,",");
    System.out.println(result);
    //结果：Hollis,每日更新Java相关技术文章


并且，Java8中的String类中也提供了一个静态的join方法，用法和StringUtils.join类似。

以上就是比较常用的五种在Java种拼接字符串的方式，那么到底哪种更好用呢？为什么阿里巴巴Java开发手册中不建议在循环体中使用`+`进行字符串拼接呢？

<img src="https://www.hollischuang.com/wp-content/uploads/2019/01/15472850170230.jpg" alt=""/>￼

(阿里巴巴Java开发手册中关于字符串拼接的规约)

### 使用`+`拼接字符串的实现原理

关于这个知识点，前面的章节介绍过，主要是通过StringBuilder的append方法实现的。

### concat是如何实现的

我们再来看一下concat方法的源代码，看一下这个方法又是如何实现的。

    public String concat(String str) {
        int otherLen = str.length();
        if (otherLen == 0) {
            return this;
        }
        int len = value.length;
        char buf[] = Arrays.copyOf(value, len + otherLen);
        str.getChars(buf, len);
        return new String(buf, true);
    }


这段代码首先创建了一个字符数组，长度是已有字符串和待拼接字符串的长度之和，再把两个字符串的值复制到新的字符数组中，并使用这个字符数组创建一个新的String对象并返回。

通过源码我们也可以看到，经过concat方法，其实是new了一个新的String，这也就呼应到前面我们说的字符串的不变性问题上了。

### StringBuffer和StringBuilder

接下来我们看看`StringBuffer`和`StringBuilder`的实现原理。

和`String`类类似，`StringBuilder`类也封装了一个字符数组，定义如下：

    char[] value;


与`String`不同的是，它并不是`final`的，所以他是可以修改的。另外，与`String`不同，字符数组中不一定所有位置都已经被使用，它有一个实例变量，表示数组中已经使用的字符个数，定义如下：

    int count;


其append源码如下：

    public StringBuilder append(String str) {
        super.append(str);
        return this;
    }


该类继承了`AbstractStringBuilder`类，看下其`append`方法：

    public AbstractStringBuilder append(String str) {
        if (str == null)
            return appendNull();
        int len = str.length();
        ensureCapacityInternal(count + len);
        str.getChars(0, len, value, count);
        count += len;
        return this;
    }


append会直接拷贝字符到内部的字符数组中，如果字符数组长度不够，会进行扩展。

`StringBuffer`和`StringBuilder`类似，最大的区别就是`StringBuffer`是线程安全的，看一下`StringBuffer`的`append`方法。

    public synchronized StringBuffer append(String str) {
        toStringCache = null;
        super.append(str);
        return this;
    }


该方法使用`synchronized`进行声明，说明是一个线程安全的方法。而`StringBuilder`则不是线程安全的。

### StringUtils.join是如何实现的

通过查看`StringUtils.join`的源代码，我们可以发现，其实他也是通过`StringBuilder`来实现的。

    public static String join(final Object[] array, String separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = EMPTY;
        }
    
        // endIndex - startIndex &gt; 0:   Len = NofStrings *(len(firstString) + len(separator))
        //           (Assuming that all Strings are roughly equally long)
        final int noOfItems = endIndex - startIndex;
        if (noOfItems &lt;= 0) {
            return EMPTY;
        }
    
        final StringBuilder buf = new StringBuilder(noOfItems * 16);
    
        for (int i = startIndex; i &lt; endIndex; i++) {
            if (i &gt; startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }


### 效率比较

既然有这么多种字符串拼接的方法，那么到底哪一种效率最高呢？我们来简单对比一下。

    long t1 = System.currentTimeMillis();
    //这里是初始字符串定义
    for (int i = 0; i &lt; 50000; i++) {
        //这里是字符串拼接代码
    }
    long t2 = System.currentTimeMillis();
    System.out.println("cost:" + (t2 - t1));


我们使用形如以上形式的代码，分别测试下五种字符串拼接代码的运行时间。得到结果如下：

    + cost:5119
    StringBuilder cost:3
    StringBuffer cost:4
    concat cost:3623
    StringUtils.join cost:25726


从结果可以看出，用时从短到长的对比是：

`StringBuilder`<`StringBuffer`<`concat`<`+`<`StringUtils.join`

`StringBuffer`在`StringBuilder`的基础上，做了同步处理，所以在耗时上会相对多一些。

StringUtils.join也是使用了StringBuilder，并且其中还是有很多其他操作，所以耗时较长，这个也容易理解。其实StringUtils.join更擅长处理字符串数组或者列表的拼接。

那么问题来了，前面我们分析过，其实使用`+`拼接字符串的实现原理也是使用的`StringBuilder`，那为什么结果相差这么多，高达1000多倍呢？

我们再把以下代码反编译下：

    long t1 = System.currentTimeMillis();
    String str = "hollis";
    for (int i = 0; i &lt; 50000; i++) {
        String s = String.valueOf(i);
        str += s;
    }
    long t2 = System.currentTimeMillis();
    System.out.println("+ cost:" + (t2 - t1));


反编译后代码如下：

    long t1 = System.currentTimeMillis();
    String str = "hollis";
    for(int i = 0; i &lt; 50000; i++)
    {
        String s = String.valueOf(i);
        str = (new StringBuilder()).append(str).append(s).toString();
    }
    
    long t2 = System.currentTimeMillis();
    System.out.println((new StringBuilder()).append("+ cost:").append(t2 - t1).toString());


我们可以看到，反编译后的代码，在`for`循环中，每次都是`new`了一个`StringBuilder`，然后再把`String`转成`StringBuilder`，再进行`append`。

而频繁的新建对象当然要耗费很多时间了，不仅仅会耗费时间，频繁的创建对象，还会造成内存资源的浪费。

所以，阿里巴巴Java开发手册建议：循环体内，字符串的连接方式，使用 `StringBuilder` 的 `append` 方法进行扩展。而不要使用`+`。

### 总结

本文介绍了什么是字符串拼接，虽然字符串是不可变的，但是还是可以通过新建字符串的方式来进行字符串的拼接。

常用的字符串拼接方式有五种，分别是使用`+`、使用`concat`、使用`StringBuilder`、使用`StringBuffer`以及使用`StringUtils.join`。

由于字符串拼接过程中会创建新的对象，所以如果要在一个循环体中进行字符串拼接，就要考虑内存问题和效率问题。

因此，经过对比，我们发现，直接使用`StringBuilder`的方式是效率最高的。因为`StringBuilder`天生就是设计来定义可变字符串和字符串的变化操作的。

但是，还要强调的是：

1、如果不是在循环体中进行字符串拼接的话，直接使用`+`就好了。

2、如果在并发场景中进行字符串拼接的话，要使用`StringBuffer`来代替`StringBuilder`。

[1]: http://www.hollischuang.com/archives/99
[2]: http://www.hollischuang.com/archives/1249
[3]: http://www.hollischuang.com/archives/2517
[4]: http://www.hollischuang.com/archives/1230
[5]: http://www.hollischuang.com/archives/1246
[6]: http://www.hollischuang.com/archives/1232
[7]: http://www.hollischuang.com/archives/61
[8]: https://www.hollischuang.com/wp-content/uploads/2019/01/15472897908391.jpg
    