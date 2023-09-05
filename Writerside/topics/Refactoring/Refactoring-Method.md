# 重构的方法

<show-structure for="chapter,procedure" depth="2"/>

## 重新组织函数

###  1. 提炼函数(Extract Method)


将代码放进一个独立函数中，并让函数名称解释该函数的用途。

动机：

+ 每个函数粒度都很小，那么被复用的机会就会更大
+ 使高层函数读起来像注释
+ 函数的覆写会更容易

做法:
 
+ 创建一个新函数，根据函数的意图来命名（做什么而不是怎么做）
+ 将提炼出来的代码从源函数复制到新函数
+ 仔细检查提炼的代码，看是否引用了“作用域限于源函数”的变量（包括局部变量和额和源函数参数）
+ 检查是否有“仅用于被提炼代码”的临时变量。如果有，在目标函数中声明为临时变量。
+ 检查被提炼的代码，看看是否有任何局部变量的值被更改，如果一个临时变量的值被更改了，看看是否可以将被提炼的代码处理为一个查询，并将结果赋值给相关变量。如果很难或者如果修改的临时变量不止一个，可能需要先使用*Split Temporary Variable*,然后再尝试提炼。也可使用*Replace Temp with Query* 来消灭临时变量
+ 将提炼代码中需要读取的临时变量，当作参数传给目标函数
+ 处理完所有局部变量之后，进行编译
+ 在源函数中，将被提炼的代码替换为对目标函数的调用
+ 编译，测试

###  2. 内联函数(Inline Method)

一个函数的本体与名称同样清楚易懂。

在函数调用点插入函数本体，然后移除该函数。

###  3. 内联临时变量(Inline Temp)

一个临时变量，只被简单表达式赋值一次，而它妨碍了其它重构手法。

将所有对该变量的引用替换为对它赋值的那个表达式自身。

```java
double basePrice = anOrder.basePrice();
return basePrice > 1000;
return anOrder.basePrice() > 1000;
```

###  4. 以查询取代临时变量(Replace Temp with Query)



以临时变量保存某一表达式的运算结果，将这个表达式提炼到一个独立函数中，将所有对临时变量的引用点替换为对新函数的调用。

Replace Temp with Query 往往是 Extract Method 之前必不可少的一个步骤，因为局部变量会使代码难以提炼。

```java
double basePrice = quantity * itemPrice;
if (basePrice > 1000)
    return basePrice * 0.95;
else
    return basePrice * 0.98;
if (basePrice() > 1000)
    return basePrice() * 0.95;
else
    return basePrice() * 0.98;

// ...
double basePrice(){
    return quantity * itemPrice;
}
```

###  5. 引起解释变量(Introduce Explaining Variable)


将复杂表达式(或其中一部分)的结果放进一个临时变量， 以此变量名称来解释表达式用途。

```java
if ((platform.toUpperCase().indexOf("MAC") > -1) &&
  (browser.toUpperCase().indexOf("IE") > -1) &&
  wasInitialized() && resize > 0) {
    // do something
}
final boolean isMacOS = platform.toUpperCase().indexOf("MAC") > -1;
final boolean isIEBrower = browser.toUpperCase().indexOf("IE") > -1;
final boolean wasResized = resize > 0;

if (isMacOS && isIEBrower && wasInitialized() && wasResized) {
    // do something
}
```

###  6. 分解临时变量(Split Temporary Variable)


某个临时变量被赋值超过一次，它既不是循环变量，也不是用于收集计算结果。

针对每次赋值，创造一个独立、对应的临时变量，每个临时变量只承担一个责任。

###  7. 移除对参数的赋值(Remove Assigments to Parameters)

以一个临时变量取代对该参数的赋值。

```java
int discount (int inputVal, int quentity, int yearToDate) {
    if (inputVal > 50) inputVal -= 2;
    ...
}
int discount (int inputVal, int quentity, int yearToDate) {
    int result = inputVal;
    if (inputVal > 50) result -= 2;
    ...
}
```

###  8. 以函数对象取代函数(Replace Method with Method Object)


当对一个大型函数采用 Extract Method 时，由于包含了局部变量使得很难进行该操作。

将这个函数放进一个单独对象中，如此一来局部变量就成了对象内的字段。然后可以在同一个对象中将这个大型函数分解为多个小型函数。

###  9. 替换算法(Subsititute Algorithn)


##  在对象之间搬移特性

###  1. 搬移函数(Move Method)


类中的某个函数与另一个类进行更多交流: 调用后者或者被后者调用。

将这个函数搬移到另一个类中。

###  2. 搬移字段(Move Field)


类中的某个字段被另一个类更多地用到，这里的用到是指调用取值设值函数，应当把该字段移到另一个类中。

###  3. 提炼类(Extract Class)

某个类做了应当由两个类做的事。

应当建立一个新类，将相关的字段和函数从旧类搬移到新类。

###  4. 将类内联化(Inline Class)

与 Extract Class 相反。

###  5. 隐藏委托关系(Hide Delegate)

建立所需的函数，隐藏委托关系。

```java
class Person {
    Department department;

    public Department getDepartment() {
        return department;
    }
}

class Department {
    private Person manager;

    public Person getManager() {
        return manager;
    }
}
```

如果客户希望知道某人的经理是谁，必须获得 Department 对象，这样就对客户揭露了 Department 的工作原理。

```java
Person manager = john.getDepartment().getManager();
```

通过为 Peron 建立一个函数来隐藏这种委托关系。

```java
public Person getManager() {
    return department.getManager();
}
```

###  6. 移除中间人(Remove Middle Man)

与 Hide Delegate 相反，本方法需要移除委托函数，让客户直接调用委托类。

Hide Delegate 有很大好处，但是它的代价是: 每当客户要使用受托类的新特性时，就必须在服务器端添加一个简单的委托函数。随着受委托的特性越来越多，服务器类完全变成了一个“中间人”。

###  7. 引入外加函数(Introduce Foreign Method)


需要为提供服务的类添加一个函数，但是无法修改这个类。

可以在客户类中建立一个函数，并以第一参数形式传入一个服务类的实例，让客户类组合服务器实例。

###  8. 引入本地扩展(Introduce Local Extension)


和 Introduce Foreign Method 目的一样，但是 Introduce Local Extension 通过建立新的类来实现。有两种方式: 子类或者包装类，子类就是通过继承实现，包装类就是通过组合实现