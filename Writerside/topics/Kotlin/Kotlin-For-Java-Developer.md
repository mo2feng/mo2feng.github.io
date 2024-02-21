# 写给Java开发者的Kotlin入坑指南


## 快速认识 Kotlin
Kotlin 是著名 IDE 公司 JetBrains 创造出的一门基于 JVM 的语言。Kotlin 有着以下几个特点：

* 简洁，1行顶5行
* 安全，主要指“空安全”
* 兼容，与 Java 兼容
* 工具友好，IntelliJ 对 Kotlin 简直不要太友好

> JetBrains 不仅创造了 Kotlin，还创造了著名的 IntelliJ IDEA。

## 基础语法

### 1. 所有 Kotlin 类都是对象 (Everything in Kotlin is an object)
   与 Java 不一样是：Kotlin 没有基本数据类型 (Primitive Types)，所有 Kotlin 里面的类都是对象，它们都继承自: Any这个类；与 Java 类似的是，Kotlin 提供了如下的内置类型：

|Type|Bit width|备注|
|--|--|--|
|Double|64|Kotlin 没有 double|
|Float|32|Kotlin 没有 float|
|Long|64|Kotlin 没有 long|
|Int|32|Kotlin 没有 int/Integer|
|Short|16|Kotlin 没有 short|
|Byte|8|Kotlin 没有 byte|

> 思考题：

既然 Kotlin 与 Java 是兼容的，那么 Kotlin Int 与 Java int、Java Integer 之间是什么关系？

#### 查看 {collapsible="true"}
Kotlin 是一种静态类型的编程语言，它在设计时就考虑到了与 Java 的完全互操作性。Kotlin Int 是 Kotlin 语言中的整数类型，而 Java int 和 Java Integer 是 Java 中的整数类型。
Java int 是 Java 中的基本数据类型，用于表示整数。
Java Integer 是 Java 中的包装类，用于封装基本类型 int，它是一个对象。
在 Kotlin 中，Int 是 java.lang.Integer 的一个封装，这意味着在 Kotlin 代码中使用的 Int 类型，在编译为 JVM 字节码后，会直接映射到 Java 的 Integer 类型。但是，由于 Kotlin 对基本类型和包装类型进行了内联操作，因此在性能上通常不会有太大的差异。
当 Kotlin 代码与 Java 代码交互时，Kotlin 的 Int 类型可以直接与 Java 的 int 和 Integer 类型无缝工作。编译器会处理这种转换，使得开发者无需手动进行类型转换。这意味着，你可以直接在 Kotlin 中使用 Java 的方法，传入 Int 类型的值，即使该方法期望的是 int 或 Integer 类型的参数。
例如，如果你有一个 Java 方法如下：
```
public void setNumber(int number) {
    // 方法体
}
```
在 Kotlin 中，你可以这样调用它：

```
fun someKotlinFunction() {
    val kotlinInt: Int = 42
    setNumber(kotlinInt) // 直接传递 Kotlin Int 给期望 Java int 的方法
}
```
但是 **Kotlin 中的Int 类型不可为空，而 Java 的 Integer 类型可以为空**。如果需要传递一个可空的 Int 值给 Java 方法，需要使用 Kotlin 的可空类型。



> 思考题2：
> 
> Kotlin Any 类型与 Java Object 类型之间有什么关系？

### 2. 可见性修饰符 (Visibility Modifiers)

|修饰符|描述|
|--|--|
|public|与Java一致|
|private|与Java一致|
|protected|与Java一致|
|internal|同 Module 内可见|

### 3. 变量定义 (Defining Variables)
定义一个 Int 类型的可变变量:
```kotlin
var a: Int = 1
```
  
定义一个 Int 类型的可变的变量(初始化之后不可改变)：
```kotlin
val b: Int = 1
```
类型可推导时，类型申明可省略:
```kotlin
val c = 1
```
语句末尾的;可有可无:
```kotlin
val d: Int;
d = 1;
```  

小结：

* `var` 定义变量
* `val` 定义不可变的变量
* Kotlin 支持类型自动推导

