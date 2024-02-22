# 写给Java开发者的Kotlin入门指南
<show-structure for="chapter,procedure" depth="2"/>

## 快速认识 Kotlin {id="quick-start"}
Kotlin 是著名 IDE 公司 JetBrains 创造出的一门基于 JVM 的语言。Kotlin 有着以下几个特点：

* 简洁，1行顶5行
* 安全，主要指“空安全”
* 兼容，与 Java 兼容
* 工具友好，IntelliJ 对 Kotlin 简直不要太友好

> JetBrains 不仅创造了 Kotlin，还创造了著名的 IntelliJ IDEA。

## 对比Java {id="compare-with-java"}

### 和Java的基本区别

* 没有new关键字, 直接创建对象.
* 没有;
* 类型分为Nullable(带?)和Non-Null.
* 继承类和实现接口都用`:`.

### 类型
在Kotlin中支持的数据类型: `Double`,` Float`, `Long`, `Int`, `Short`, `Byte`. 不再像Java一样支持小数到大数的默认转换, 所有的转换都要通过显式的方法, 比如`toInt()`方法.

字符类型: `Char`, 不能像Java中一样直接当数字对待了. 

布尔类型: `Boolean`.

以上类型在运行时都会被表示为JVM原生类型(除非是可空类型和泛型应用), 但是在代码中它们只有一种写法(不像Java中有`int`和`Integer`两种), 在用户看来它们就是普通的类.

所有的类型需要可空(nullable)类型时, 会被自动装箱.

字符串类型: `String`: 字符串是不可变(immutable)类型, 可以用`[]`访问元素, 可以用`$`加入模板表达式(可以是变量也可以是用{}包含的表达式).

### 方法声明
#### 方法声明格式:

```
fun 方法名(参数): 返回值类型 {    方法体}
```

参数列表中变量名在前面, 变量类型在后面.

比如:

```
fun sum(a: Int, b: Int): Int {    return a + b}
```

方法体要是返回一个简单的表达式, 可以直接用`=`

```
fun sum(a: Int, b: Int) = a + b
```

这种情况下由于返回值类型可以被推断出来, 所以也可以省略不写.

返回值为空时，类型为` Unit`，可以省略不写.

当方法有body时, 除非返回值是`Unit`, 否则返回值类型是不能被省略的.

#### 方法默认参数
声明方法的时候, 可以用`=`给参数设置默认值. 当调用方法时省略了该参数, 就会使用默认值.

调用方法的时候可以指定参数的名字.

```
fun reformat(str: String,             normalizeCase: Boolean = true,             upperCaseFirstLetter: Boolean = true,             divideByCamelHumps: Boolean = false,             wordSeparator: Char = ' ') {...}
```

调用的时候:

reformat(str, wordSeparator = '_')

