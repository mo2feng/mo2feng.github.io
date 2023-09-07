# AQS源码解析

AbstractQueuedSynchronizer(AQS)是JDK中实现并发编程的核心，平时我们工作中经常用到的ReentrantLock，CountDownLatch等都是基于它来实现的。

  

AQS类中维护了一个双向链表(FIFO队列)， 如下图所示：

  ![AQS双向链表](827234-20170515212822119-1328747179.png)

  队列中的每个元素都用一个Node表示，我们可以看到，Node类中有几个静态常量表示的状态：


```java
static final class Node {
        /** Marker to indicate a node is waiting in shared mode */
        static final Node SHARED = new Node();
        /** Marker to indicate a node is waiting in exclusive mode */
        static final Node EXCLUSIVE = null;

        /** waitStatus value to indicate thread has cancelled */
        static final int CANCELLED =  1;
        /** waitStatus value to indicate successor's thread needs unparking */
        static final int SIGNAL    = -1;
        /** waitStatus value to indicate thread is waiting on condition */
        static final int CONDITION = -2;
        static final int PROPAGATE = -3;
        volatile int waitStatus;
        volatile Node prev;

        volatile Node next;
        volatile Thread thread;
      
        Node nextWaiter;

        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {
        }

        Node(Thread thread, Node mode) { 
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) {
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }
```

 此外，AQS中通过一个state的volatile变量表示同步状态。

 那么AQS是如何通过队列实现锁操作的呢？

## 获取锁操作

  下面的是AQS中执行获取锁的代码:



```java
public final void acquire(int arg) {        
//通过tryAcquire获取锁，如果成功获取到锁直接终止(selfInterrupt),否则将当前线程插入队列        
//这里的Node.EXCLUSIVE表示创建一个独占模式的节点      
        if (!tryAcquire(arg) &&acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
```

 然而实际上，AQS中并没有实现上面的tryAcquire(arg)方法，具体获取锁的操作需要由其子类比如ReentrantLock中的Sync实现：

 

```java
protected final boolean tryAcquire(int acquires) {
			//取到当前线程
            final Thread current = Thread.currentThread();
            //获取到state值(前文提到)
            int c = getState();
            //state为0标识当前没有线程占有锁  
            //如果队列中前面没有元素(因为是公平锁的原因，非公平锁中不进行判断，如果state为0直接获取到锁)，CAS修改当前值
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    //标识当前线程成功获取锁
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }            
            //state不为0，且占有锁的线程是当前线程(这里涉及到一个可重入锁的概念)
            else if (current == getExclusiveOwnerThread()) {
                //增加重入次数
                int nextc = c + acquires;
                //如果次数值溢出，抛出异常
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }            
            //如果锁已经被其它线程占用,获取锁失败
            return false;
        }
```

上面的代码注释中提到了可重入锁的概念，可重入锁又叫递归锁，简单来讲就是已经获取到锁的线程还可以再次获取到同一个锁，我们通常使用的syschronized操作,ReentrantLock都属于可重入锁。自旋锁则不属于可重入锁。

 下面我们再看一下如果tryAcquire失败，AQS是如何处理的：

```java
private Node addWaiter(Node mode) {        //创建一个队列的Node
        Node node = new Node(Thread.currentThread(), mode);
        //获取当前队列尾部
        Node pred = tail;
        if (pred != null) {            //CAS操作尝试插入Node到等待队列，这里只尝试一次
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }        //如果添加失败，enq这里会做自旋操作，知道插入成功。
        enq(node);
        return node;
    } 
```

```java
//自旋操作添加元素到队列尾部private Node enq(final Node node) {
        for (;;) {            //获取尾节点
            Node t = tail;            //如果尾节点为空，说明当前队列是空，需要初始化队列
            if (t == null) {                //初始化当前队列
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {                //通过CAS操作插入Node，设置Node为队列的尾节点，并返回Node
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
```



```java
/** 如果插入的节点前面是head,尝试获取锁，*/ 
final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;            
            //自旋操作
            for (;;) {                
                //获取当前插入节点的前置节点
                final Node p = node.predecessor();                
                //前置节点是head，尝试获取锁
                if (p == head && tryAcquire(arg)) {                    
                    //设置head为当前节点，表示获取锁成功
                    setHead(node);
                    p.next = null; 
                    // help GC
                    failed = false;
                    return interrupted;
                }                
                //是否挂起当前线程，如果是，则挂起线程
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
```

 上面的代码有些复杂，这里解释一下，之前的addWaiter代码已经将node加入了等待队列，所以这里需要让节点队列中挂起，等待唤醒。队列的head节点代表的是当前占有锁的节点，首先判断插入的node的前置节点是否是head，如果是，尝试获取锁(tryAcquire),如果获取成功则将head设置为当前节点；如果获取失败需要判断是否挂起当前线程。

```java
 /*** 判断是否可以挂起当前线程*/
 private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {        
     //ws为node前置节点的状态
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
　　　　　　　//如果前置节点状态为SIGNAL，当前节点可以挂起
            return true;
        if (ws > 0) {
            //通过循环跳过所有的CANCELLED节点，找到一个正常的节点，将当前节点排在它后面           
            //GC会将这些CANCELLED节点回收
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {            
            //将前置节点的状态修改为SIGNAL
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
```



 

```java
//通过LockSupport挂起线程，等待唤醒
private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
}
```

 

 

## 释放锁操作

  有了获取锁的基础，再来看释放锁的源码就比较容易了，下面的代码执行的是AQS中释放锁的操作：

```java
//释放锁的操作
public final boolean release(int arg)         
        //尝试释放锁，这里tryRelease同样由子类实现，如果失败直接返回false
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
```



 下面的代码是尝试释放锁的操作：

```java
 protected final boolean tryRelease(int releases) {            
             //获取state值，释放一定值
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;            
            //如果差是0，表示锁已经完全释放
            if (c == 0) {
                free = true;
                //下面设置为null表示当前没有线程占用锁
                setExclusiveOwnerThread(null);
            }
            //如果c不是0表示锁还没有完全释放，修改state值
            setState(c);
            return free;
        }
```



 释放锁后，还需要唤醒队列中的一个后继节点：



```java
private void unparkSuccessor(Node node) {
        //将当前节点的状态修改为0
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);
        //从队列里找出下一个需要唤醒的节点        
    //首先是直接后继
        Node s = node.next;        
    //如果直接后继为空或者它的waitStatus大于0(已经放弃获取锁了)，我们就遍历整个队列，        
    //获取第一个需要唤醒的节点
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)            
            //将节点唤醒
            LockSupport.unpark(s.thread);
    }
```



 