> 思考题3：
>
> Kotlin val 变量与 Java 的 final 有什么关系？

#### 点击查看 {collapsible="true"}
在 Kotlin 中，`val` 关键字用于声明一个不可变的变量，这意味着一旦一个变量被初始化后，它的值就不能再被改变。这与 Java 中的 `final` 变量非常相似。
当你用 Kotlin 编写代码时，使用 val 声明的变量在编译成 Java 字节码后，会被转换成 Java 中的 `final` 变量。这意味着 Kotlin 的 val 变量在 Java 代码中看起来就像是一个 `final` 变量。
例如，在 Kotlin 中你可能有这样的声明：
```kt
val a: Int = 10
```
这会被编译成 Java 代码中的：
```java
final int a = 10;
```
这种设计选择确保了 Kotlin 的不可变变量在 Java 代码中也是不可变的，保持了类型安全和线程安全性。同时，这也使得 Kotlin 代码能够与 Java 代码无缝交互，因为 Java 开发者可以信赖 Kotlin val 变量不会被重新赋值。
需要注意的是，尽管 Kotlin 的` val` 类似于 Java 的 `final`，但它们在语义上并不完全相同。在 Kotlin 中，`val` 可以用于创建运行时的不可变对象，而 Java 的 final 仅保证变量引用本身不会被改变，不保证引用的对象本身是不可变的。例如，一个 `final` 的` ArrayList` 在 Java 中可以被修改，尽管你不能重新分配它。而在 Kotlin 中，如果你想要一个不可变的集合，你需要使用专门的不可变集合类型，如 `List` 而不是` MutableList`。


### 4 空安全 (Null Safety)

定义一个可为空的 String 变量:
```kotlin
var b: String? = "Kotlin"
b = null
print(b)
// 输出 null
```
定义一个不可为空的 String 变量:
```kotlin
var a: String = "Kotlin"
a = null
// 编译器报错，null 不能被赋给不为空的变量
```

变量赋值：
```kotlin
var a: String? = "Kotlin"
var b: String = "Kotlin"
b = a // 编译报错，String? 类型不可以赋值给 String 类型

a = b // 编译通过
```

空安全调用
```kotlin
var a: String? = "Kotlin"
print(a.length) // 编译器报错，因为 a 是可为空的类型
a = null
print(a?.length) // 使用?. 的方式调用，输出 null
```

Elvis 操作符
```kotlin
// 下面两个语句等价
val l: Int = if (b != null) b.length else -1
val l = b?.length ?: -1
```
// Elvis 操作符在嵌套属性访问时很有用
```kotlin
val name = userInstance?.user?.baseInfo?.profile?.name?: "Kotlin"
```
小结：

* `T` 代表不可为空类型，编译器会检查，保证不会被 null 赋值
*` T?` 代表可能为空类型
* 不能将 `T?` 赋值给 `T`
* 使用 instance?.fun() 进行空安全调用
* 使用 Elvis 操作符为可空变量替代值，简化逻辑

### 5. 类型检查与转换 (Type Checks and Casts)
#### 类型判断、智能类型转换 {id="type-check-and-auto-cast"}
   ```kotlin
   // x is String 类似 Java 里的 instanceOf
    if (x is String) {
    print(x.length) // x 被编译自动转换为 String
   }
   ```
 
#### 不安全的类型转换 as {id="unsafe-cast"}
   ```kotlin
    val y = null
    val x: String = y as String
   //抛异常，null 不能被转换成 String
   ```


####  安全的类型转换 as? {id="safe-cast"}
   ```kotlin
   val y = null
   val z: String? = y as? String
   print(z)
   // 输出 null
   ```

小结：
* 使用is 关键字进行类型判断
* 使用as 进行类型转换，可能会抛异常
* 使用as? 进行安全的类型转换

### 6. if 判断
   基础用法跟 Java 一毛一样。它们主要区别在于：Java If is Statement，Kotlin If is Expression。因此它对比 Java 多了些“高级”用法，懒得讲了，看后面的实战吧。

### 7. for 循环
   跟 Java 也差不多：

