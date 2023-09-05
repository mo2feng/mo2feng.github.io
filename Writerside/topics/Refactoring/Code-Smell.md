# 代码的坏味道

本章主要介绍一些不好的代码，也就是说这些代码应该被重构。

## 1. 重复代码(Duplicated Code)


* 同一个类的两个函数有相同表达式，用 *Extract Method*  提取出重复代码，分别调用；

* 两个互为兄弟的子类含有相同的表达式，先使用 *Extract Method* ，然后把提取出来的函数 *Pull Up Method* 推入超类。

* 如果只是部分相同，用 *Extract Method*  分离出相似部分和差异部分，然后使用 Form Template Method 这种模板方法设计模式。

* 如果两个毫不相关的类出现重复代码，则使用 *Extract Class* 方法将重复代码提取到一个独立类中。

##  2. 过长函数（Long Method）


函数应该尽可能小，因为小函数具有解释能力、共享能力、选择能力。

分解长函数: 当需要用注释来说明一段代码时，就需要把这部分代码写入一个独立的函数中（哪怕函数仅有一行代码）。

*Extract Method*  会把很多参数和临时变量都当做参数，可以用 *Replace Temp with Query* 消除临时变量，*Introduce Parameter Object* 和 *Preserve Whole Object* 可以将过长的参数列变得更简洁。

如果仍然有太多临时变量和参数，使用杀手锏 *Replace Method with Method Object*

条件和循环语句往往也是提炼的信号。使用*Decompose Conditional* 处理条件表达式。
应该将循环和其内的代码提炼到一个独立函数中。

## 3. 过大的类(Large Class)


应该尽可能让一个类只做一件事，而过大的类做了过多事情，需要使用 *Extract Class* 或 *Extract Subclass*。

先确定客户端如何使用该类，然后运用 *Extract Interface* 为每一种使用方式提取出一个接口。

##  4. 过长的参数列表(Long Parameter List)

太长的参数列表往往难以理解，太多参数会造成前后不一致，不易使用。

* 如果向已有对象(函数所属类的字段或者另一个参数)发出一条请求就可以取代一个参数，应该使用*Replace Parameter with Mothod*
* 可以运用 *Preserve Whole Object* 将来自同一对象的数据收集，并以改对象替换他们。
* 如果某些数据缺乏合理的对象归属，可使用 *Introduce Parameter Object* 来制造一个“参数对象”。

##  5. 发散式变化(Divergent Change)
特点： 一个类锚定了多个变化，当任意一个发生变化时，就必须对类做修改。

设计原则: 一个类应该只有一个引起改变的原因。也就是说，针对某一外界变化所有相应的修改，都只应该发生在单一类中。

针对某种原因的变化，使用 Extract Class 将它提炼到一个类中。

## 6. 散弹式修改(Shotgun Surgery)

特点： 类似发散式变化，但相反，如果每遇到变化，都必须在不同的类中做小修改，所面临的坏味道就是 散弹式修改(一个变化引起程序多出修改)。
一个变化引起多个类修改。

使用 Move Method 和 Move Field 把所有需要修改的代码放到同一个类中。

## 7. 依恋情结(Feature Envy)

一个函数对某个类的兴趣高于对自己所处类的兴趣,比如我们经常见到的函数为了计算某个值，从另一个对象那儿调用了"半打"的取值函数。

这时就应该使用 *Move Method* 将它移到该去的地方，如果对多个类都有 Feature Envy，先用 *Extract Method*  提取出多个函数。

一个函数往往会用到几个类的功能时，原则是：判断哪个类拥有最多的被此函数使用的数据，然后就把这个函数和哪些数据摆在一起。

## 8. 数据泥团(Data Clumps)

有些数据经常一起出现，比如两个类具有相同的字段、许多函数有相同的参数，这些绑定在一起出现的数据应该拥有属于它们自己的对象。

使用 *Extract Class* 将它们提炼到一个对立对象，然后将注意力转移到签名函数上，运用*Introduce Paramter Object* 或者*Preserve Whole Object* 来减肥。

##  9. 基本类型偏执(Primitive Obsession)

（结构类型的数据）使用类往往比使用基本类型更好，比如：结合数值和币种的Money类。
* 使用 *Replace Data Value with Object* 将数据值替换为对象。
* 如果想要替换的数据是类型码，则可运用*Replace Type Code With Class*
* 如果有与类型码相关的条件表达式，可运用 *Replace Type Code With SubClass* 或者*Replace Type Code with State/Strategy*

* 如果有一组应该总是放在一起的字段，可运用 *Extract Class*。
* 如果在参数列表中看到基本型数据，可以试试*Introduce Parameter Object*.
* 如果发现正从数组中挑选数据，可运用*Replace Array With Object*

##  10. switch 惊悚现身(Switch Statements)

大多数时候，一看到switch语句，就应该考虑以多态替换。

##  11. 平行继承体系(Parallel Inheritance Hierarchies)

平行集成体系是散弹式修改的特殊情况

