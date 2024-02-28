# Eclipse Collections 库推荐

## Eclipse Collections：便利性驱动的Java集合框架

在快节奏的软件开发中，便利性是提升开发效率的关键因素。Eclipse Collections作为一个专为便利性设计的Java集合框架，提供了以下显著优势：

简化代码编写：
Eclipse Collections提供了直观的API，使得集合操作更加简洁。例如，select、reject、collect等方法可以直接在集合上调用，无需额外的lambda表达式或流操作，大大减少了代码量和复杂性。

提高代码可读性：
通过提供清晰的命名和直观的接口，Eclipse Collections使得代码意图更加明确。这不仅有助于新团队成员快速理解现有代码，也便于维护和重构。

减少样板代码：
Eclipse Collections通过提供丰富的方法，如groupBy、topOccurrences等，减少了编写重复代码的需要。这些方法封装了常见的集合操作模式，使得开发者可以专注于业务逻辑。


总之，Eclipse Collections通过其简洁的API设计、丰富的功能和强大的社区支持，为Java开发者提供了一个便利性极高的集合操作框架。这不仅能够提升开发效率，还能帮助团队更快地交付高质量的软件产品。

## 安装
11.1版本支持JDK8 - JDK21
```xml
<dependency>
  <groupId>org.eclipse.collections</groupId>
  <artifactId>eclipse-collections-api</artifactId>
  <version>11.1.0</version>
</dependency>

<dependency>
  <groupId>org.eclipse.collections</groupId>
  <artifactId>eclipse-collections</artifactId>
  <version>11.1.0</version>
</dependency>
```

## 更简洁的代码

1. 伪代码1
```
create <newCollection>
 for each <element> of <collection>
     if condition(<element>)
         add <element> to <newCollection>
```
EC实现

```java
MutableList<Integer> greaterThanFifty = list.select(each -> each > 50);
```
或者这样
```java
MutableList<Integer> greaterThanFifty = list.select(Predicates.greaterThan(50));
```

2. 伪代码2
```
create <newCollection>
 for each <element> of <collection>
     if not condition(<element>)
         add <element> to <newCollection>
```
EC实现
```java
MutableList<Integer> notGreaterThanFifty = list.reject(each -> each > 50);
```

3. 伪代码3
```
create <newCollection>
 for each <element> of <collection>
     <result> = transform(<element>)
     add <result> to <newCollection>
```

Java Stream 实现
```java
List<Address> addresses = people.stream()
            .map(person -> person.getAddress())
            .collect(Collectors.toList());
```

EC实现
```java
MutableList<Address> addresses = people.collect(person -> person.getAddress());
```

4. 伪代码4
```
create <newCollection>
 for each <element> of <collection>
     <results> = transform(<element>)
     Add all <results> to <newCollection>
```



EC实现
```java
MutableList<Address> flatAddress = 
people.flatCollect(person -> person.getAddresses());
```

5. 伪代码5
```
for each <element> of <collection>
  if condition(<element>)
    return <element>
```

EC实现
```java
Integer result = 
    list.detect(each -> each > 50);
```

6. 伪代码6
```
for each <element> of <collection>
     if condition(<element>)
         return true
 otherwise return false
```

EC实现
```java
boolean result = list.anySatisfy(num -> num > 50);
```

7. 伪代码7
```
for each <element> of <collection>
     if not condition(<element>)
         return false
 otherwise return true                
```

EC实现
```java
boolean result = list.allSatisfy(each -> each > 50);
```

8. 伪代码8
```
set <result> to <initialvalue>
 for each <element> of <collection>
     <result> = apply(<result>, <element>)
 return <result>
```
EC实现
```java
Integer result = Lists.mutable.of(1, 2).injectInto(3, Integer::sum);
```

## 更高性能的容器

EC 容器与Java的Collection 容器对比

![](https://eclipse.dev/collections/img/set.png)

![](https://eclipse.dev/collections/img/map.png)

![](https://eclipse.dev/collections/img/ints.png)

使用JMH对比 EC 与JDK LIST的addAll性能结果

```
Benchmark            Mode  Cnt    Score   Error  Units
ListAddAllTest.ec   thrpt   20  175.720 ± 5.025  ops/s
ListAddAllTest.jdk  thrpt   20  144.614 ± 5.756  ops/s
```

对比源码如下：
```java
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ListAddAllTest
{
    private static final int SIZE = 1000;
    private final List<Integer> integersJDK = new ArrayList<>(Interval.oneTo(SIZE));
    private final MutableList<Integer> integersEC = FastList.newList(Interval.oneTo(SIZE));

    @Benchmark
    public void jdk()
    {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
        {
            result.addAll(this.integersJDK);
        }
        if (result.size() != 1_000_000)
        {
            throw new AssertionError();
        }
    }

    @Benchmark
    public void ec()
    {
        MutableList<Integer> result = FastList.newList();
        for (int i = 0; i < 1000; i++)
        {
            result.addAll(this.integersEC);
        }
        if (result.size() != 1_000_000)
        {
            throw new AssertionError();
        }
    }

    @Test
    public void runTests() throws RunnerException
    {
        int warmupCount = this.warmUpCount();
        int runCount = this.runCount();
        Options opts = new OptionsBuilder()
            .include(".*" + this.getClass().getName() + ".*")
            .warmupTime(TimeValue.seconds(2))
            .warmupIterations(warmupCount)
            .measurementTime(TimeValue.seconds(2))
            .measurementIterations(runCount)
            .verbosity(VerboseMode.EXTRA)
            .forks(2)
            .build();

        new Runner(opts).run();
    }
}
```

更多的对比结果可以参考[这里](https://github.com/eclipse/eclipse-collections/tree/master/jmh-tests)。