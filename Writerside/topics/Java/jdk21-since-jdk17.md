# Java17升Java21重要特性

<show-structure for="chapter,procedure" depth="2"/>

## 语言新特性

### switch的模式匹配 {id="jdk21-Pattern-Matching-for-switch"}

> 在Java 17中JEP 406: switch的模式匹配发布了首个预览版本，之后在JDK 18、JDK 19、JDK 20中又都进行了更新和完善。如今，在JDK 21中，该特性得到了完善，并正式进入。

在以往的switch语句中，对于case中的类型匹配限制是很多的。比如下面这个例子中的Map中可能存储了不同类型的对象，我们要判断的时候，就只能依靠if-else来完成。

```java
Map<String, Object> data = new HashMap<>();
data.put("key1", "aaa");
data.put("key2", 111);
if (data.get("key1") instanceof String s) {
  log.info(s);
}

if (data.get("key") instanceof String s) {
  log.info(s);
} else if (data.get("key") instanceof Double s) {
  log.info(s);
} else if (data.get("key") instanceof Integer s) {
  log.info(s);
}
```
在JDK21中，上面的代码可以简化为：
```java
switch (data.get("key1")) {
  case String s  -> log.info(s);
  case Double d  -> log.info(d.toString());
  case Integer i -> log.info(i.toString());
  default        -> log.info("");
}

```


### 440: Record Patterns (21)