每当为某个类增加一个子类，必须也为另一个类相应增加一个子类。

消除重复性的一般策略: 让一个继承体系的实例引用另一个继承体系的实例。

##  12. 冗余类(Lazy Class)

如果一个类没有做足够多的工作，就应该消失。如果某些子类没有做足够的工作，试试*Collapse Hierarchy*，对于几乎没用的组件，使用*Inline Class*来处理

##  13. 夸夸其谈未来性(Speculative Generality)

有些内容是用来处理未来可能发生的变化，但是往往会造成系统难以理解和维护，并且预测未来可能发生的改变很可能和最开始的设想相反。因此，如果不是必要，就不要这么做。

如果函数或类的唯一用户是测试用例 应该把它们连同其测试用例一并删除（如果用途是帮助测试用例检测正当功能，则应保留）。

## 14. 令人迷惑的暂时字段(Temporary Field)

某个字段仅为某种特定情况而设，这样的代码不易理解，因为通常认为对象在所有时候都需要它的所有字段。

把这种字段和特定情况的处理操作使用 *Extract Class* 提炼到一个独立类中。也许可以使用*Introduce Null Object* 在“变量不合法”的情况下创建一个NULL对象，从而避免写条件式代码。

##  15. 过度耦合的消息链(Message Chains)

一个对象请求另一个对象，然后再向后者请求另一个对象，然后...，这就是消息链。比如`A.getB().getC()`。采用这种方式，意味着客户代码将与对象间的关系紧密耦合。一旦对象间的关系发生任何变化，客户端都要做出相应修改。

这时就应该使用 *Hide Delegate*来删除一个消息链。有时更好的选择是：先观察消息链最终得到的对象是用来干什么的。看看能否以 提炼函数(Extract Method)把使用该对象的代码提炼到一个独立函数中，再运用 搬移函数(Move Method) 把这个函数推入消息链。

## 16. 中间人(Middle Man)

中间人负责处理委托给它的操作，如果一个类中有过多的函数都委托给其它类，那就是过度运用委托。

过度使用委托。这意味着当需求发生某些的变化的时候，这个中间人的类总是被牵连进来一并修改。这种中间人代码越多，浪费掉的时间也就越多。
中间人的代码在于过度使用和委托两点。因此解决中间人这种代码坏味道就应该从减少委托下手：

删除中间人的方法，可以使用 *Remove Middle Man*（移除中间人）这种重构技巧。

当然如果原有代码的代理类中并不怎么变化，也可以选择延迟重构，依照“事不过三，三则重构”的原则可以选择当发生变化的时候进行重构。

## 17. 狎昵关系(Inappropriate Intimacy)

两个类多于亲密，花费太多时间去探讨彼此的 private 成分。
* 狎昵关系会导致强耦合的表现；
* 而且类和类之间的职责将会变得模糊；
* 会因为访问对方的私有信息而导致过多的操作出现，或者产生封装上的妥协，让两个类纠缠不清。

可以尝试
1. 通过 Move Field （搬移属性），Move Method（搬移方法）来移动属性和方法的位置 让属性和方法移动到它们本应该出现的位置。

2. 如果直接移动属性和方法并不合适，可以尝试使用 Extract Class（提炼类）看是否能够找到公共类。
3. 如果是因为相互调用导致的问题，可以尝试 Change Bidirectional Association to Unidirectional（将双向关联改为单向关联）尝试将关联关系划清。
4. 如果是因为继承导致狎昵关系，可以尝试移除继承关系，改用代理类来实现。


## 18. 异曲同工的类(Alernative Classes with Different Interfaces)

两个函数做同一件事，却有着不同的签名。

使用 *Rename Method* 根据它们的用途重新命名。但这往往不够，请反复运用*Move Method* 将某些行为移入类，知道两者协议一致。

## 19. 不完美的类库(Incomplete Library Class)

类库的设计者不可能设计出完美的类库，当我们需要对类库进行一些修改时，可以使用以下两种方法: 如果只是修改一两个函数，使用 *Introduce Foreign Method*；如果要添加一大堆额外行为，使用 *Introduce Local Extension*。

##  20. 幼稚的数据类(Data Class)

它只拥有一些数据字段，以及用于访问这些字段的函数，除此之外一无长物。

找出这些`Getter`/`Setter`函数被其他类运用的地点。尝试以*Move Method* 把调用行为搬运到Data Class。

找出字段使用的地方，然后把相应的操作移到 Data Class 中。不久之后就可以使用*Hide Method* 把这个`Getter`/`Setter` 函数隐藏了。

## 21. 被拒绝的馈赠(Refused Bequest)

子类不想继承超类的所有函数和数据。

为子类新建一个兄弟类，不需要的函数或数据使用 *Push Down Method* 和 *Push Down Field* 下推给那个兄弟。

## 22. 过多的注释(Comments)

使用 *Extract Method*  提炼出需要注释的部分，然后用函数名来解释函数的行为。