```kotlin
// 集合遍历，跟 Java 差不多
for (item in collection) {
    print(item)
}

// 辣鸡 Kotlin 语法
for (item in collection) print(item)

// 循环 1，2，3
for (i in 1..3) {
    println(i)
}

// 6，4，2，0
for (i in 6 downTo 0 step 2) {
    println(i)
}
```

### 8. when
when 就相当于高级版的 switch(类似JDK21 中的新switch 语法)，它的高级之处在于支持`模式匹配(Pattern Matching)`:

```kotlin
val x = 9
when (x) {
    in 1..10 -> print("x is in the range")
    in validNumbers -> print("x is valid")
    !in 10..20 -> print("x is outside the range")
    is String -> print("x is String")
    x.isOdd() -> print("x is odd")
    else -> print("none of the above")
}
// 输出：x is in the range
```



### 9. 相等性 (Equality)
Kotlin 有两种类型的相等性：

* 结构相等 (Structural Equality)
* 引用相等 (Referential Equality)
结构相等：
```kotlin
// 下面两句两个语句等价
a == b
a?.equals(b) ?: (b === null)
// 如果 a 不等于 null，则通过 equals 判断 a、b 的结构是否相等
// 如果 a 等于 null，则判断 b 是不是也等于 null
```

引用相等：
```kotlin
print(a === b)
// 判断 a、b 是不是同一个对象
```

> 思考题4：

```kotlin
val a: Int = 10000
val boxedA: Int? = a
val anotherBoxedA: Int? = a
print(boxedA == anotherBoxedA)
print(boxedA === anotherBoxedA)
// 输出什么内容?
```


思考题5：
```kotlin
val a: Int = 1
val boxedA: Int? = a
val anotherBoxedA: Int? = a
print(boxedA == anotherBoxedA)
print(boxedA === anotherBoxedA)
// 输出什么内容?
```


### 10. 函数 (Functions)
```kotlin    
fun triple(x: Int): Int {
    return 3 * x
}
// 函数名：triple
// 传入参数：不为空的 Int 类型变量
// 返回值：不为空的 Int 类型变量
```

   
### 11. 类 (Classes)
#### 类定义
使用主构造器(Primary Constructor)定义类一个 Person 类，需要一个 String 类型的变量：

```kotlin
class Person constructor(firstName: String) { ... }
```

如果主构造函数没有注解或者可见性修饰符，constructor 关键字可省略:

```kotlin
class Person(firstName: String) { ... }
```
也可以使用`次构造函数(Secondary Constructor)`定义类：
```kotlin
class Person{
    constructor(name: String) { ... }
}

// 创建 person 对象
val instance = Person("Kotlin")
```


#### `init` 代码块
Kotlin 为我们提供了 init 代码块，用于放置初始化代码：

```kotlin
class Person{
    var name = "Kotlin"
    init {
        name = "I am Kotlin."
        println(name)
    }

    constructor(s: String) {
        println(“Constructor”)
    }
}

fun main(args: Array<String>) {
    Person("Kotlin")
}
```

以上代码输出结果为：

I

结论：init 代码块执行时机在类构造之后，但又在“次构造器”执行之前。

### 12. 继承 (Inheritance)
* 使用 open 关键字修饰的类，可以被继承
* 使用 open 关键字修饰的方法，可以被重写
* 没有 open 关键字修饰的类，不可被继承
* 没有 open 关键字修饰的方法，不可被重写

以 Java 的思想来理解，Kotlin 的类和方法，默认情况下是 `final` 的
定义一个可被继承的 Base 类，其中的 `add()` 方法可以被重写，`test()` 方法不可被重写：

```kotlin
open class Base{
    open fun add() { ... }
    fun test()
}
```

定义 Foo 继承 Base 类，重写 `add()` 方法

```kotlin
class Foo() : Base() {
    override fun add()
}
```

使用: 符号来表示继承
使用`override` 重写方法

### 13. This 表达式 (Expression)

```kotlin
class A{
    fun testA(){    }
    inner class B{ // 在 class A 定义内部类 B
        fun testB(){    }   
        fun foo() {
            this.testB() // ok
            this.testA() // 编译错误
            this@A.testA() // ok
            this@B.testB() // ok
```
    