更多可以查看官方文档: [Functions](https://kotlinlang.org/docs/functions.html)

### 变量声明
变量声明有两个关键字: `val`和`var`.

其中val只能被赋值一次, 相当于final.

```kotlin
val a: Int = 1  // 立即声明并赋值
val b = 2   // 自动推断`Int`类型 
val c: Int  // 没有初始值, 需要声明类型
c = 3       // 明确赋值
```

变量声明时如果初始化可以推断出类型, 则可以省略类型.

### 流程控制
#### if
在kotlin中的if是一个表达式(expression), 即可以返回值.

它的分支可以是{}包围的block, 其中最后一个表达式就是对应分支的值.

```kotlin
val max = if (a > b) {    
    print("Choose a")    
    a
} else {   
    print("Choose b")    
    b
}
```
        
当if作为表达式被使用的时候, 它**必须**有else.

Java中常用的三元运算符条件? 满足 : 不满足, 在Kotlin中是**不存在**的.

Kotlin中的Elvis Operator是用来进行null判断然后选择处理的. (具体见下篇空安全文章介绍)

#### when
when是用来取代Java中的switch的.
```kotlin
when (x) {   
    1 -> print("x == 1")    
    2 -> print("x == 2")    
    else -> { // Note the block        
        print("x is neither 1 nor 2")    
    }
}
```

和if一样, when也是可以被用作expression(表达式)或statement. 作为表达式使用的时候, else是必须的(除非编译器可以推断所有的情况都已经被覆盖了).

#### 循环
while, do...while, break, continue的用法都和Java一样.

for的用法也类似, 只不过在Java中是`:`, 在Kotlin中变成了`in`.

Kotlin有一些Range操作符
```kotlin
val x = 10
val y = 9
if (x in 1..y+1) {
    println("fits in range")
}
```
更多可以参考: [Ranges](https://kotlinlang.org/docs/basic-syntax.html#ranges)


### 空安全: Kotlin的一大卖点 {id="null-safety"}
Kotlin的type system旨在减少NullPointerException(NPE).

主要是通过编译期间, 就区分了哪些东西是不会为null的, 哪些是可能为null的. 如果想要把null传递给不能为null的类型, 会有编译错误. 可能为null的引用使用时必须要做相应的检查.

```kotlin
var a: String = "abc"
// a = null // compile error
val aLength = a.length

var b: String? = "abc"
b = null // ok
// val bLength = b.length // 不能直接使用, compile error
```
### 检查是否为null
对于可能为null的类型, 可以用if来做显式的检查. 但是这种只适用于变量不可变的情形, 免得刚检查完是否为null, 它就变了.

### Safe Calls
可以用操作符·来进行安全操作.

```kotlin
var b: String? = "abc"
val bLength = b?.length // 如果b不为null, 返回长度, 如果b为null, 返回null
```
上面bLength类型为Int?.

Safe calls在链式操作时非常有用. 比如:

`bob?.department?.head?.name`
其中任何一个环节为null了最后的结果就为null.

还可以用在表达式的左边:

````kotlin
// 如果 `person` 或者 `person.department` 为 null,方法就不会被调用
person?.department?.head = managersPool.getManager()
````
### let()
实践中比较常见的是用`let()`操作符, 对非null的值做一些操作:

```kotlin
 val listWithNulls: List<String?> = listOf("Kotlin", null)
    for (item in listWithNulls) {    
        item?.let { 
            println(it)  // it 编辑lambda表达式的默认变量
        }
    }
```
参考 [Scope Functions](https://kotlinlang.org/docs/scope-functions.html#let)


### Elvis Operator
我们可能有这样的需求, 有个可能为null的变量, 我们需要在其不为null的时候返回一个表达式, 为null的时候返回一个特定的值.

比如:

```kotlin
val l: Int = if (b != null) b.length else -1
`
我们可以写成这样:

```kotlin
val l = b?.length ?: -1
```
仅当?:左边为null的时候, 右边的表达式才会执行.

实际应用: 在Kotlin中, return和throw都是表达式, 所以我们可以这样用:

```kotlin 
fun foo(node: Node): String? {
    val parent = node.getParent() ?: return null
    val name = node.getName() ?: throw IllegalArgumentException("name expected")
// ...
}
```
### 操作符!!

`!!`操作符表达的是: "嘿, 肯定不为`null`". 所以它称作not-null assertion operator. 它的作用是把原来可能为`null`的类型**强转**成不能为`null`的类型.

> 如果这种断言失败了, 就抛出NPE了.

### 仍然可能会遇到NPE的情形
* `throw NullPointerException()`.
* `!!`使用在了为null的对象上.
* 初始化相关的一些数据不一致情形.
* 一个构造中没有初始化的this传递到其他地方使用.
* 基类的构造中使用了`open`的成员, 实现在子类中, 此时可能还没有被初始化.
* 和Java互相调用的时候:

    * Java声明的类型叫[platform type](https://kotlinlang.org/docs/java-to-kotlin-nullability-guide.html#platform-types), 其null安全和Java中的一样.
    * 和Java互相调用时的泛型使用了错误的类型. 比如Kotlin中的`MutableList<String>`, 在Java代码中可能加入一个`null`. 此时应该声明为`MutableList<String?>`.
    * 其他外部Java代码导致的情形.




### 安全强转
强转时如果类型不匹配会抛出ClassCastException 可以用`as?`来做安全强转, 当类型不匹配的时候返回`null`, 而不是抛出异常.

```kotlin
val aInt: Int? = a as? Int
```
类型检查用关键字`is`.

### 集合过滤
集合中有个`filterNotNull()`可以用来过滤出非空元素.

```kotlin
    val nullableList: List<Int?> = listOf(1, 2, null, 4)
    val intList: List<Int> = nullableList.filterNotNull()
    println(intList) //[1, 2, 4]
```

### Kotlin中的类和对象 {id="class-and-objects-in-kotlin"}
Kotlin中的类关键字仍然是`class`, 但是创建类的实例不需要`new`.

### 构造函数
构造函数分为: `primary constructor`(一个)和`secondary constructor`(0个、1个或多个).

如果一个非抽象类自己没有声明任何构造器, 它将会生成一个无参数的主构造, 可见性为`public`.

#### 主构造函数 {id = "Primary-Constructor"}
主构造函数写在类名后面, 作为class header:

```kotlin
class Person constructor(firstName: String) {
//... 
}
```
如果没有注解和可见性修饰符, 关键字`constructor`是可以省略的:

```kotlin
class Person2(firstName: String) {
//... 
}
```

#### init代码块和属性初始化代码 {id="init-code-block-and-property-initialization-code"}
主构造函数是不能包含任何代码的, 如果需要初始化的代码, 可以放在init块中.

在实例的初始化阶段, init块和属性初始化的执行顺序和它们在body中出现的顺序一致.
```kotlin
class InitOrderDemo(name: String) {
    val firstProperty = "First property: $name".also(::println)

    init {
        println("First initializer block that prints $name")
    }

    val secondProperty = "Second property: ${name.length}".also(::println)

    init {
        println("Second initializer block that prints ${name.length}")
    }
}
```
这个类被实例化的时候,`print`的顺序和代码中的顺序一致.

这个例子也可以看出, 主构造函数中传入的参数在`init`块和属性初始化代码中是可以直接使用的.

实际上, 对于主构造中传入的参数是可以直接声明为属性并初始化的.

```kotlin
class PersonWithProperties(val firstName: String, val lastName: String, var age: Int) {
//... 
}
```
可以是`val`也可以是`var`.

#### 次构造函数 {id="Secondary-Constructors"}
次构造是用`constructor`关键字标记, 写在body里的构造.

如果有主构造, 次构造需要代理到主构造, 用`this`关键字.

注意init块是和主构造关联的, 所以会在次构造代码之前执行.

即便没有声明主构造, 这个代理也是隐式发生的, 所以init块仍然会执行.

```kotlin
class Constructors {
    init {
        println("Init block")
    }

    constructor(i: Int) {
        println("Constructor")
    }
}
```

创建实例, 输出:

Init blockConstructor

### 继承
继承用`:`. 方法覆写的时候`override`关键字是必须的.

Kotlin中默认是不鼓励继承的(类和方法默认都是final的):

* 类需要显式声明为`open`才可以被继承.
* 方法要显式声明为`open`才可以被覆写.

抽象类(abstract)默认是`open`的. 一个已经标记为`override`的方法是`open`的, 如果想要禁止它被进一步覆写, 可以在前面加上`final`.

属性也可以被覆盖, 同方法类似, 基类需要标记`open`, 子类标记`override`.

注意`val`可以被覆写成`var`, 但是反过来却不行.

```kotlin
interface Foo {
    val count: Int
}

class Bar1(override val count: Int) : Foo

class Bar2 : Foo {
    override var count: Int = 0
}
```

注意, 由于初始化顺序问题. 在基类的构造, `init`块和属性初始化中, 不要使用`open`的成员.

### 对象表达式和对象声明
Java中的匿名内部类, 在Kotlin中用对象表达式(expression)和对象声明(declaration).

表达式和声明的区别: 声明不可以被用在`=`的右边.

#### 对象表达式 {id="object-expression"}
用`objec`关键字可以创建一个匿名类的对象.

这个匿名类可以继承一个或多个基类:

```kotlin
open class A(x: Int) {
    public open val y: Int = x
}

interface B {
// ...
}

val ab: A = object : A(1), B {
    override val y = 15
}
```

也可以没有基类:

```kotlin
fun foo() {
    val adHoc = object {
        var x: Int = 0        var y: Int = 0
    } 
    print (adHoc.x + adHoc.y)
}
```

#### 对象声明 {id="object-declaration"}
在Kotlin中可以用object来声明一个单例.
```kotlin
// singleton
object DataProviderManager {
    fun doSomething() {
    }
}

fun main() {
    DataProviderManager.doSomething()
}
```

对象声明的初始化是线程安全的. 使用的时候直接用类名即可调用它的方法.

#### 伴生对象 {id="Companion-Objects"}
在类里面写的对象声明可以用companion关键字标记, 表示伴生对象.

Kotlin中的类并没有静态方法. 在大多数情况下, 推荐使用包下的方法.

如果在类中声明一个伴生对象, 就可以像Java中的静态方法一样, 用类名.方法名调用方法.

```kotlin
class MyClass {
    companion object Factory {
        fun create(): MyClass = MyClass()
    }
}

fun main() {
    MyClass.create()
}
```

### Model类神器: data class
~~Lombok 给我死~~

model类可以用`data`来标记为`data class`.

```kotlin
data class User(val name: String, val age: Int)
```
编译器会根据在主构造中声明的所有属性, 自动生成需要的方法, 包括`equals()`/`hashCode()`, `toString()`, `componentN()`和`copy()`.

为了让生成的代码有意义, 需要满足这些条件:

* 主构造至少要有一个参数.
* 所有的主构造参数都要被标记为`val`或`var`.
* data class不能为`abstract`, `open`, `sealed`或`inner`.


> 注意
{style="warning"}

* 如果equals(), hashCode()或 toString()有显式的实现, 或基类有final版本, 这三个方法将不会被生成, 而使用现有版本.
* 如果需要生成的类有无参数的构造器, 那么所有的属性都需要指定一个默认值.
    ```kotlin
    data class UserWithDefaults(val name: String = "", val age: Int = 0)
    ```
* 生成的方法只会使用主构造中声明的属性, 如果想要排除某些属性, 可以放在body中声明.

> 建议

虽然`data class`中的属性可以被声明为`val`或`var`, 但是推荐使用`val`, 即不可变(immutable)的属性, 从而让类的实例是不可变的.

不可变的实例在创建完成之后就不会再改变值. 可以用`copy()`方法创建新的实例, 修改一些属性.




## 基础语法 {collapsible="true"}

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

####  既然 Kotlin 与 Java 是兼容的，那么 Kotlin Int 与 Java int、Java Integer 之间是什么关系？{collapsible="true"}

{collapsible="true" default-state="collapsed"}

Kotlin的Int类型在底层是直接映射到Java的int类型的。这意味着在Kotlin中，当你声明一个Int类型的变量时，它在Java字节码层面上就是int类型。Kotlin的Int类型提供了与Java的int类型相同的功能，包括所有的算术运算和比较操作。

Kotlin的Int类型在某些情况下可以被视为Java的`Integer`类型。这是因为Kotlin的Int类型是不可空的（non-nullable），而Java的`Integer`类型是可空的（nullable）。在Kotlin中，你可以将Int类型的值赋给`Integer`类型的变量，或者将`Integer`类型的值赋给`Int`类型的变量，但后者需要显式转换（例如，使用`.toInt()`方法）。

在Kotlin中，如果你需要一个可空的整型变量，你应该使用Int?类型。Int?类型在字节码层面上仍然是Integer类型，因为它是可空的。

总结来说，Kotlin的Int类型在功能上与Java的int类型相同，但在类型系统上，它同时提供了与Java的Integer类型相似的可空性处理。这使得Kotlin在处理整型数据时更加灵活，同时保持了与Java的兼容性。


#### Kotlin Any 类型与 Java Object 类型之间有什么关系？{collapsible="true"}

{collapsible="true" default-state="collapsed"}

Kotlin的`Any`类型与Java的`Object`类型在概念上是相似的，但它们在语言层面上有一些关键的区别。

在Java中，`Object`是所有类的根类，是所有引用类型的超类。这意味着所有的Java类（除了基本数据类型）都直接或间接地继承自`Object`。基本数据类型（如int, float, boolean等）在Java中不是类，它们有对应的包装类（如`Integer`, `Float`, `Boolean`等），这些包装类继承自`Object`。

在Kotlin中，`Any`类型是所有非空类型的超类型，它不仅包括所有类，还包括Kotlin中的所有基本数据类型（如`Int`, `Float`, `Boolean`等）。在Kotlin中，所有类型都是引用类型，即使是基本数据类型，它们也被当作对象处理。这意味着在Kotlin中，你可以将基本数据类型直接赋值给`Any`类型的变量，而不需要使用包装类。Kotlin的`Any`类型在底层对应于Java的`Object`类型，当你在Kotlin中使用Any类型时，它在编译成Java字节码时会映射为`Object`。

Kotlin的Any类型提供了一些基本的方法，如`equals()`, `hashCode()`, 和 `toString()`，这些方法在Kotlin中被定义为开放（open）方法，允许子类重写它们。然而，Kotlin的Any类型并不包含JavaObject类中的所有方法，例如`wait()`, notify(), notifyAll()等，因为这些方法在Kotlin中没有直接的对应。

总结来说，Kotlin的Any类型在功能上类似于Java的Object类型，但Kotlin通过Any类型统一了对所有类型（包括基本数据类型）的处理，使得类型系统更加简洁和一致。在Kotlin中，你不需要像在Java中那样区分基本数据类型和引用类型，因为所有类型在Kotlin中都是对象。


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

> 思考题：

##### Kotlin val 变量与 Java 的 final 有什么关系？ {collapsible="true"}
{collapsible="true" default-state="collapsed"}
在 Kotlin 中，`val` 关键字用于声明一个不可变的变量，这意味着一旦一个变量被初始化后，它的值就不能再被改变。这与 Java 中的 `final` 变量非常相似。
当你用 Kotlin 编写代码时，使用 val 声明的变量在编译成 Java 字节码后，会被转换成 Java 中的 `final` 变量。这意味着 Kotlin 的 val 变量在 Java 代码中看起来就像是一个 `final` 变量。
例如，在 Kotlin 中你可能有这样的声明：
```kotlin
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

> 思考题

```kotlin
val a: Int = 10000
val boxedA: Int? = a
val anotherBoxedA: Int? = a
print(boxedA == anotherBoxedA)
print(boxedA === anotherBoxedA)
// 输出什么内容?
```

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


#### `init` 代码块 {id="init-code-block"}
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

`by Delegates.observable` 实现"观察者模式"的变量
观察者模式，又被称为订阅模式。最常见的场景就是：比如读者们订阅了公众号，每次公众号更新的时候，就会收到推送。而观察者模式应用到变量层面，就延伸成了：如果这个的值改变了，就通知我。

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

> 思考题：

#### `lazy` 委托的`LazyThreadSafetyMode.PUBLICATION`适用于什么样的场景？{collapsible="true"}

{collapsible="true" default-state="collapsed"}

LazyThreadSafetyMode.PUBLICATION适用于那些需要在多线程环境中进行延迟初始化，但不需要严格保证线程安全的场景。在这种模式下，lazy委托的属性值会在第一次被访问时进行初始化，并且这个值会被所有线程共享。如果初始化过程中抛出异常，它会尝试重新初始化。但是，与LazyThreadSafetyMode.SYNCHRONIZED不同，PUBLICATION模式不会阻止其他线程访问尚未初始化的值，这意味着在某些情况下，可能会有多个线程同时执行初始化操作，但最终只有第一个成功初始化的值会被保留。

这种模式适用于以下场景：

单例模式：在多线程环境中，你可能希望创建一个单例对象，但不希望在每次访问时都进行同步。`PUBLICATION`模式可以确保所有线程都使用同一个已初始化的实例，而不需要在每次访问时都进行同步。

性能优化：如果你的应用在多线程环境中运行，并且初始化操作相对耗时，使用`PUBLICATION`模式可以避免不必要的同步开销，从而提高性能。这是因为只有第一个线程会执行初始化操作，后续的线程可以直接使用已经初始化的值。

并发控制：在某些情况下，你可能需要在多个线程中并行执行初始化操作，但最终只保留一个结果。`PUBLICATION`模式通过CAS（Compare-And-Swap）操作确保只有一个线程的初始化结果被保留。

资源密集型操作：如果初始化操作涉及到资源密集型的操作（如数据库连接、网络请求等），`PUBLICATION`模式可以避免在每次访问时都重新创建资源，从而节省资源。

需要注意的是，使用`PUBLICATION`模式时，你需要确保初始化操作是幂等的，即多次执行相同的操作不会影响最终结果。此外，如果初始化过程中的操作不是线程安全的，可能会导致竞态条件或其他并发问题。在这种情况下，你可能需要考虑使用`SYNCHRONIZED`模式或者确保初始化逻辑本身是线程安全的
