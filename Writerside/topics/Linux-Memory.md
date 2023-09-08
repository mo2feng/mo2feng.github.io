# Linux内核的内存屏障

[英文原文链接](https://www.kernel.org/doc/Documentation/memory-barriers.txt) **作者：**[David Howells](mailto:dhowells@redhat.com)、[Paul E. McKenney](mailto:paulmck@linux.vnet.ibm.com) 


## 抽象的内存访问模型

考虑下面这个系统的抽象模型：

```
		            :                :
		            :                :
		            :                :
		+-------+   :   +--------+   :   +-------+
		|       |   :   |        |   :   |       |
		|       |   :   |        |   :   |       |
		| CPU 1 |<----->| Memory |<----->| CPU 2 |
		|       |   :   |        |   :   |       |
		|       |   :   |        |   :   |       |
		+-------+   :   +--------+   :   +-------+
		    ^       :       ^        :       ^
		    |       :       |        :       |
		    |       :       |        :       |
		    |       :       v        :       |
		    |       :   +--------+   :       |
		    |       :   |        |   :       |
		    |       :   |        |   :       |
		    +---------->| Device |<----------+
		            :   |        |   :
		            :   |        |   :
		            :   +--------+   :
		            :                :
```

每个CPU执行一个有内存访问操作的程序。在这个抽象的CPU中，内存操作的顺序是非常宽松的。假若能让程序的因果关系看起来是保持着的，CPU就可以以任意它喜欢的顺序执行内存操作。同样，只要不影响程序的结果，编译器可以以它喜欢的任何顺序安排指令。

因此，上图中，一个CPU执行内存操作的结果能被系统的其它部分感知到，因为这些操作穿过了CPU与系统其它部分之间的接口（虚线）。

例如，请考虑以下的事件序列：

```
	CPU 1		CPU 2
	===============	===============
	{ A == 1; B == 2 }
	A = 3;		x = A;
	B = 4;		y = B;
```

内存系统能看见的访问顺序可能有24种不同的组合：

```
	STORE A=3,	STORE B=4,	x=LOAD A->3,	y=LOAD B->4
	STORE A=3,	STORE B=4,	y=LOAD B->4,	x=LOAD A->3
	STORE A=3,	x=LOAD A->3,	STORE B=4,	y=LOAD B->4
	STORE A=3,	x=LOAD A->3,	y=LOAD B->2,	STORE B=4
	STORE A=3,	y=LOAD B->2,	STORE B=4,	x=LOAD A->3
	STORE A=3,	y=LOAD B->2,	x=LOAD A->3,	STORE B=4
	STORE B=4,	STORE A=3,	x=LOAD A->3,	y=LOAD B->4
	STORE B=4, ...
	...
```

因此，可能产生四种不同的值组合：

```
	x == 1, y == 2
	x == 1, y == 4
	x == 3, y == 2
	x == 3, y == 4
```

此外，一个CPU 提交store指令到存储系统，另一个CPU执行load指令时感知到的这些store的顺序可能并不是第一个CPU提交的顺序。

另一个例子，考虑下面的事件序列：

```
          CPU 1		CPU 2
	===============	===============
	{ A == 1, B == 2, C = 3, P == &A, Q == &C }
	B = 4;		Q = P;
	P = &B		D = *Q;
```

这里有一个明显的数据依赖，D的值取决于CPU 2从P取得的地址。执行结束时，下面任一结果都是有可能的；

```
	(Q == &A) and (D == 1)
	(Q == &B) and (D == 2)
	(Q == &B) and (D == 4)
```

注意：CPU 2永远不会将C的值赋给D，因为CPU在对*Q发出load指令之前会先将P赋给Q。

### 硬件操作 

一些硬件的控制接口，是一组存储单元，但这些控制寄存器的访问顺序是非常重要的。例如，考虑拥有一系列内部寄存器的以太网卡，它通过一个地址端口寄存器（A）和一个数据端口寄存器（D）访问。现在要读取编号为5的内部寄存器，可能要使用下列代码：

```
	*A = 5;
	x = *D;
```

但上面代码可能表现出下列两种顺序：

```
	STORE *A = 5, x = LOAD *D
 	x = LOAD *D, STORE *A = 5
```

其中第二个几乎肯定会导致故障，因为它在读取寄存器**之后**才设置地址值。

### 保障

下面是CPU必须要保证的最小集合：

- 任意CPU，有依赖的内存访问指令必须按顺序发出。这意味着对于

  ```
  			Q = P; D = *Q;
  		
  ```

  CPU会发出下列内存操作：

  ```
  			Q = LOAD P, D = LOAD *Q
  		
  ```

  并且总是以这种顺序。

- 在一个特定的CPU中，重叠的load和store指令在该CPU中将会看起来是有序的。这意味着对于：

  ```
  		a = *X; *X = b;
  		
  ```

  CPU发出的内存操只会是下面的顺序：

  ```
  		a = LOAD *X, STORE *X = b
  		
  ```

  对于：

  ```
  		*X = c; d = *X;
  		
  ```

  CPU只会发出：

  ```
  		STORE *X = c, d = LOAD *X
  		
  ```

  （如果load和store指令的目标内存块有重叠，则称load和store重叠了。）。

还有一些**必须要**和**一定不能**假设的东西：

- 一定不能假设无关联的load和store指令会按给定的顺序发出，这意味着对于：

  ```
  	X = *A; Y = *B; *D = Z;
  	
  ```

  我们可能得到下面的序列之一：

  ```
  	X = LOAD *A,  Y = LOAD *B,  STORE *D = Z
  	X = LOAD *A,  STORE *D = Z, Y = LOAD *B
  	Y = LOAD *B,  X = LOAD *A,  STORE *D = Z
  	Y = LOAD *B,  STORE *D = Z, X = LOAD *A
  	STORE *D = Z, X = LOAD *A,  Y = LOAD *B
  	STORE *D = Z, Y = LOAD *B,  X = LOAD *A
  ```

- 必须要假定重叠的内存访问可能会被合并或丢弃。这意味着对于

  ```
  	X = *A; Y = *(A + 4);
  ```

  我们可能得到下面的序列之一：

  ```
  	X = LOAD *A; Y = LOAD *(A + 4);
  	Y = LOAD *(A + 4); X = LOAD *A;
  	{X, Y} = LOAD {*A, *(A + 4) };
  ```

  对于：

  ```
  	*A = X; Y = *A;
  ```

  我们可能得到下面的序列之一：

  ```
  	STORE *A = X; Y = LOAD *A;
  	STORE *A = Y = X;
  ```

## 什么是内存屏障？

如上所述，没有依赖关系的内存操作实际会以随机的顺序执行，但对CPU-CPU的交互和I / O来说却是个问题。我们需要某种方式来指导编译器和CPU以约束执行顺序。

内存屏障就是这样一种干预手段。它们会给屏障两侧的内存操作强加一个偏序关系。

这种强制措施是很重要的，因为一个系统中，CPU和其它硬件可以使用各种技巧来提高性能，包括内存操作的重排、延迟和合并；预取；推测执行分支以及各种类型的缓存。内存屏障是用来禁用或抑制这些技巧的，使代码稳健地控制多个CPU和(或)设备的交互。

### 内存屏障的种类

内存屏障有四种基本类型：

1. write（或store）内存屏障。

2. write内存屏障保证：所有该屏障之前的store操作，看起来一定在所有该屏障之后的store操作之前执行。

3. write屏障仅保证store指令上的偏序关系，不要求对load指令有什么影响。

4. 随着时间推移，可以视CPU提交了一系列store操作到内存系统。在该一系列store操作中，write屏障之前的所有store操作将在该屏障后面的store操作之前执行。

5. **[!]**注意，write屏障一般与read屏障或数据依赖障碍成对出现；请参阅“SMP屏障配对”小节。

6. 数据依赖屏障。

   数据依赖屏障是read屏障的一种较弱形式。在执行两个load指令，第二个依赖于第一个的执行结果（例如：第一个load执行获取某个地址，第二个load指令取该地址的值）时，可能就需要一个数据依赖屏障，来确保第二个load指令在获取目标地址值的时候，第一个load指令已经更新过该地址。

   数据依赖屏障仅保证相互依赖的load指令上的偏序关系，不要求对store指令，无关联的load指令以及重叠的load指令有什么影响。

   如[write（或store）内存屏障](https://ifeve.com/linux-memory-barriers/#write-barrier)中提到的，可以视系统中的其它CPU提交了一些列store指令到内存系统，然后the CPU being considered就能感知到。由该CPU发出的数据依赖屏障可以确保任何在该屏障之前的load指令，如果该load指令的目标被另一个CPU的存储（store）指令修改，在屏障执行完成之后，所有在该load指令对应的store指令之前的store指令的更新都会被所有在数据依赖屏障之后的load指令感知。

   参考”内存屏障顺序实例”小节图中的顺序约束。

   **[!]**注意：第一个load指令确实必须有一个数据依赖，而不是控制依赖。如果第二个load指令的目标地址依赖于第一个load，但是这个依赖是通过一个条件语句，而不是实际加载的地址本身，那么它是一个控制依赖，需要一个完整的read屏障或更强的屏障。查看”控制依赖”小节，了解更多信息。

   [！]注意：数据依赖屏障一般与写障碍成对出现；看到“SMP屏障配对”章节。

7. read（或load）内存屏障。

   read屏障是数据依赖屏障外加一个保证，保证所有该屏障之前的load操作，看起来一定在所有该屏障之后的load操作之前执行。

   read屏障仅保证load指令上的偏序关系，不要求对store指令有什么影响。

   read屏障包含了数据依赖屏障的功能，因此可以替代数据依赖屏障。

   **[!]**注意：read屏障通常与write屏障成对出现；请参阅“SMP屏障配对”小节。

8. 通用内存屏障。

   通用屏障确保所有该屏障之前的load和store操作，看起来一定在所有屏障之后的load和store操作之前执行。

   通用屏障能保证load和store指令上的偏序关系。

   通用屏障包含了read屏障和write屏障，因此可以替代它们两者。

一对隐式的屏障变种：

1. LOCK操作。

   LOCK操作可以看作是一个单向渗透的屏障。它保证所有在LOCK之后的内存操作看起来一定在LOCK操作后才发生。

   LOCK操作之前的内存操作可能会在LOCK完成之后发生。

   LOCK操作几乎总是与UNLOCK操作成对出现。

2. UNLOCK操作。

   这也是一个单向渗透屏障。它保证所有UNLOCK操作之前的内存操作看起来一定在UNLOCK操作之前发生。

   UNLOCK操作之后的内存操作可能会在UNLOCK完成之前发生。

   LOCK和UNLOCK操作严格保证自己对指令的顺序。

   使用了LOCK和UNLOCK操作，一般就不需要其它类型的内存屏障了（但要注意在”MMIO write屏障”一节中提到的例外情况）。

仅当两个CPU之间或者CPU与其它设备之间有交互时才需要屏障。如果可以确保某段代码中不会有任何这种交互，那么这段代码就不需要内存屏障。

注意，这些是最低限度的保证。不同的架构可能会提供更多的保证，但是它们不是必须的，不应该依赖其写代码（they may not be relied upon outside of arch specific code）。

### 什么是内存屏障不能确保的？

有一些事情，Linux内核的内存屏障并不保证：

- 不能保证，任何在内存屏障之前的内存访问操作能在内存屏障指令执行完成时也执行完成；内存屏障相当于在CPU的访问队列中划了一条界线，相应类型的指令不能跨过该界线。

- 不能保证，一个CPU发出的内存屏障能对另一个CPU或该系统中的其它硬件有任何直接影响。只会间接影响到第二个CPU看第一个CPU的存取操作发生的顺序，但请看下一条：

- 不能保证，一个CPU看到第二个CPU存取操作的结果的顺序，即使第二个CPU使用了内存屏障，除非第一个CPU也使用与第二个CPU相匹配的内存屏障（见”SMP屏障配对”小节）。

- 不能保证，一些CPU相关的硬件[*]不会对内存访问重排序。 CPU缓存的一致性机制会在多个CPU之间传播内存屏障的间接影响，但可能不是有序的。

  

  [*]总线主控DMA和一致性相关信息，请参阅：

  Documentation/PCI/pci.txt

  Documentation/PCI/PCI-DMA-mapping.txt

  Documentation/DMA-API.txt

### 数据依赖屏障

数据依赖屏障的使用条件有点微妙，且并不总是很明显。为了阐明问题，考虑下面的事件序列：

```
	CPU 1		CPU 2
	===============	===============
	{ A == 1, B == 2, C = 3, P == &A, Q == &C }
	B = 4;
	<write barrier>
	P = &B
			Q = P;
			D = *Q;
```

这里很明显存在数据依赖，看起来在执行结束后，Q不是＆A就是＆B，并且：

```
	(Q == &A) 意味着 (D == 1)
	(Q == &B) 意味着 (D == 4)
```

但是，从CPU 2可能先感知到P更新，然后才感知到B更新，这就导致了以下情况：

```
	(Q == &B) and (D == 2) ????
```

虽然这可能看起来像是一致性或因果关系维护失败，但实际并不是的，且这种行为在一些真实的CPU上也可以观察到（如DEC Alpha）。

为了处理这个问题，需要在地址load和数据load之间插入一个数据依赖屏障或一个更强的屏障：

```
	CPU 1		CPU 2
	===============	===============
	{ A == 1, B == 2, C = 3, P == &A, Q == &C }
	B = 4;
	<write barrier>
	P = &B
			Q = P;
			<data dependency barrier>
			D = *Q;
```

这将迫使结果为前两种情况之一，而防止了第三种可能性的出现。

**[!]**注意：这种极其有违直觉的场景，在有多个独立缓存（split caches）的机器上很容易出现，比如：一个cache bank处理偶数编号的缓存行，另外一个cache bank处理奇数编号的缓存行。指针P可能存储在奇数编号的缓存行，变量B可能存储在偶数编号的缓存行中。然后，如果在读取CPU缓存的时候，偶数的bank非常繁忙，而奇数bank处于闲置状态，就会出现指针P（&B）是新值，但变量B（2）是旧值的情况。

另外一个需要数据依赖屏障的例子是从内存中读取一个数字，然后用来计算某个数组的下标；

```
	CPU 1		CPU 2
	===============	===============
	{ M[0] == 1, M[1] == 2, M[3] = 3, P == 0, Q == 3 }
	M[1] = 4;
	<write barrier>
	P = 1
			Q = P;
			<data dependency barrier>
			D = M[Q];
```

数据依赖屏障对RCU系统是很重要的，如，看include/linux/ rcupdate.h的rcu_dereference()函数。这个函数允许RCU的指针被替换为一个新的值，而这个新的值还没有完全的初始化。

更多详细的例子参见”高速缓存一致性”小节。

### 控制依赖

控制依赖需要一个完整的read内存屏障来保证其正确性，而不简单地只是数据依赖屏障。考虑下面的代码：

```
	q = &a;
	if (p)
		q = &b;
	<data dependency barrier>
	x = *q;
```

这不会产生想要的结果，因为这里没有实际的数据依赖，而是一个控制依赖，CPU可能会提前预测结果而使if语句短路。在这样的情况下，实际需要的是下面的代码：

```
	q = &a;
	if (p)
		q = &b;
	<read barrier>
	x = *q;
```

### SMP屏障配对

当处理CPU-CPU之间的交互时，相应类型的内存屏障总应该是成对出现的。缺少相应的配对屏障几乎可以肯定是错误的。

write屏障应始终与数据依赖屏障或者read屏障配对，虽然通用内存屏障也是可以的。同样地，read屏障或数据依赖屏障应至少始终与write屏障配对使用，虽然通用屏障仍然也是可以的：

```
	CPU 1		CPU 2
	===============	===============
	a = 1;
	<write barrier>
	b = 2;		x = b;
			<read barrier>
			y = a;
```

或者:

```
	CPU 1		CPU 2
	===============	===============================
	a = 1;
	<write barrier>
	b = &a;		x = b;
			<data dependency barrier>
			y = *x;
```

基本上，那个位置的read屏障是必不可少的，尽管可以是“更弱“的类型。

**[!]**注意：write屏障之前的store指令通常与read屏障或数据依赖屏障后的load指令相匹配，反之亦然：

```
	CPU 1                           CPU 2
	===============                 ===============
	a = 1;           }----   --->{  v = c
	b = 2;           }    \ /    {  w = d
	<write barrier>        \        <read barrier>
	c = 3;           }    / \    {  x = a;
	d = 4;           }----   --->{  y = b;
```

### 内存屏障序列实例

首先，write屏障确保store操作的偏序关系。考虑以下事件序列：

```
	CPU 1
	=======================
	STORE A = 1
	STORE B = 2
	STORE C = 3
	<write barrier>
	STORE D = 4
	STORE E = 5
```

这一连串的事件提交给内存一致性系统的顺序，可以使系统其它部分感知到无序集合{ STORE A,STORE B, STORE C } 中的操作都发生在无序集合{ STORE D, STORE E}中的操作之前：

```
	+-------+       :      :
	|       |       +------+
	|       |------>| C=3  |     }     /\
	|       |  :    +------+     }-----  \  -----> Events perceptible to
	|       |  :    | A=1  |     }        \/       the rest of the system
	|       |  :    +------+     }
	| CPU 1 |  :    | B=2  |     }
	|       |       +------+     }
	|       |   wwwwwwwwwwwwwwww }   <--- At this point the write barrier
	|       |       +------+     }        requires all stores prior to the
	|       |  :    | E=5  |     }        barrier to be committed before
	|       |  :    +------+     }        further stores may take place
	|       |------>| D=4  |     }
	|       |       +------+
	+-------+       :      :
	                   |
	                   | Sequence in which stores are committed to the
	                   | memory system by CPU 1
	                   V
```

其次，数据依赖屏障确保于有数据依赖关系的load指令间的偏序关系。考虑以下事件序列：

```
	CPU 1			CPU 2
	=======================	=======================
		{ B = 7; X = 9; Y = 8; C = &Y }
	STORE A = 1
	STORE B = 2
	<write barrier>
	STORE C = &B		LOAD X
	STORE D = 4		LOAD C (gets &B)
				LOAD *C (reads B)
```

在没有其它干涉时，尽管CPU 1发出了write屏障，CPU2感知到的CPU1上事件的顺序也可能是随机的：

```
	+-------+       :      :                :       :
	|       |       +------+                +-------+  | Sequence of update
	|       |------>| B=2  |-----       --->| Y->8  |  | of perception on
	|       |  :    +------+     \          +-------+  | CPU 2
	| CPU 1 |  :    | A=1  |      \     --->| C->&Y |  V
	|       |       +------+       |        +-------+
	|       |   wwwwwwwwwwwwwwww   |        :       :
	|       |       +------+       |        :       :
	|       |  :    | C=&B |---    |        :       :       +-------+
	|       |  :    +------+   \   |        +-------+       |       |
	|       |------>| D=4  |    ----------->| C->&B |------>|       |
	|       |       +------+       |        +-------+       |       |
	+-------+       :      :       |        :       :       |       |
	                               |        :       :       |       |
	                               |        :       :       | CPU 2 |
	                               |        +-------+       |       |
	    Apparently incorrect --->  |        | B->7  |------>|       |
	    perception of B (!)        |        +-------+       |       |
	                               |        :       :       |       |
	                               |        +-------+       |       |
	    The load of X holds --->    \       | X->9  |------>|       |
	    up the maintenance           \      +-------+       |       |
	    of coherence of B             ----->| B->2  |       +-------+
	                                        +-------+
	                                        :       :
```

在上述的例子中，尽管load *C（可能是B）在load C之后，但CPU 2感知到的B却是7；

然而，在CPU2中，如果数据依赖屏障放置在loadC和load *C（即：B）之间：

```
	CPU 1			CPU 2
	=======================	=======================
		{ B = 7; X = 9; Y = 8; C = &Y }
	STORE A = 1
	STORE B = 2
	<write barrier>
	STORE C = &B		LOAD X
	STORE D = 4		LOAD C (gets &B)
				<data dependency barrier>
				LOAD *C (reads B)
```

将发生以下情况：

```
	+-------+       :      :                :       :
	|       |       +------+                +-------+
	|       |------>| B=2  |-----       --->| Y->8  |
	|       |  :    +------+     \          +-------+
	| CPU 1 |  :    | A=1  |      \     --->| C->&Y |
	|       |       +------+       |        +-------+
	|       |   wwwwwwwwwwwwwwww   |        :       :
	|       |       +------+       |        :       :
	|       |  :    | C=&B |---    |        :       :       +-------+
	|       |  :    +------+   \   |        +-------+       |       |
	|       |------>| D=4  |    ----------->| C->&B |------>|       |
	|       |       +------+       |        +-------+       |       |
	+-------+       :      :       |        :       :       |       |
	                               |        :       :       |       |
	                               |        :       :       | CPU 2 |
	                               |        +-------+       |       |
	                               |        | X->9  |------>|       |
	                               |        +-------+       |       |
	  Makes sure all effects --->   \   ddddddddddddddddd   |       |
	  prior to the store of C        \      +-------+       |       |
	  are perceptible to              ----->| B->2  |------>|       |
	  subsequent loads                      +-------+       |       |
	                                        :       :       +-------+
```

第三，read屏障确保load指令上的偏序关系。考虑以下的事件序列：

```
	CPU 1			CPU 2
	=======================	=======================
		{ A = 0, B = 9 }
	STORE A=1
	<write barrier>
	STORE B=2
				LOAD B
				LOAD A
```

在没有其它干涉时，尽管CPU1发出了一个write屏障，CPU 2感知到的CPU 1中事件的顺序也可能是随机的：

```
	+-------+       :      :                :       :
	|       |       +------+                +-------+
	|       |------>| A=1  |------      --->| A->0  |
	|       |       +------+      \         +-------+
	| CPU 1 |   wwwwwwwwwwwwwwww   \    --->| B->9  |
	|       |       +------+        |       +-------+
	|       |------>| B=2  |---     |       :       :
	|       |       +------+   \    |       :       :       +-------+
	+-------+       :      :    \   |       +-------+       |       |
	                             ---------->| B->2  |------>|       |
	                                |       +-------+       | CPU 2 |
	                                |       | A->0  |------>|       |
	                                |       +-------+       |       |
	                                |       :       :       +-------+
	                                 \      :       :
	                                  \     +-------+
	                                   ---->| A->1  |
	                                        +-------+
	                                        :       :
```

然而，如果在CPU2上的load A和load B之间放置一个read屏障：

```
	CPU 1			CPU 2
	=======================	=======================
		{ A = 0, B = 9 }
	STORE A=1
	<write barrier>
	STORE B=2
				LOAD B
				<read barrier>
				LOAD A
```

CPU1上的偏序关系将能被CPU2正确感知到：

```
	+-------+       :      :                :       :
	|       |       +------+                +-------+
	|       |------>| A=1  |------      --->| A->0  |
	|       |       +------+      \         +-------+
	| CPU 1 |   wwwwwwwwwwwwwwww   \    --->| B->9  |
	|       |       +------+        |       +-------+
	|       |------>| B=2  |---     |       :       :
	|       |       +------+   \    |       :       :       +-------+
	+-------+       :      :    \   |       +-------+       |       |
	                             ---------->| B->2  |------>|       |
	                                |       +-------+       | CPU 2 |
	                                |       :       :       |       |
	                                |       :       :       |       |
	  At this point the read ---->   \  rrrrrrrrrrrrrrrrr   |       |
	  barrier causes all effects      \     +-------+       |       |
	  prior to the storage of B        ---->| A->1  |------>|       |
	  to be perceptible to CPU 2            +-------+       |       |
	                                        :       :       +-------+
```

为了更彻底说明这个问题，考虑read屏障的两侧都有load A将发生什么：

```
	CPU 1			CPU 2
	=======================	=======================
		{ A = 0, B = 9 }
	STORE A=1
	<write barrier>
	STORE B=2
				LOAD B
				LOAD A [first load of A]
				<read barrier>
				LOAD A [second load of A]
```

即使两个load A都发生在loadB之后，它们仍然可能获得不同的值：

```
	+-------+       :      :                :       :
	|       |       +------+                +-------+
	|       |------>| A=1  |------      --->| A->0  |
	|       |       +------+      \         +-------+
	| CPU 1 |   wwwwwwwwwwwwwwww   \    --->| B->9  |
	|       |       +------+        |       +-------+
	|       |------>| B=2  |---     |       :       :
	|       |       +------+   \    |       :       :       +-------+
	+-------+       :      :    \   |       +-------+       |       |
	                             ---------->| B->2  |------>|       |
	                                |       +-------+       | CPU 2 |
	                                |       :       :       |       |
	                                |       :       :       |       |
	                                |       +-------+       |       |
	                                |       | A->0  |------>| 1st   |
	                                |       +-------+       |       |
	  At this point the read ---->   \  rrrrrrrrrrrrrrrrr   |       |
	  barrier causes all effects      \     +-------+       |       |
	  prior to the storage of B        ---->| A->1  |------>| 2nd   |
	  to be perceptible to CPU 2            +-------+       |       |
	                                        :       :       +-------+
```

但是，在read屏障完成之前，CPU1对A的更新就可能被CPU2看到：

```
	+-------+       :      :                :       :
	|       |       +------+                +-------+
	|       |------>| A=1  |------      --->| A->0  |
	|       |       +------+      \         +-------+
	| CPU 1 |   wwwwwwwwwwwwwwww   \    --->| B->9  |
	|       |       +------+        |       +-------+
	|       |------>| B=2  |---     |       :       :
	|       |       +------+   \    |       :       :       +-------+
	+-------+       :      :    \   |       +-------+       |       |
	                             ---------->| B->2  |------>|       |
	                                |       +-------+       | CPU 2 |
	                                |       :       :       |       |
	                                 \      :       :       |       |
	                                  \     +-------+       |       |
	                                   ---->| A->1  |------>| 1st   |
	                                        +-------+       |       |
	                                    rrrrrrrrrrrrrrrrr   |       |
	                                        +-------+       |       |
	                                        | A->1  |------>| 2nd   |
	                                        +-------+       |       |
	                                        :       :       +-------+
```

如果load B == 2，可以保证第二次load A总是等于 1。但是不能保证第一次load A的值，A == 0或A == 1都可能会出现。

### read内存屏障与load预加载

许多CPU都会预测并提前加载：即，当系统发现它即将需要从内存中加载一个条目时，系统会寻找没有其它load指令占用总线资源的时候提前加载 —— 即使还没有达到指令执行流中的该点。这使得实际的load指令可能会立即完成，因为CPU已经获得了值。

也可能CPU根本不会使用这个值，因为执行到了另外的分支而绕开了这个load – 在这种情况下，它可以丢弃该值或仅是缓存该值供以后使用。

考虑下面的场景：

```
	CPU 1	   		CPU 2
	=======================	=======================
	 	   		LOAD B
	 	   		DIVIDE		} Divide instructions generally
	 	   		DIVIDE		} take a long time to perform
	 	   		LOAD A
```

可能出现：

```
	                                        :       :       +-------+
	                                        +-------+       |       |
	                                    --->| B->2  |------>|       |
	                                        +-------+       | CPU 2 |
	                                        :       : DIVIDE|       |
	                                        +-------+       |       |
	The CPU being busy doing a --->     --->| A->0  |~~~~   |       |
	division speculates on the              +-------+   ~   |       |
	LOAD of A                               :       :   ~   |       |
	                                        :       : DIVIDE|       |
	                                        :       :   ~   |       |
	Once the divisions are complete -->     :       :   ~-->|       |
	the CPU can then perform the            :       :       |       |
	LOAD with immediate effect              :       :       +-------+
```

在第二个LOAD指令之前，放置一个read屏障或数据依赖屏障：

```
	CPU 1	   		CPU 2
	=======================	=======================
	 	   		LOAD B
	 	   		DIVIDE
	 	   		DIVIDE
				<read barrier>
	 	   		LOAD A
```

是否强制重新获取预取的值，在一定程度上依赖于使用的屏障类型。如果值没有发送变化，将直接使用预取的值：

```
	                                        :       :       +-------+
	                                        +-------+       |       |
	                                    --->| B->2  |------>|       |
	                                        +-------+       | CPU 2 |
	                                        :       : DIVIDE|       |
	                                        +-------+       |       |
	The CPU being busy doing a --->     --->| A->0  |~~~~   |       |
	division speculates on the              +-------+   ~   |       |
	LOAD of A                               :       :   ~   |       |
	                                        :       : DIVIDE|       |
	                                        :       :   ~   |       |
	                                        :       :   ~   |       |
	                                    rrrrrrrrrrrrrrrr~   |       |
	                                        :       :   ~   |       |
	                                        :       :   ~-->|       |
	                                        :       :       |       |
	                                        :       :       +-------+
```

但如果另一个CPU有更新该值或者使该值失效，就必须重新加载该值：

```
	                                        :       :       +-------+
	                                        +-------+       |       |
	                                    --->| B->2  |------>|       |
	                                        +-------+       | CPU 2 |
	                                        :       : DIVIDE|       |
	                                        +-------+       |       |
	The CPU being busy doing a --->     --->| A->0  |~~~~   |       |
	division speculates on the              +-------+   ~   |       |
	LOAD of A                               :       :   ~   |       |
	                                        :       : DIVIDE|       |
	                                        :       :   ~   |       |
	                                        :       :   ~   |       |
	                                    rrrrrrrrrrrrrrrrr   |       |
	                                        +-------+       |       |
	The speculation is discarded --->   --->| A->1  |------>|       |
	and an updated value is                 +-------+       |       |
	retrieved                               :       :       +-------+
```

### 传递性

传递性是有关顺序的一个非常直观的概念，但是真实的计算机系统往往并不保证。下面的例子演示传递性（也可称为“积累律（cumulativity）”）：

```
	CPU 1			CPU 2			CPU 3
	=======================	=======================	=======================
		{ X = 0, Y = 0 }
	STORE X=1		LOAD X			STORE Y=1
				<general barrier>	<general barrier>
				LOAD Y			LOAD X
```

假设CPU 2 的load X返回1、load Y返回0。这表明，从某种意义上来说，CPU 2的LOAD X在CPU 1 store X之后，CPU 2的load y在CPU 3的store y 之前。问题是“CPU 3的 load X是否可能返回0？”

因为，从某种意义上说，CPU 2的load X在CPU 1的store之后，我们很自然地希望CPU 3的load X必须返回1。这就是传递性的一个例子：如果在CPU B上执行了一个load指令，随后CPU A 又对相同位置进行了load操作，那么，CPU A load的值要么和CPU B load的值相同，要么是个更新的值。

在Linux内核中，使用通用内存屏障能保证传递性。因此，在上面的例子中，如果从CPU 2的load X指令返回1，且其load Y返回0，那么CPU 3的load X也必须返回1。

但是，read或write屏障不保证传递性。例如，将上述例子中的通用屏障改为read屏障，如下所示：

```
	CPU 1			CPU 2			CPU 3
	=======================	=======================	=======================
		{ X = 0, Y = 0 }
	STORE X=1		LOAD X			STORE Y=1
				<read barrier>		<general barrier>
				LOAD Y			LOAD X
```

这就破坏了传递性：在本例中，CPU 2的load X返回1，load Y返回0，但是CPU 3的load X返回0是完全合法的。

关键点在于，虽然CPU 2的read屏障保证了CPU2上的load指令的顺序，但它并不能保证CPU 1上的store顺序。因此，如果这个例子运行所在的CPU 1和2共享了存储缓冲区或某一级缓存，CPU 2可能会提前获得到CPU 1写入的值。因此，需要通用屏障来确保所有的CPU都遵守CPU1和CPU2的访问组合顺序。

要重申的是，如果你的代码需要传递性，请使用通用屏障。

## 显式内核屏障

Linux内核有多种不同的屏障，工作在不同的层上：

- 编译器屏障。
- CPU内存屏障。
- MMIO write屏障。

### 编译器屏障

Linux内核有一个显式的编译器屏障函数，用于防止编译器将内存访问从屏障的一侧移动到另一侧：

```
barrier();
```

这是一个通用屏障 – 不存在弱类型的编译屏障。

编译屏障并不直接影响CPU，CPU依然可以按照它所希望的顺序进行重排序。

### CPU内存屏障

Linux内核有8个基本的CPU内存屏障：

```
	TYPE		MANDATORY		SMP CONDITIONAL
	===============	=======================	===========================
	GENERAL		mb()			smp_mb()
	WRITE		wmb()			smp_wmb()
	READ		rmb()			smp_rmb()
	DATA DEPENDENCY	read_barrier_depends()	smp_read_barrier_depends()
```

除了数据依赖屏障之外，其它所有屏障都包含了编译器屏障的功能。数据依赖屏障不强加任何额外的编译顺序。

旁白：在数据依赖的情况下，可能希望编译器以正确的顺序发出load指令（如:’a[b]’，将会在load a[b]之前load b），但在C规范下并不能保证如此，编译器可能不会预先推测b的值（即，等于1），然后在load b之前先load a（即，tmp = a [1];if（b！= 1）tmp = a[b];）。还有编译器重排序的问题，编译器load a[b]之后重新load b，这样，b就拥有比a[b]更新的副本。关于这些问题尚未形成共识，然而ACCESS_ONCE宏是解决这个问题很好的开始。

在单处理器编译系统中，SMP内存屏障将退化为编译屏障，因为它假定CPU可以保证自身的一致性，并且可以正确的处理重叠访问。

**[!]**注意：SMP内存屏障必须用在SMP系统中来控制引用共享内存的顺序，使用锁也可以满足需求。

强制性屏障不应该被用来控制SMP，因为强制屏障在UP系统中会产生过多不必要的开销。但是，它们可以用于控制在通过松散内存I / O窗口访问的MMIO操作。即使在非SMP系统中，这些也是必须的，因为它们可以禁止编译器和CPU的重排从而影响内存操作的顺序。

下面是些更高级的屏障函数：

```
(*) set_mb(var, value)
```

这个函数将值赋给变量，然后在其后插入一个完整的内存屏障，根据不同的实现。在UP编译器中，不能保证插入编译器屏障之外的屏障。

```
 (*) smp_mb__before_atomic_dec();
 (*) smp_mb__after_atomic_dec();
 (*) smp_mb__before_atomic_inc();
 (*) smp_mb__after_atomic_inc();
```

这些都是用于原子加，减，递增和递减而不用返回值的，主要用于引用计数。这些函数并不包含内存屏障。

例如，考虑下面的代码片段，它标记死亡的对象， 然后将该对象的引用计数减1：

```
	obj->dead = 1;
	smp_mb__before_atomic_dec();
	atomic_dec(&obj->ref_count);
```

这可以确保设置对象的死亡标记是在引用计数递减之前；

更多信息参见Documentation/atomic_ops.txt ，“Atomic operations” 章节介绍了它的使用场景。

```
 (*) smp_mb__before_clear_bit(void);
 (*) smp_mb__after_clear_bit(void);
```

这些类似于用于原子自增，自减的屏障。他们典型的应用场景是按位解锁操作，必须注意，因为这里也没有隐式的内存屏障。

考虑通过清除一个lock位来实现解锁操作。 clear_bit()函数将需要像下面这样使用内存屏障：

```
	smp_mb__before_clear_bit();
	clear_bit( ... );
```

这可以防止在clear之前的内存操作跑到clear后面。UNLOCK的参考实现见”锁的功能”小节。

更多信息见Documentation/atomic_ops.txt ， “Atomic operations“章节有关于使用场景的介绍；

### MMIO write屏障

对于内存映射I / O写操作，Linux内核也有个特殊的障碍；

```
mmiowb();
```

这是一个强制性写屏障的变体，保证对弱序I / O区的写操作有偏序关系。其影响可能超越CPU和硬件之间的接口，且能实际地在一定程度上影响到硬件。

更多信息参见”锁与I / O访问”章节。

## 隐式内核内存屏障 

Linux内核中的一些其它的功能暗含着内存屏障，主要是锁和调度功能。

该规范是一个最低限度的保证，任何特定的体系结构都可能提供更多的保证，但是在特定体系结构之外不能依赖它们。

### 锁功能 

Linux内核有很多锁结构：

- 自旋锁
- R / W自旋锁
- 互斥
- 信号量
- R / W信号量
- RCU

所有的情况下，它们都是LOCK操作和UNLOCK操作的变种。这些操作都隐含着特定的屏障：

1. LOCK操作的含义：
2. 在LOCK操作之后的内存操作将会在LOCK操作结束之后完成；

3. 在LOCK操作之前的内存操作可能在LOCK操作结束之后完成；

4. UNLOCK操作的含义：
5. 在UNLOCK操作之前的内存操作将会在UNLOCK操作结束之前完成；

6. 在UNLOCK操作之后的内存操作可能在UNLOCK操作结束之前完成；

7. LOCK与LOCK的含义：
8. 在一个LOCK之前的其它LOCK操作一定在该LOCK结束之前完成；

9. LOCK与UNLOCK的含义：
10. 在某个UNLOCK之前的所有其它LOCK操作一定在该UNLOCK结束之前完成；

11. 在某个LOCK之前的所有其它UNLOCK操作一定在该LOCK结束之前完成；

12. 失败的有条件锁的含义：
13. 某些锁操作的变种可能会失败，要么是由于无法立即获得锁，要么是在休眠等待锁可用的同时收到了一个解除阻塞的信号。失败的锁操作并不暗含任何形式的屏障。

因此，根据（1），（2）和（4），一个无条件的LOCK后面跟着一个UNLOCK操作相当于一个完整的屏障，但一个UNLOCK后面跟着一个LOCK却不是。

**[!]**注意：将LOCK和UNLOCK作为单向屏障的一个结果是，临界区外的指令可能会移到临界区里。

LOCK后跟着一个UNLOCK并不认为是一个完整的屏障，因为存在LOCK之前的存取发生在LOCK之后，UNLOCK之后的存取在UNLOCK之前发生的可能性，这样，两个存取操作的顺序就可能颠倒：

```
	*A = a;
	LOCK
	UNLOCK
	*B = b;
```

可能会发生：

```
	LOCK, STORE *B, STORE *A, UNLOCK
```

锁和信号量在UP编译系统中不保证任何顺序，所以在这种情况下根本不能考虑为屏障 —— 尤其是对于I / O访问 —— 除非结合中断禁用操作。

更多信息请参阅”CPU之间的锁屏障”章节。

考虑下面的例子：

```
	*A = a;
	*B = b;
	LOCK
	*C = c;
	*D = d;
	UNLOCK
	*E = e;
	*F = f;
```

以下的顺序是可以接受的：

```
	LOCK, {*F,*A}, *E, {*C,*D}, *B, UNLOCK
```

**[+]** Note that {*F,*A} indicates a combined access.

但下列情形的，是不能接受的：

```
	{*F,*A}, *B,	LOCK, *C, *D,	UNLOCK, *E
	*A, *B, *C,	LOCK, *D,	UNLOCK, *E, *F
	*A, *B,		LOCK, *C,	UNLOCK, *D, *E, *F
	*B,		LOCK, *C, *D,	UNLOCK, {*F,*A}, *E
```

### 中断禁用功能

禁止中断（等价于LOCK）和允许中断（等价于UNLOCK）仅可充当编译屏障。所以，如果某些场景下需要内存或I / O屏障，必须通过其它的手段来提供。

### 休眠和唤醒功能

一个全局数据标记的事件上的休眠和唤醒，可以被看作是两块数据之间的交互：正在等待的任务的状态和标记这个事件的全局数据。为了确保正确的顺序，进入休眠的原语和唤醒的原语都暗含了某些屏障。

首先，通常一个休眠任务执行类似如下的事件序列：

```
	for (;;) {
		set_current_state(TASK_UNINTERRUPTIBLE);
		if (event_indicated)
			break;
		schedule();
	}
```

set_current_state()会在改变任务状态后自动插入一个通用内存屏障；

```
	CPU 1
	===============================
	set_current_state();
	  set_mb();
	    STORE current->state
	    <general barrier>
	LOAD event_indicated
```

set_current_state（）可能包含在下面的函数中：

```
	prepare_to_wait();
	prepare_to_wait_exclusive();
```

因此，在设置状态后，这些函数也暗含了一个通用内存屏障。上面的各个函数又被封装在其它函数中，所有这些函数都在对应的地方插入了内存屏障；

```
	wait_event();
	wait_event_interruptible();
	wait_event_interruptible_exclusive();
	wait_event_interruptible_timeout();
	wait_event_killable();
	wait_event_timeout();
	wait_on_bit();
	wait_on_bit_lock();
```

其次，执行正常唤醒的代码如下：

```
	event_indicated = 1;
	wake_up(&event_wait_queue);
```

或：

```
	event_indicated = 1;
	wake_up_process(event_daemon);
```

类似wake_up()的函数都暗含一个内存屏障。当且仅当他们唤醒某个任务的时候。任务状态被清除之前内存屏障执行，也即是在设置唤醒标志事件的store操作和设置TASK_RUNNING的store操作之间：

```
	CPU 1				CPU 2
	===============================	===============================
	set_current_state();		STORE event_indicated
	  set_mb();			wake_up();
	    STORE current->state	  <write barrier>
	    <general barrier>		  STORE current->state
	LOAD event_indicated
```

可用唤醒函数包括：

```
	complete();
	wake_up();
	wake_up_all();
	wake_up_bit();
	wake_up_interruptible();
	wake_up_interruptible_all();
	wake_up_interruptible_nr();
	wake_up_interruptible_poll();
	wake_up_interruptible_sync();
	wake_up_interruptible_sync_poll();
	wake_up_locked();
	wake_up_locked_poll();
	wake_up_nr();
	wake_up_poll();
	wake_up_process();
```

**[!]**注意：在休眠任务执行set_current_state()之后，若要load唤醒前store指令存储的值，休眠和唤醒所暗含的内存屏障都不能保证唤醒前多个store指令的顺序。例如：休眠函数如下

```
	set_current_state(TASK_INTERRUPTIBLE);
	if (event_indicated)
		break;
	__set_current_state(TASK_RUNNING);
	do_something(my_data);
```

以及唤醒函数如下：

```
	my_data = value;
	event_indicated = 1;
	wake_up(&event_wait_queue);
```

并不能保证休眠函数在对my_data做过修改之后能够感知到event_indicated的变化。在这种情况下，两侧的代码必须在隔离数据访问之间插入自己的内存屏障。因此，上面的休眠任务应该这样：

```
	set_current_state(TASK_INTERRUPTIBLE);
	if (event_indicated) {
		smp_rmb();
		do_something(my_data);
	}
```

以及唤醒者应该做的：

```
	my_data = value;
	smp_wmb();
	event_indicated = 1;
	wake_up(&event_wait_queue);
```

### 其它函数

其它暗含内存屏障的函数：

- schedule()以及类似函数暗含了完整内存屏障。

## CPU之间的锁屏障效应

在SMP系统中，锁原语提供了更加丰富的屏障类型：在任意特定的锁冲突的情况下，会影响其它CPU上的内存访问顺序。

### 锁与内存访问

考虑下面的场景：系统有一对自旋锁（M）、（Q）和三个CPU，然后发生以下的事件序列：

```
	CPU 1				CPU 2
	===============================	===============================
	*A = a;				*E = e;
	LOCK M				LOCK Q
	*B = b;				*F = f;
	*C = c;				*G = g;
	UNLOCK M			UNLOCK Q
	*D = d;				*H = h;
```



对CPU 3来说， *A到*H的存取顺序是没有保证的，不同于单独的锁在单独的CPU上的作用。例如，它可能感知的顺序如下：

```
	*E, LOCK M, LOCK Q, *G, *C, *F, *A, *B, UNLOCK Q, *D, *H, UNLOCK M
```

但它不会看到任何下面的场景：

```
	*B, *C or *D 在 LOCK M 之前
	*A, *B or *C 在 UNLOCK M 之后
	*F, *G or *H 在 LOCK Q 之前
	*E, *F or *G 在 UNLOCK Q 之后
```

但是，如果发生以下情况：

```
	CPU 1				CPU 2
	===============================	===============================
	*A = a;
	LOCK M		[1]
	*B = b;
	*C = c;
	UNLOCK M	[1]
	*D = d;				*E = e;
					LOCK M		[2]
					*F = f;
					*G = g;
					UNLOCK M	[2]
					*H = h;
```

CPU 3可能会看到：

```
	*E, LOCK M [1], *C, *B, *A, UNLOCK M [1],
		LOCK M [2], *H, *F, *G, UNLOCK M [2], *D
```

但是，假设CPU 1先得到锁，CPU 3将不会看到任何下面的场景：

```
	*B, *C, *D, *F, *G or *H 在 LOCK M [1] 之前
	*A, *B or *C 在  UNLOCK M [1] 之后
	*F, *G or *H 在 LOCK M [2] 之前
	*A, *B, *C, *E, *F or *G 在 UNLOCK M [2] 之后
```

### 锁与I/O访问

在某些情况下（尤其是涉及NUMA），在两个不同CPU上的两个自旋锁区内的I / O访问，在PCI桥看来可能是交叉的，因为PCI桥不一定保证缓存一致性，此时内存屏障将失效。

例如：

```
	CPU 1				CPU 2
	===============================	===============================
	spin_lock(Q)
	writel(0, ADDR)
	writel(1, DATA);
	spin_unlock(Q);
					spin_lock(Q);
					writel(4, ADDR);
					writel(5, DATA);
					spin_unlock(Q);
```

PCI桥可能看到的顺序如下所示：

```
	STORE *ADDR = 0, STORE *ADDR = 4, STORE *DATA = 1, STORE *DATA = 5
```

这可能会导致硬件故障。

这里有必要在释放自旋锁之前插入mmiowb()函数，例如：

```
	CPU 1				CPU 2
	===============================	===============================
	spin_lock(Q)
	writel(0, ADDR)
	writel(1, DATA);
	mmiowb();
	spin_unlock(Q);
					spin_lock(Q);
					writel(4, ADDR);
					writel(5, DATA);
					mmiowb();
					spin_unlock(Q);
```

这将确保在CPU 1上的两次store比CPU 2上的两次store操作先被PCI感知。

此外，相同的设备上如果store指令后跟随一个load指令，可以省去mmiowb()函数，因为load强制在load执行前store指令必须完成：

```
	CPU 1				CPU 2
	===============================	===============================
	spin_lock(Q)
	writel(0, ADDR)
	a = readl(DATA);
	spin_unlock(Q);
					spin_lock(Q);
					writel(4, ADDR);
					b = readl(DATA);
					spin_unlock(Q);
```

更多信息参见：Documentation/DocBook/deviceiobook.tmpl

## 什么地方需要内存障碍？

在正常操作下，一个单线程代码片段中内存操作重排序一般不会产生问题，仍然可以正常工作，即使是在一个SMP内核系统中也是如此。但是，下面四种场景下，重新排序可能会引发问题：

- 多理器间的交互。
- 原子操作。
- 设备访问。
- 中断。

### 多理器间的交互

当系统具有一个以上的处理器，系统中多个CPU可能要访问同一数据集。这可能会导致同步问题，通常处理这种场景是使用锁。然而，锁是相当昂贵的，所以如果有其它的选择尽量不使用锁。在这种情况下，能影响到多个CPU的操作可能必须仔细排序，以防止出现故障。

例如，在R / W信号量慢路径的场景。这里有一个waiter进程在信号量上排队，并且它的堆栈上的一块空间链接到信号量上的等待进程列表：

```
	struct rw_semaphore {
		...
		spinlock_t lock;
		struct list_head waiters;
	};

	struct rwsem_waiter {
		struct list_head list;
		struct task_struct *task;
	};
```

要唤醒一个特定的waiter进程，up_read（）或up_write（）函数必须做以下动作：

1. 读取waiter记录的next指针，获取下一个waiter记录的地址；
2. 
3. 读取waiter的task结构的指针；
4. 
5. 清除task指针，通知waiter已经获取信号量；
6. 
7. 在task上调用wake_up_process()函数;
8. 
9. 释放waiter的task结构上的引用。
10. 

换句话说，它必须执行下面的事件：

```
	LOAD waiter->list.next;
	LOAD waiter->task;
	STORE waiter->task;
	CALL wakeup
	RELEASE task
```

如果这些步骤的顺序发生任何改变，那么就会出问题。

一旦进程将自己排队并且释放信号锁，waiter将不再获得锁，它只需要等待它的任务指针被清零，然后继续执行。由于记录是在waiter的堆栈上，这意味着如果在列表中的next指针被读取出之前，task指针被清零，另一个CPU可能会开始处理，up*（）函数在有机会读取next指针之前waiter的堆栈就被修改。

考虑上述事件序列可能发生什么：

```
	CPU 1				CPU 2
	===============================	===============================
					down_xxx()
					Queue waiter
					Sleep
	up_yyy()
	LOAD waiter->task;
	STORE waiter->task;
					Woken up by other event
	<preempt>
					Resume processing
					down_xxx() returns
					call foo()
					foo() clobbers *waiter
	</preempt>
	LOAD waiter->list.next;
	--- OOPS ---
```

虽然这里可以使用信号锁来处理，但在唤醒后的down_xxx()函数不必要的再次获得自旋锁。

这个问题可以通过插入一个通用的SMP内存屏障来处理：

```
	LOAD waiter->list.next;
	LOAD waiter->task;
	smp_mb();
	STORE waiter->task;
	CALL wakeup
	RELEASE task
```

在这种情况下，即使是在其它的CPU上，屏障确保所有在屏障之前的内存操作一定先于屏障之后的内存操作执行。但是它不能确保所有在屏障之前的内存操作一定先于屏障指令身执行完成时执行；

在一个UP系统中， 这种场景不会产生问题 ， smp_mb()仅仅是一个编译屏障，可以确保编译器以正确的顺序发出指令，而不会实际干预到CPU。因为只有一个CPU，CPU的依赖顺序逻辑会管理好一切。

### 原子操作

虽然它们在技术上考虑了处理器间的交互，但是特别注意，有一些原子操作暗含了完整的内存屏障，另外一些却没有包含，但是它们作为一个整体在内核中应用广泛。

任一原子操作，修改了内存中某一状态并返回有关状态（新的或旧的）的信息，这意味着在实际操作（明确的lock操作除外）的两侧暗含了一个SMP条件通用内存屏障（smp_mb()），包括；

```
	xchg();
	cmpxchg();
	atomic_cmpxchg();
	atomic_inc_return();
	atomic_dec_return();
	atomic_add_return();
	atomic_sub_return();
	atomic_inc_and_test();
	atomic_dec_and_test();
	atomic_sub_and_test();
	atomic_add_negative();
	atomic_add_unless();	/* when succeeds (returns 1) */
	test_and_set_bit();
	test_and_clear_bit();
	test_and_change_bit();
```

它们都是用于实现诸如LOCK和UNLOCK的操作，以及判断引用计数器决定对象销毁，同样，隐式的内存屏障效果是必要的。

下面的操作存在潜在的问题，因为它们并没有包含内存障碍，但可能被用于执行诸如解锁的操作：

```
	atomic_set();
	set_bit();
	clear_bit();
	change_bit();
```

如果有必要，这些应使用恰当的显式内存屏障（例如：smp_mb__before_clear_bit()）。

下面这些也没有包含内存屏障，因此在某些场景下可能需要明确的内存屏障（例如：smp_mb__before_atomic_dec()）：

```
	atomic_add();
	atomic_sub();
	atomic_inc();
	atomic_dec();
```

如果将它们用于统计，那么可能并不需要内存屏障，除非统计数据之间有耦合。

如果将它们用于对象的引用计数器来控制生命周期，也许也不需要内存屏障，因为可能引用计数会在锁区域内修改，或调用方已经考虑了锁，因此内存屏障不是必须的。

如果将它们用于构建一个锁的描述，那么确实可能需要内存屏障，因为锁原语通常以特定的顺序来处理事情；

基本上，每一个使用场景都必须仔细考虑是否需要内存屏障。

以下操作是特殊的锁原语：

```
	test_and_set_bit_lock();
	clear_bit_unlock();
	__clear_bit_unlock();
```

这些实现了诸如LOCK和UNLOCK的操作。在实现锁原语时应当优先考虑使用它们，因为它们的实现可以在很多架构中进行优化。

**[!]**注意：对于这些场景，也有特定的内存屏障原语可用，因为在某些CPU上原子指令暗含着完整的内存屏障，再使用内存屏障显得多余，在这种情况下，特殊屏障原语将是个空操作。

更多信息见 Documentation/atomic_ops.txt。

### 设备访问

许多设备都可以映射到内存上，因此对CPU来说它们只是一组内存单元。为了控制这样的设备，驱动程序通常必须确保对应的内存访问顺序的正确性。

然而，聪明的CPU或者聪明的编译器可能为引发潜在的问题，如果CPU或者编译器认为重排、合并、联合访问更加高效，驱动程序精心编排的指令顺序可能在实际访问设备是并不是按照这个顺序访问的 —— 这会导致设备故障。

在Linux内核中，I / O通常需要适当的访问函数 —— 如inb() 或者 writel() —— 它们知道如何保持适当的顺序。虽然这在大多数情况下不需要明确的使用内存屏障，但是下面两个场景可能需要：

1. 在某些系统中，I / O存储操作并不是在所有CPU上都是严格有序的，所以，对所有的通用驱动，锁是必须的，且必须在解锁临界区之前执行mmiowb().
2. 如果访问函数是用来访问一个松散访问属性的I / O存储窗口，那么需要强制内存屏障来保证顺序。

更多信息参见 Documentation/DocBook/deviceiobook.tmpl。

### 中断

驱动可能会被自己的中断服务例程中断，因此，驱动程序两个部分可能会互相干扰，尝试控制或访问该设备。

通过禁用本地中断（一种锁的形式）可以缓和这种情况，这样，驱动程序中关键的操作都包含在中断禁止的区间中 。有时驱动的中断例程被执行，但是驱动程序的核心不是运行在相同的CPU上，并且直到当前的中断被处理结束之前不允许其它中断，因此，在中断处理器不需要再次加锁。

但是，考虑一个驱动使用地址寄存器和数据寄存器跟以太网卡交互，如果该驱动的核心在中断禁用下与网卡通信，然后驱动程序的中断处理程序被调用：

```
	LOCAL IRQ DISABLE
	writew(ADDR, 3);
	writew(DATA, y);
	LOCAL IRQ ENABLE
	<interrupt>
	writew(ADDR, 4);
	q = readw(DATA);
	</interrupt>
```

如果排序规则十分宽松，数据寄存器的存储可能发生在第二次地址寄存器之后：

```
	STORE *ADDR = 3, STORE *ADDR = 4, STORE *DATA = y, q = LOAD *DATA
```

如果是宽松的排序规则，它必须假设中断禁止部分的内存访问可能向外泄漏，可能会和中断部分交叉访问 – 反之亦然 – 除非使用了隐式或显式的屏障。

通常情况下，这不会产生问题，因为这种区域中的I / O访问将在严格有序的IO寄存器上包含同步load操作，形成隐式内存屏障。如果这还不够，可能需要显式地使用一个mmiowb()。

类似的情况可能发生在一个中断例程和运行在不同CPU上进行通信的两个例程的时候。这样的情况下，应该使用中断禁用锁来保证顺序。

## 内核I/O屏障效应

访问I/O内存时，驱动应使用适当的存取函数：

- inX(), outX():
- 它们都旨在跟I / O空间打交道，而不是内存空间，但这主要是一个特定于CPU的概念。在 i386和x86_64处理器中确实有特殊的I / O空间访问周期和指令，但许多CPU没有这样的概念。

- 包括PCI总线也定义了I / O空间，比如在i386和x86_64的CPU 上很容易将它映射到CPU的I / O空间上。然而，对于那些不支持IO空间的CPU，它也可能作为虚拟的IO空间被映射CPU的的内存空间。

- 访问这个空间可能是完全同步的（在i386），但桥设备（如PCI主桥）可能不完全履行这一点。

- 可以保证它们彼此之间的全序关系。

- 对于其他类型的内存和I / O操作，不保证它们的全序关系。

- readX(), writeX():
- 无论是保证完全有序还是不合并访问取决于他们访问时定义的访问窗口属性，例如，最新的i386架构的机器通过MTRR寄存器控制。

- 通常情况下，只要不是访问预取设备，就保证它们的全序关系且不合并。

- 然而，对于中间链接硬件（如PCI桥）可能会倾向延迟处理，当刷新一个store时，首选从同一位置load，但是对同一个设备或配置空间load时，对与PCI来说一次就足够了。

- **[\*]**注意：试图从刚写过的相同的位置load数据可能导致故障 – 考虑16550 RX / TX串行寄存器的例子。

- 对于可预取的I / O内存，可能需要一个mmiowb()屏障保证顺序；

- 请参阅PCI规范获得PCI事务间交互的更多信息；

- readX_relaxed()
- 这些类似readX()，但在任何时候都不保证顺序。因为没有I / O读屏障。

- ioreadX(), iowriteX()
- 它们通过选择inX()/outX() or readX()/writeX()来实际操作。

## 假想的最小执行顺序模型

首先假定概念上CPU是弱有序的，但它能维护程序因果关系。某些CPU（如i386或x86_64）比其它类型的CPU（如PowerPC的或FRV）受到更多的约束，所以，在考虑与具体体系结构无关的代码时，必须假设处在最宽松的场景（即DEC ALPHA）。

这意味着必须考虑CPU将以任何它喜欢的顺序执行它的指令流 —— 甚至是并行的 —— 如果流中的某个指令依赖前面较早的指令，则该较早的指令必须在后者执行之前完全结束[*]，换句话说：保持因果关系。

**[\*]**有些指令会产生多个结果 —— 如改变条件码，改变寄存器或修改内存 —— 不同的指令可能依赖于不同的结果。

CPU也可能会放弃那些最终不产生效果的指令。例如，如果两个相邻的指令加载一个直接值到同一个寄存器中，第一个可能被丢弃。

同样地，必须假定编译器可能以任何它认为合适的方式会重新排列指令流，但同样维护程序因果关系。

## CPU缓存的影响

缓存的内存操作被系统交叉感知的方式，在一定程度上，受到CPU和内存之间的缓存、以及保持系统一致状态的内存一致性系统的影响。

若CPU和系统其它部分的交互通过cache进行，内存系统就必须包括CPU缓存，以及CPU及其缓存之间的内存屏障（内存屏障逻辑上如下图中的虚线）：

```
	    <--- CPU --->         :       <----------- Memory ----------->
	                          :
	+--------+    +--------+  :   +--------+    +-----------+
	|        |    |        |  :   |        |    |           |    +--------+
	|  CPU   |    | Memory |  :   | CPU    |    |           |    |	      |
	|  Core  |--->| Access |----->| Cache  |<-->|           |    |	      |
	|        |    | Queue  |  :   |        |    |           |--->| Memory |
	|        |    |        |  :   |        |    |           |    |	      |
	+--------+    +--------+  :   +--------+    |           |    | 	      |
	                          :                 | Cache     |    +--------+
	                          :                 | Coherency |
	                          :                 | Mechanism |    +--------+
	+--------+    +--------+  :   +--------+    |           |    |	      |
	|        |    |        |  :   |        |    |           |    |        |
	|  CPU   |    | Memory |  :   | CPU    |    |           |--->| Device |
	|  Core  |--->| Access |----->| Cache  |<-->|           |    | 	      |
	|        |    | Queue  |  :   |        |    |           |    | 	      |
	|        |    |        |  :   |        |    |           |    +--------+
	+--------+    +--------+  :   +--------+    +-----------+
	                          :
	                          :
```

虽然一些特定的load或store实际上可能不出现在发出这些指令的CPU之外，因为在该CPU自己的缓存内已经满足，但是，如果其它CPU关心这些数据，那么还是会产生完整的内存访问，因为高速缓存一致性机制将迁移缓存行到需要访问的CPU，并传播冲突。

只要能维持程序的因果关系，CPU核心可以以任何顺序执行指令。有些指令生成load和store操作，并将他们放入内存请求队列等待执行。CPU内核可以以任意顺序放入到队列中，并继续执行，直到它被强制等待某一个指令完成。

内存屏障关心的是控制访问穿越CPU到内存一边的顺序，以及系统其他组建感知到的顺序。

**[!]**对于一个给定的CPU，并不需要内存屏障，因为CPU总是可以看到自己的load和store指令，好像发生的顺序就是程序顺序一样。

**[!]**MMIO或其它设备访问可能绕过缓存系统。这取决于访问设备时内存窗口属性，或者某些CPU支持的特殊指令。

### 缓存一致性

但是事情并不像上面说的那么简单，虽然缓存被期望是一致的，但是没有保证这种一致性的顺序。这意味着在一个CPU上所做的更改最终可以被所有CPU可见，但是并不保证其它的CPU能以相同的顺序感知变化。

考虑一个系统，有一对CPU（1＆2），每一个CPU有一组并行的数据缓存（CPU 1有A / B，CPU 2有C / D）：

```
	            :
	            :                          +--------+
	            :      +---------+         |        |
	+--------+  : +--->| Cache A |<------->|        |
	|        |  : |    +---------+         |        |
	|  CPU 1 |<---+                        |        |
	|        |  : |    +---------+         |        |
	+--------+  : +--->| Cache B |<------->|        |
	            :      +---------+         |        |
	            :                          | Memory |
	            :      +---------+         | System |
	+--------+  : +--->| Cache C |<------->|        |
	|        |  : |    +---------+         |        |
	|  CPU 2 |<---+                        |        |
	|        |  : |    +---------+         |        |
	+--------+  : +--->| Cache D |<------->|        |
	            :      +---------+         |        |
	            :                          +--------+
	            :
```

假设该系统具有以下属性：

- 奇数编号的缓存行在缓存A或者C中，或它可能仍然驻留在内存中;
- 偶数编号的缓存行在缓存B或者D中，或它可能仍然驻留在内存中;
- 当CPU核心正在访问一个cache，其它的cache可能利用总线来访问该系统的其余组件 —— 可能是取代脏缓存行或预加载;
- 每个cache有一个操作队列，用来维持cache与系统其余部分的一致性;
- 正常load已经存在于缓存行中的数据时，一致性队列不会刷新，即使队列中的内容可能会影响这些load。

接下来，试想一下，第一个CPU上有两个写操作，并且它们之间有一个write屏障，来保证它们到达该CPU缓存的顺序：

```
	CPU 1		CPU 2		COMMENT
	===============	===============	=======================================
					u == 0, v == 1 and p == &u, q == &u
	v = 2;
	smp_wmb();			Make sure change to v is visible before
					 change to p
	<A:modify v=2>			v is now in cache A exclusively
	p = &v;
	<B:modify p=&v>			p is now in cache B exclusively
```

write内存屏障强制系统中其它CPU能以正确的顺序感知本地CPU缓存的更改。现在假设第二个CPU要读取这些值：

```
	CPU 1		CPU 2		COMMENT
	===============	===============	=======================================
	...
			q = p;
			x = *q;
```

上述一对读操作可能不会按预期的顺序执行，持有P的缓存行可能被第二个CPU的某一个缓存更新，而持有V的缓存行在第一个CPU的另外一个缓存中因为其它事情被延迟更新了；

```
	CPU 1		CPU 2		COMMENT
	===============	===============	=======================================
					u == 0, v == 1 and p == &u, q == &u
	v = 2;
	smp_wmb();
	<A:modify v=2>	<C:busy>
			<C:queue v=2>
	p = &v;		q = p;
			<D:request p>
	<B:modify p=&v>	<D:commit p=&v>
		  	<D:read p>
			x = *q;
			<C:read *q>	Reads from v before v updated in cache
			<C:unbusy>
			<C:commit v=2>
```

基本上，虽然两个缓存行CPU 2在最终都会得到更新，但是在不进行干预的情况下不能保证更新的顺序与在CPU 1在提交的顺序一致。

所以我们需要在load之间插入一个数据依赖屏障或read屏障。这将迫使缓存在处理其他任务之前强制提交一致性队列；

```
	CPU 1		CPU 2		COMMENT
	===============	===============	=======================================
					u == 0, v == 1 and p == &u, q == &u
	v = 2;
	smp_wmb();
	<A:modify v=2>	<C:busy>
			<C:queue v=2>
	p = &v;		q = p;
			<D:request p>
	<B:modify p=&v>	<D:commit p=&v>
		  	<D:read p>
			smp_read_barrier_depends()
			<C:unbusy>
			<C:commit v=2>
			x = *q;
			<C:read *q>	Reads from v after v updated in cache
```

DEC Alpha处理器上可能会遇到这类问题，因为他们有一个分列缓存，通过更好地利用数据总线以提高性能。虽然大部分的CPU在读操作需要读取内存的时候会使用数据依赖屏障，但并不都这样，所以不能依赖这些。

其它CPU也可能页有分列缓存，但是对于正常的内存访问，它们会协调各个缓存列。在缺乏内存屏障的时候，Alpha 的语义会移除这种协作。

### 缓存一致性与DMA

对于DMA的设备，并不是所有的系统都维护缓存一致性。这时访问DMA的设备可能从RAM中得到脏数据，因为脏的缓存行可能驻留在各个CPU的缓存中，并且可能还没有被写入到RAM。为了处理这种情况，内核必须刷新每个CPU缓存上的重叠位（或是也可以直接废弃它们）。

此外，当设备以及加载自己的数据之后，可能被来自CPU缓存的脏缓存行写回RAM所覆盖，或者当前CPU缓存的缓存行可能直接忽略RAM已被更新，直到缓存行从CPU的缓存被丢弃和重载。为了处理这个问题，内核必须废弃每个CPU缓存的重叠位。

更多信息参见Documentation/cachetlb.txt。

### 缓存一致性与MMIO

内存映射I / O通常作为CPU内存空间窗口中的一部分地址，它们与直接访问RAM的的窗口有不同的属性。

这些属性通常是，访问绕过缓存直接进入到设备总线。这意味着MMIO的访问可能先于早些时候发出的访问被缓存的内存的请求到达。在这样的情况下，一个内存屏障还不够，如果缓存的内存写和MMIO访问存在依赖，cache必须刷新。

## CPU能做的事情

程序员可能想当然的认为CPU会完全按照指定的顺序执行内存操作，如果确实如此的话，假设CPU执行下面这段代码：

```
	a = *A;
	*B = b;
	c = *C;
	d = *D;
	*E = e;
```

他们会期望CPU执行下一个指令之前上一个一定执行完成，然后在系统中可以观察到一个明确的执行顺序；

```
	LOAD *A, STORE *B, LOAD *C, LOAD *D, STORE *E.
```

当然，现实中是非常混乱的。对许多CPU和编译器来说，上述假设都不成立，因为：

- load操作可能更需要立即完成的，以保持执行进度，而推迟store往往是没有问题的;
- load操作可能预取，当结果证明是不需要的，可以丢弃;
- load操作可能预取，导致取数的时间和预期的事件序列不符合;
- 内存访问的顺序可能被重排，以更好地利用CPU总线和缓存;
- 与内存和IO设备交互时，如果能批访问相邻的位置，load和store可能会合并，以提高性能，从而减少了事务设置的开销（内存和PCI设备都能够做到这一点）;
- CPU的数据缓存也可能会影响顺序，虽然缓存一致性机制可以缓解 – 一旦store操作命中缓存 —— 并不能保证一致性能正确的传播到其它CPU。

所以对另一个CPU，上面的代码实际观测的结果可能是：

```
	LOAD *A, ..., LOAD {*C,*D}, STORE *E, STORE *B

	(Where "LOAD {*C,*D}" is a combined load)
```

但是，CPU保证自身的一致性：不需要内存屏障，也可以保证自己以正确的顺序访问内存，如下面的代码：

```
	U = *A;
	*A = V;
	*A = W;
	X = *A;
	*A = Y;
	Z = *A;
```

假设不受到外部的影响，最终的结果可能为：

```
	U == the original value of *A
	X == W
	Z == Y
	*A == Y
```

上面的代码CPU可能产生的全部的内存访问顺序如下：

```
	U=LOAD *A, STORE *A=V, STORE *A=W, X=LOAD *A, STORE *A=Y, Z=LOAD *A
```

对于这个顺序，如果没有干预，在保持一致的前提下,一些操作也可能被合并，丢弃。

在CPU感知这些操作之前，编译器也可能合并、丢弃、延迟加载这些元素。

例如：

```
	*A = V;
	*A = W;
```

可减少到：

```
	*A = W;
```

因为在没有write屏障的情况下，可以假定将V写入到*A的操作被丢弃了，同样：

```
	*A = Y;
	Z = *A;
```

若没有内存屏障，可被简化为：

```
	*A = Y;
	Z = Y;
```

在CPU之外根本看不到load操作。

### ALPHA处理器

DEC Alpha CPU是最松散的CPU之一。不仅如此，一些版本的Alpha CPU有一个分列的数据缓存，允许它们在不同的时间更新语义相关的缓存。在同步多个缓存，保证一致性的时候，数据依赖屏障是必须的，使CPU可以正确的顺序处理指针的变化和数据的获得。

Alpha定义了Linux内核的内存屏障模型。

参见上面的“缓存一致性”章节。

## 使用示例

### 循环缓冲区

内存屏障可以用来实现循环缓冲，不需要用锁来使得生产者与消费者串行。

更多详情参考“Documentation/circular-buffers.txt”

## 参考

- Alpha AXP Architecture Reference Manual, Second Edition (Sites & Witek,
  Digital Press)
- Chapter 5.2: Physical Address Space Characteristics

- Chapter 5.4: Caches and Write Buffers

- Chapter 5.5: Data Sharing

- Chapter 5.6: Read/Write Ordering

- AMD64 Architecture Programmer’s Manual Volume 2: System Programming
- Chapter 7.1: Memory-Access Ordering

- Chapter 7.4: Buffering and Combining Memory Writes

- IA-32 Intel Architecture Software Developer’s Manual, Volume 3:
  System Programming Guide
- Chapter 7.1: Locked Atomic Operations

- Chapter 7.2: Memory Ordering

- Chapter 7.4: Serializing Instructions

- The SPARC Architecture Manual, Version 9
- Chapter 8: Memory Models

- Appendix D: Formal Specification of the Memory Models

- Appendix J: Programming with the Memory Models

- UltraSPARC Programmer Reference Manual
- Chapter 5: Memory Accesses and Cacheability

- Chapter 15: Sparc-V9 Memory Models

- UltraSPARC III Cu User’s Manual
- Chapter 9: Memory Models

- UltraSPARC IIIi Processor User’s Manual
- Chapter 8: Memory Models

- UltraSPARC Architecture 2005
- Chapter 9: Memory

- Appendix D: Formal Specifications of the Memory Models

- UltraSPARC T1 Supplement to the UltraSPARC Architecture 2005 Chapter 8: Memory Models

- Appendix F: Caches and Cache Coherency

- Solaris Internals, Core Kernel Architecture, p63-68:
- Chapter 3.3: Hardware Considerations for Locks and
  Synchronization

- Unix Systems for Modern Architectures, Symmetric Multiprocessing and Caching
  for Kernel Programmers:
- Chapter 13: Other Memory Models

- Intel Itanium Architecture Software Developer’s Manual: Volume 1:
- Section 2.6: Speculation

- Section 4.4: Memory Access