小结：

* inner 关键字定义内部类
* 在内部类当中访问外部类，需要显示使用this@OutterClass.fun() 的语法

### 14. 数据类 (Data Class)
假设我们有个这样一个 Java Bean:

```java
public class Developer {
    private String name;

    public Developer(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Developer developer = (Developer) o;
        return name != null ? name.equals(developer.name) : developer.name == null;
    }
    
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        return result;
    }
    
    @Override
    public String toString() {
        return "Developer{" + "name='" + name + '}';
    }
}
```


如果我们将其翻译成 Kotlin 代码，大约会是这样的:

```kotlin
class Developer(var name: String?) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val developer = o as Developer?
        return if (name != null) name == developer!!.name else developer!!.name == null
    }
    
    override fun hashCode(): Int {
        return if (name != null) name!!.hashCode() else 0
    }
    
    override fun toString(): String {
        return "Developer{" + "name='" + name + '}'.toString()
    }
}
```

然而，Kotlin 为我们提供了另外一种选择，它叫做数据类:

```kotlin
data class Developer(var
```

上面这一行简单的代码，完全能替代前面我们的写的那一大堆模板 Java 代码，甚至额外多出了一些功能。如果将上面的数据类翻译成等价的 Java 代码，大概会长这个样子：
```java

public final class Developer {
   @NotNull
   private String name;

   public Developer(@NotNull {
      super();
      this.name = name;
   }

   @NotNull
   public final String getName() {   return this.name;   }

   public final void setName(@NotNull {    this.name = var1;   }

   @NotNull
   public final Developer copy(@NotNull {   return new Developer(name);   }

   public String toString() {   return "Developer(name=" + this.name + ")";   }

   public int hashCode() {   return this.name != null ? this.name.hashCode() : 0;   }

   public boolean equals(Object var1) {
      if (this != var1) {
         if (var1 instanceof Developer) {
            Developer var2 = (Developer)var1;
            if (Intrinsics.areEqual(this.name, var2.name)) {
               return true;
            }
         }
         return false;
      } else {
         return true;
      }
   }
}

```

可以看到，Kotlin 的数据类不仅为我们提供了 getter、setter、equals、hashCode、toString，还额外的帮我们实现了 copy 方法！这也体现了 Kotlin 的简洁特性。

> 序列化的坑
> 
> 如果是旧工程迁移到 Kotlin，那么可能需要注意这个坑:
>
> ```kotlin
> // 定义一个数据类，其中成员变量 name 是不可为空的 String 类型，默认值是 MOMO
> data class Person(val age: Int, val name: String = "Kotlin")
> val person = gson.fromJson("""{"age":42}""", Person::class.java)
> print(person.name) // 输出 null
> ```
>

对于上面的情况，由于 Gson 最初是为 Java 语言设计的序列化框架，并不支持 Kotlin 不可为空、默认值这些特性，从而导致原本不可为空的属性变成null，原本应该有默认值的变量没有默认值。

对于这种情，市面上已经有了解决方案:

* `kotlinx.serialization`
* `moshi`

### 15. 扩展 (Extensions)
如何才能在不修改源码的情况下给一个类新增一个方法？比如我想给 `Context` 类增加一个 `toast` 类，怎么做？

如果使用 Java，上面的需求是无法被满足的。然而 Kotlin 为我们提供了扩展语法，让我们可以轻松实现以上的需求。

#### 扩展函数
为 Context 类定义一个 toast 方法：
```kotlin
fun Context.toast(msg: String, length: Int{
    Toast.makeText(this, msg, length).show()
}
```

扩展函数的使用：
```kotlin
val activity: Context? = getActivity()
activity?.toast("Hello world!")
activity?.toast("Hello world!", Toast.LENGTH_LONG)
```

#### 属性扩展
除了扩展函数，Kotlin 还支持扩展属性，用法基本一致。

思考题6：
上面的例子中，我们给不可为空的 Context 类增加了扩展函数，因此我们在使用这个方法的时候需要判空。实际上，Kotlin 还支持我们为 可为空的 类增加扩展函数：

