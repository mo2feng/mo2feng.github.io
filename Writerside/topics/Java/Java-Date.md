# ALL ABOUT JAVA.UTIL.DATE

> 翻译文章(施工中)，原文连接：https://codeblog.jonskeet.uk/2017/04/23/all-about-java-util-date/

在Stack Overflow上引发大量相似问题的类中，`java.util.Date` 可谓名列前茅。这种情况的出现主要有四个原因：

* 日期和时间处理本质上相当复杂，并充满了各种特殊情况。尽管它是可控的，但确实需要投入一定的时间去理解和掌握。
* `java.util.Date` 类在许多方面存在严重缺陷（下文会具体阐述）。
* 开发者普遍对该类的理解不足。
* 库的作者对该类进行了不当使用，这进一步加剧了混淆和困扰。

## 简而言之：java.util.Date 的核心要点 {id="java-util-Date-in-a-nutshell"}

关于 java.util.Date 最重要的几点是：

- 如有可能，应尽量避免使用它。如果可能的话，改用 `java.time.*` 包（适用于Java 8及更高版本），或者对于旧版本的Java，可以使用 [`ThreeTen-Backport`（基本等同于java.time包）](http://www.threeten.org/threetenbp/)或 [`Joda Time`](http://www.joda.org/joda-time/)。
    + 如果不得不使用它，请避免使用已废弃的成员方法。它们中的大部分已经废弃近20年了，而且有充分的理由。
    +  如果你确实觉得必须使用那些已废弃的成员方法，请确保你真正理解它们的含义和用法。

- Date 实例表示的是一个时间点，而不是具体的日期。这一点非常重要，意味着：
    + 它不表示一个时间段。
    + 它不包含时区信息。
    + 它没有特定的格式。
    + 它不依赖于特定的日历系统。

接下来，我们深入了解细节……


##  `java.util.Date` 有什么问题? {id="what-s-wrong-with-java-util-date"}

`java.util.Date`（以下简称为`Date`）是一个设计糟糕的类型，这也是为什么在Java 1.1版本中就废弃了它的大部分功能（遗憾的是，至今仍有人在使用它）。

其设计缺陷包括：

- 名称具有误导性：它并不表示“日期”，而是表示时间的一个瞬间。因此，应该将其命名为`Instant`——就像其在`java.time`包中的等价物一样。
- 非最终类（non-final）：这种设计鼓励了不良的继承用法，例如`java.sql.Date`（该类确实表示日期，但由于名称相同而容易引起混淆）。
- 可变性：日期/时间类型自然适合用不可变类型来建模。`Date`的可变性（例如通过`setTime`方法）意味着细心的开发者不得不在各处创建防御性副本以保证安全。
- 在很多地方隐式地使用了系统的本地时区，包括`toString()`方法，这导致了许多开发者的困惑。更多相关信息请参见“什么是瞬时时间”部分。
- 月份编号基于0，这一特性是从C语言中借鉴来的，导致了大量的偏移量错误。
- 年份编号基于1900，同样源自C语言。到了Java发布的时候，我们理应认识到这对于可读性是不利的。
- 方法命名不够清晰：`getDate()`返回月份中的日期，而`getDay()`返回星期几。给这些方法取更具有描述性的名字有多难呢？
- 对于是否支持闰秒模糊不清：“一秒由0到61之间的整数表示；值60和61仅在正确跟踪闰秒的Java实现中用于表示闰秒。”我强烈怀疑大多数开发者（包括我自己）都曾假设`getSeconds()`方法返回的范围实际上是在0到59之间（包括两端）。
- 没有明显理由的情况下却采用了宽松模式：“在所有情况下，为这些目的提供的参数不必落在指示范围内；例如，可以指定日期为1月32日，然后解释为2月1日。”这种特性的实用性有多少呢？

我还可以找到更多的问题，但如果继续深入就会显得过于挑剔。上述这些问题已经足够多了。不过，从积极的一面来看：

- 它明确地表示了一个单一的值：精确到毫秒的时间瞬间，不附带任何日历系统、时区或文本格式。

不幸的是，即使是这个“好的方面”，开发人员也常常对其理解不足。让我们进一步剖析这一点……

## 什么是瞬时时间? {id="what-s-an-instant-in-time"}

> 注意：本文剩余部分完全忽略了相对论和闰秒的影响。虽然这对某些人来说非常重要，但对于大多数读者而言，引入这些概念只会增加更多混淆。 

当我说“瞬时”时，我指的是可用于标识*何时发生了某事*的那种概念（它可以指未来发生的事件，但从过去发生的事情角度思考最容易理解）。它独立于时区和日历系统，因此使用各自“本地”时间表示方式的不同人们可以用不同的方式谈论同一瞬时。

让我们以一个发生在我们不熟悉时区的地方的具体例子为例：尼尔·阿姆斯特朗在月球上行走。登月行动开始于一个特定的瞬时——如果世界各地的人们同时观看，他们几乎都会同时说出“我现在看到了”。

如果你在休斯顿的控制中心观看，可能会把这个瞬时视为“1969年7月20日晚上9点56分20秒，美国中部夏令时间”。如果你在伦敦观看，可能会认为是“1969年7月21日凌晨3点26分20秒，英国夏令时间”。如果你在利雅得观看，可能会认为是“伊斯兰历1389年7月7日凌晨5点56分20秒（+03）”（采用乌玛尔·卡勒里亚历）。尽管不同的观察者会在他们的时钟上看到不同的时间，甚至年份也可能不同，但他们仍然在考虑同一个瞬时。他们只是应用了不同的时区和日历系统，将瞬时转换为我们更加熟悉的、人类中心的概念。

那么，计算机是如何表示瞬时的呢？它们通常存储自某个特定瞬时以来或之前的持续时间，这个特定瞬时相当于一个原点。许多系统使用Unix纪元，即在公历UTC标准下的1970年1月1日午夜。但这并不意味着纪元本身“属于”UTC——Unix纪元同样可以定义为“纽约1969年12月31日晚上7点的那一刻”。

`Date` 类使用了“自Unix纪元以来的毫秒数”——这是`getTime()`方法返回的值，并可通过`Date(long)`构造函数或`setTime()`方法设置。由于月球行走发生在Unix纪元之前，所以其值为负数：实际上是-14159020000。

为了演示`Date`如何与系统时区交互，让我们展示前面提到过的三个时区——休斯顿（America/Chicago）、伦敦（Europe/London）和利雅得（Asia/Riyadh）。当我们根据纪元毫秒值构建日期时，当前系统的时区并不重要——这完全不依赖于本地时区。但是，如果我们使用`Date.toString()`，它会将结果转换为当前默认时区显示出来。更改默认时区并不会改变`Date`值。对象内部状态完全保持不变。它仍然代表相同的瞬时，但是像`toString()`、`getMonth()`和`getDate()`这样的方法将会受到影响。以下是展示这一点的示例代码：

```java
import java.util.Date;
import java.util.TimeZone;

public class Test {

    public static void main(String[] args) {
        // The default time zone makes no difference when constructing
        // a Date from a milliseconds-since-Unix-epoch value
        Date date = new Date(-14159020000L);

        // Display the instant in three different time zones
        TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"));
        System.out.println(date);

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
        System.out.println(date);

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Riyadh"));
        System.out.println(date);

        // Prove that the instant hasn't changed...
        System.out.println(date.getTime());
    }
}
```

The output is as follows:

```
Sun Jul 20 21:56:20 CDT 1969
Mon Jul 21 03:56:20 GMT 1969
Mon Jul 21 05:56:20 AST 1969
-14159020000
```

在这段输出中，“GMT”和“AST”的缩写非常令人遗憾——`java.util.TimeZone`在所有情况下并未对1970年以前的值使用正确的时区名称。尽管，*时间*是正确的。

由于`java.util.Date`的`toString()`方法在显示日期时会依赖于当前系统的默认时区，因此在处理1970年前的日期时，`TimeZone`类可能无法提供准确的时区名称。尽管如此，这里所显示的*时间*还是基于原始的瞬时值换算成各个时区的结果，即使时区名称不准确，时间本身是经过正确转换的。

## 常见问题 {id="common-questions"}

### 如何将 `Date` 转换为不同的时区? {id="how-do-i-convert-a-date-to-a-different-time-zone"}

​	你的代码无需进行此类转换——因为`Date`对象并没有携带时区信息。它仅仅表示一个时间点。**不要被`toString()`方法的输出结果所迷惑。**该输出将时间点按照默认时区展示，但这并不是`Date`对象值的一部分。

如果你的代码接受一个`Date`作为输入，那么从“本地时间和时区”到时间点的转换应当已经完成了。（希望这个转换是正确执行的……）

如果你开始编写如下方法，实际上是在给自己制造麻烦：

```java
// 类似这样的方法总是错误的
Date convertTimeZone(Date input, TimeZone fromZone, TimeZone toZone)
```

因为`Date`对象只表示时间点，而非带有时区信息的日期时间。如果你想要将一个时间点从一个时区转换到另一个时区，你应该使用Java 8以后推出的`java.time`包中的`ZonedDateTime`或其他相关类来进行处理。正确的做法可能是首先将`Date`转换为`Instant`（表示时间戳，无时区），再将此`Instant`转换为目标时区的`ZonedDateTime`对象。这样做才符合逻辑且能正确处理时区转换问题。


### 如何将 `Date` 转换为不同的格式? {id="how-do-i-convert-a-date-to-a-different-format"}

你不能直接修改`Date`的格式——因为`Date`对象本身不具备格式属性。**切勿被`toString()`方法的输出结果所误导**。`toString()`方法始终按照固定的格式生成字符串，正如[JDK文档](http://docs.oracle.com/javase/8/docs/api/java/util/Date.html#toString--)中所述。

若要以特定格式呈现`Date`对象，你需要使用合适的`DateFormat`（可能是`SimpleDateFormat`子类）进行格式化操作，并记住将其时区设置为你所需的目标时区。这样才能正确地将`Date`对象转换为你所需的格式和时区的日期时间字符串。例如：

```java
Date date = new Date();
SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault());
formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
String formattedDate = formatter.format(date);
System.out.println(formattedDate);
```

这段代码将`Date`对象格式化为"年-月-日 时:分:秒 时区"的形式，并设定为美国纽约时区。