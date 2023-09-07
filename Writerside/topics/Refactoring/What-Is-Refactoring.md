# 重构是什么

## 整洁代码(Clean Code)
重构的主要目的是消除技术债务。它将一团糟的代码转化为简洁的代码和简单的设计。

不错！但什么是简洁代码呢？以下是它的一些特点：

#### 对于其他程序员来说，简洁代码是显而易见的。
我说的不是超级复杂的算法。糟糕的变量命名、臃肿的类和方法、神奇的数字--你说得出来--所有这些都会让代码变得马虎且难以掌握。

#### 整洁的代码不包含重复。
每次对重复的代码进行修改时，你都必须记住对每个实例进行相同的修改。这会增加认知负担，减慢进度。
 
#### 简洁的代码包含最少的类和其他活动部件。
代码越少，需要记在脑子里的东西就越少。代码越少，维护越少。代码少，错误就少。代码是责任，要简短。

#### 干净的代码能通过所有测试。
当只有 95% 的测试通过时，你就知道你的代码不干净。当你的测试覆盖率为 0% 时，你就知道你完蛋了。

#### 整洁的代码更容易维护，维护成本更低


## Technical debt
Everyone does their best to write excellent code from scratch. There probably isn’t a programmer out there who intentionally writes unclean code to the detriment of the project. But at what point does clean code become unclean?

The metaphor of “technical debt” in regards to unclean code was originally suggested by Ward Cunningham.

If you get a loan from a bank, this allows you to make purchases faster. You pay extra for expediting the process - you don’t just pay off the principal, but also the additional interest on the loan. Needless to say, you can even rack up so much interest that the amount of interest exceeds your total income, making full repayment impossible.

The same thing can happen with code. You can temporarily speed up without writing tests for new features, but this will gradually slow your progress every day until you eventually pay off the debt by writing tests.

Causes of technical debt
### Business pressure
Sometimes business circumstances might force you to roll out features before they’re completely finished. In this case, patches and kludges will appear in the code to hide the unfinished parts of the project.

### Lack of understanding of the consequences of technical debt
Sometimes your employer might not understand that technical debt has “interest” insofar as it slows down the pace of development as debt accumulates. This can make it too difficult to dedicate the team’s time to refactoring because management doesn’t see the value of it.

### Failing to combat the strict coherence of components
This is when the project resembles a monolith rather than the product of individual modules. In this case, any changes to one part of the project will affect others. Team development is made more difficult because it’s difficult to isolate the work of individual members.

### Lack of tests
The lack of immediate feedback encourages quick, but risky workarounds or kludges. In worst cases, these changes are implemented and deployed right into the production without any prior testing. The consequences can be catastrophic. For example, an innocent-looking hotfix might send a weird test email to thousands of customers or even worse, flush or corrupt an entire database.

### Lack of documentation
This slows down the introduction of new people to the project and can grind development to a halt if key people leave the project.

### Lack of interaction between team members
If the knowledge base isn’t distributed throughout the company, people will end up working with an outdated understanding of processes and information about the project. This situation can be exacerbated when junior developers are incorrectly trained by their mentors.

### Long-term simultaneous development in several branches
This can lead to the accumulation of technical debt, which is then increased when changes are merged. The more changes made in isolation, the greater the total technical debt.

### Delayed refactoring
The project’s requirements are constantly changing and at some point it may become obvious that parts of the code are obsolete, have become cumbersome, and must be redesigned to meet new requirements.

On the other hand, the project’s programmers are writing new code every day that works with the obsolete parts. Therefore, the longer refactoring is delayed, the more dependent code will have to be reworked in the future.

### Lack of compliance monitoring
This happens when everyone working on the project writes code as they see fit (i.e. the same way they wrote the last project).

### Incompetence
This is when the developer just doesn’t know how to write decent code.


## When to refactor



#### Rule of Three
![img](https://refactoring.guru/images/content-public/r1.svg)

1. When you’re doing something for the first time, just get it done.
2. When you’re doing something similar for the second time, cringe at having to repeat but do the same thing anyway.
3. When you’re doing something for the third time, start refactoring.


#### When adding a feature

![img](https://refactoring.guru/images/content-public/r2.svg)

- Refactoring helps you understand other people’s code. If you have to deal with someone else’s dirty code, try to refactor it first. Clean code is much easier to grasp. You will improve it not only for yourself but also for those who use it after you.
- Refactoring makes it easier to add new features. It’s much easier to make changes in clean code.



#### When fixing a bug
![img](https://refactoring.guru/images/content-public/r3.svg)

Bugs in code behave just like those in real life: they live in the darkest, dirtiest places in the code. Clean your code and the errors will practically discover themselves.

Managers appreciate proactive refactoring as it eliminates the need for special refactoring tasks later. Happy bosses make happy programmers!


#### During a code review
![img](https://refactoring.guru/images/content-public/r4.svg)

The code review may be the last chance to tidy up the code before it becomes available to the public.

It’s best to perform such reviews in a pair with an author. This way you could fix simple problems quickly and gauge the time for fixing the more difficult ones.

## How to refactor

Refactoring should be done as a series of small changes, each of which makes the existing code slightly better while still leaving the program in working order.

###  Checklist of refactoring done *right way*

####  The code should become cleaner.

If the code remains just as unclean after refactoring... well, I’m sorry, but you’ve just wasted an hour of your life. Try to figure out why this happened.

It frequently happens when you move away from refactoring with small changes and mix a whole bunch of refactorings into one big change. So it’s very easy to lose your mind, especially if you have a time limit.

But it can also happen when working with extremely sloppy code. Whatever you improve, the code as a whole remains a disaster.

In this case, it’s worthwhile to think about completely rewriting parts of the code. But before that, you should have written tests and set aside a good chunk of time. Otherwise, you’ll end up with the kinds of results we talked about in the first paragraph.

####  New functionality shouldn’t be created during refactoring.

Don’t mix refactoring and direct development of new features. Try to separate these processes at least within the confines of individual commits.

####  All existing tests must pass after refactoring.

There are two cases when tests can break down after refactoring:

- **You made an error during refactoring.** This one is a no-brainer: go ahead and fix the error.

- **Your tests were too low-level.** For example, you were testing private methods of classes.

In this case, the tests are to blame. You can either refactor the tests themselves or write an entirely new set of higher-level tests. A great way to avoid this kind of a situation is to write [BDD-style](https://refactoring.guru/refactoring/bdd) tests.