```kotlin
// 为 Context? 添加扩展函数
fun Context?.toast(msg: String, length: Int{
    if (this == null) {    //do something    }
    Toast.makeText(this, msg, length).show()
}
```


扩展函数使用：
```kotlin
val activity: Context? = getActivity()
activity.toast("Hello world!")
activity.toast("Hello world!", Toast.LENGTH_LONG)
```
请问这两种定义扩展函数的方式，哪种更好？分别适用于什么情景？为什么？

### 16. 委托 (Delegation)

Kotlin 中，使用by关键字表示委托：
```kotlin
interface Animal{
    fun bark()
}

// 定义 Cat 类，实现 Animal 接口
class Cat : Animal {
    override fun bark() {
        println("喵喵")
    }
}

// 将 Zoo 委托给它的参数 animal
class Zoo(animal: Animal) : Animal by animal

fun main(args: Array<String>) {
    val cat = Cat()
    Zoo(cat).bark()
}
// 输出结果：喵喵
```

#### 属性委托 (Property Delegation)
其实，从上面类委托的例子中，我们就能知道，Kotlin 之所以提供委托这个语法，主要是为了方便我们使用者，让我们可以很方便的实现代理这样的模式。这一点在 Kotlin 的委托属性这一特性上体现得更是淋漓尽致。

Kotlin 为我们提供的标准委托非常有用。

by lazy 实现”懒加载“
```kotlin
// 通过 by 关键字，将 lazyValue 属性委托给 lazy {} 里面的实现
val lazyValue: String by lazy {
    val result = compute()
    println("computed!")
    result
}

// 模拟计算返回的变量
fun compute():String{
    return "Hello"
}

fun main(args: Array<String>) {
    println(lazyValue)
    println("=======")
    println(lazyValue)
}

```

以上代码输出的结果：
```
computed!

Hello
```

由此可见，`by lazy` 这种委托的方式，可以让我们轻松实现懒加载。

其内部实现，大致是这样的：


lazy 求值的线程模式: `LazyThreadSafetyMode`.
Kotlin 为lazy 委托提供三种线程模式，他们分别是：

* `LazyThreadSafetyMode.SYNCHRONIZED`
* `LazyThreadSafetyMode.NONE`
* `LazyThreadSafetyMode.PUBLICATION`
上面这三种模式，前面两种很好理解：

`LazyThreadSafetyMode.SYNCHRONIZED` 通过加锁实现多线程同步，这也是默认的模式。
`LazyThreadSafetyMode.NONE` 则没有任何线程安全代码，线程不安全。
我们详细看看LazyThreadSafetyMode.PUBLICATION，官方文档的解释是这样的：

> Initializer function can be called several times on concurrent access to uninitialized [Lazy] instance value, but only the first returned value will be used as the value of [Lazy] instance.

意思就是，用LazyThreadSafetyMode.PUBLICATION模式的 lazy 委托变量，它的初始化方法是可能会被多个线程执行多次的，但最后这个变量的取值是仅以第一次算出的值为准的。即，哪个线程最先算出这个值，就以这个值为准。

by Delegates.observable 实现"观察者模式"的变量
观察者模式，又被称为订阅模式。最常见的场景就是：比如读者们订阅了MOMO公众号，每次MOMO更新的时候，读者们就会收到推送。而观察者模式应用到变量层面，就延伸成了：如果这个的值改变了，就通知我。

```kotlin
class User{
    // 为 name 这个变量添加观察者，每次 name 改变的时候，都会执行括号内的代码
    var name: String by Delegates.observable("<no name>") {
        prop, old, new ->
        println("name 改变了：$old -> $new")
    }
}

fun main(args: Array<String>) {
    val user = User()
    user.name = "first: Tom"
    user.name = "second: Jack"
}
```

以上代码的输出为：
```
name 改变了：<no name> -> first: Tom
name 改变了：first: Tom -> second: Jack
```

> 思考题7：
>
> `lazy` 委托的`LazyThreadSafetyMode.PUBLICATION`适用于什么样的场景？

