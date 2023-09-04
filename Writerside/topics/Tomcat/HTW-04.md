# 第四章:Tomcat的默认连接器
##  概要
第3章的连接器运行良好，可以完善以获得更好的性能。但是，它只是作为一个教育工具，设计来介绍Tomcat4的默认连接器用的。理解第3章中的连接器是理解Tomcat4的默认连接器的关键所在。现在，在第4章中将通过剖析Tomcat4的默认连接器的代码，讨论需要什么来创建一个真实的Tomcat连接器。 

> **注意**
> 
> 本章中提及的“默认连接器”是指Tomcat4的默认连接器。即使默认的连接器已经被弃用，被更快的，代号为Coyote的连接器所代替，它仍然是一个很好的学习工具。
>
> {style="warning"}


Tomcat连接器是一个可以插入servlet容器的独立模块，已经存在相当多的连接器了，包括`Coyote`, `mod_jk`, `mod_jk2`和`mod_webapp`。

一个Tomcat连接器必须符合以下条件： 
1. 必须实现接口`org.apache.catalina.Connector`。 
2. 必须创建请求对象，该请求对象的类必须实现接口`org.apache.catalina.Request`。
3. 必须创建响应对象，该响应对象的类必须实现接口`org.apache.catalina.Response`。

Tomcat4的默认连接器类似于第3章的简单连接器。它等待前来的HTTP请求，创建`request`和`response`对象，然后把`request`和`response`对象传递给容器。连接器是通过调用接口`org.apache.catalina.Containe`r的`invoke`方法来传递request和response对象的。

invoke的方法签名如下所示：
```java
public void invoke( org.apache.catalina.Request request, org.apache.catalina.Response response);
```
在`invoke`方法里边，容器加载`servlet`，调用它的`service`方法，管理会话，记录出错日志等等。 

默认连接器同样使用了一些第3章中的连接器未使用的优化。首先就是提供一个各种各样对象的对象池用于避免昂贵对象的创建。接着，在很多地方使用字节数组来代替字符串。


本章中的应用程序是一个和默认连接器管理的简单容器。然而，本章的焦点不是简单容器而是默认连接器。我们将会在第5章中讨论容器。不管怎样，为了展示如何使用默认连接器，将会在接近本章末尾的“简单容器的应用程序”一节中讨论简单容器。

另一个需要注意的是默认连接器除了提供HTTP0.9和HTTP1.0的支持外，还实现了HTTP1.1的所有新特性。为了理解HTTP1.1中的新特性，你首先需要理解本章首节解释的这些新特性。

在这之后，我们将会讨论接口 `org.apache.catalina.Connector`和如何创建请求和响应对象。假如你理解第3章中连接器如何工作的话，那么在理解默认连接器的时候你应该不会遇到任何问题。

本章首先讨论HTTP1.1的三个新特性。理解它们是理解默认连接器内部工作机制的关键所在。然后，介绍所有连接器都会实现的接口`org.apache.catalina.Connector`。你会发现第3章中遇到的那些类，例如`HttpConnector`, `HttpProcessor`等等。不过，这个时候，它们比第3章那些类似的要高级些。

### HTTP 1.1新特性
本节解释了HTTP1.1的三个新特性。理解它们是理解默认连接器如何处理HTTP请求的关键。
####    持久连接
在HTTP1.1之前，无论什么时候浏览器连接到一个web服务器，当请求的资源被发送之后，连接就被服务器关闭了。然而，一个互联网网页包括其他资源， 例如图片文件，applet等等。因此，当一个页面被请求的时候,浏览器同样需要下载页面所引用到的资源。加入页面和它所引用到的全部资源使用不同连接来 下载的话，进程将会非常慢。那就是为什么HTTP1.1引入持久连接的原因了。使用持久连接的时候，当页面下载的时候，服务器并不直接关闭连接。相反，它 等待web客户端请求页面所引用的全部资源。这种情况下，页面和所引用的资源使用同一个连接来下载。考虑建立和解除HTTP连接的宝贵操作的话，这就为 web服务器，客户端和网络节省了许多工作和时间。 持久连接是HTTP1.1的默认连接方式。同样，为了明确这一点，浏览器可以发送一个值为`keep-alive`的请求头部`connection`: 
```http
connection: keep-alive
```

#### 块编码
建立持续连接的结果就是，使用同一个连接，服务器可以从不同的资源发送字节流，而客户端可以使用发送多个请求。结果就是，发送方必须为每个请求或响应发送 内容长度的头部，以便接收方知道如何解释这些字节。然而，大部分的情况是发送方并不知道将要发送多少个字节。例如，在开头一些字节已经准备好的时 候，servlet容器就可以开始发送响应了，而不会等到所有都准备好。这意味着，在content-length头部不能提前知道的情况下，必须有一种 方式来告诉接收方如何解释字节流。

即使不需要发送多个请求或者响应，服务器或者客户端也不需要知道将会发送多少数据。在HTTP1.0中，服务器可以仅仅省略`content-length` 头部，并保持写入连接。当写入完成的时候，它将简单的关闭连接。在这种情况下，客户端将会保持读取状态，直到获取到-1，表示已经到达文件的尾部。 HTTP1.1使用一个特别的头部`transfer-encoding`来表示有多少以块形式的字节流将会被发送。对每块来说，在数据之前，长度(十六进 制)后面接着CR/LF将被发送。整个事务通过一个零长度的块来标识。假设你想用2个块发送以下38个字节，第一个长度是29，第二个长度是9。 `I'm as helpless as a kitten up a tree.` 你将这样发送： 
```
1D\r\n 
I'm as helpless as a kitten u 
9\r\n 
p a tree. 
0\r\n
``` 
`1D`,是29的十六进制，指示第一块由29个字节组成。`0\r\n`标识这个事务的结束。

####  状态100(持续状态)的使用
在发送请求内容之前，HTTP 1.1客户端可以发送Expect: 100-continue头部到服务器，并等待服务器的确认。这个一般发生在当客户端需要发送一份长的请求内容而未能确保服务器愿意接受它的时候。如果你 发送一份长的请求内容仅仅发现服务器拒绝了它，那将是一种浪费来的。 当接受到Expect: 100-continue头部的时候，假如乐意或者可以处理请求的话，服务器响应100-continue头部，后边跟着两对CRLF字符。 HTTP/1.1 100 Continue 接着，服务器应该会继续读取输入流。
  Connector接口
  Tomcat连接器必须实现org.apache.catalina.Connector接口。在这个接口的众多方法中，最重要的是getContainer,setContainer, createRequest和createResponse。 setContainer是用来关联连接器和容器用的。getContainer返回关联的容器。createRequest为前来的HTTP请求构造一个请求对象，而createResponse创建一个响应对象。 类org.apache.catalina.connector.http.HttpConnector是Connector接口的一个实现，将会在下一 节“HttpConnector类”中讨论。现在，仔细看一下Figure 4.1中的默认连接器的UML类图。注意的是，为了保持图的简单化，Request和Response接口的实现被省略了。除了 SimpleContainer类，org.apache.catalina前缀也同样从类型名中被省略了。
  Figure 4.1: The default connector class diagram 因此，Connector需要被org.apache.catalina.Connector,util.StringManager org.apache.catalina.util.StringManager等等访问到。 一个Connector和Container是一对一的关系。箭头的方向显示出Connector知道Container但反过来就不成立了。同样需要注意的是，不像第3章的是，HttpConnector和HttpProcessor是一对多的关系。
  HttpConnector类
  由于在第3章中org.apache.catalina.connector.http.HttpConnector的简化版本已经被解释过了，所以你已 经知道这个类是怎样的了。它实现了org.apache.catalina.Connector (为了和Catalina协调), java.lang.Runnable (因此它的实例可以运行在自己的线程上)和org.apache.catalina.Lifecycle。接口Lifecycle用来维护每个已经实现它的Catalina组件的生命周期。 Lifecycle将在第6章中解释，现在你不需要担心它，只要明白通过实现Lifecycle,在你创建HttpConnector实例之后，你应该 调用它的initialize和start方法。这两个方法在组件的生命周期里必须只调用一次。我们将看看和第3章的HttpConnector类的那些 不同方面:HttpConnector如何创建一个服务器套接字，它如何维护一个HttpProcessor对象池，还有它如何处理HTTP请求。
  创建一个服务器套接字
  HttpConnector的initialize方法调用open这个私有方法，返回一个java.net.ServerSocket实例，并把它赋予 serverSocket。然而，不是调用java.net.ServerSocket的构造方法，open方法是从一个服务端套接字工厂中获得一个 ServerSocket实例。如果你想知道这工厂的详细信息，可以阅读包org.apache.catalina.net里边的接口 ServerSocketFactory和类DefaultServerSocketFactory。它们是很容易理解的。
  维护HttpProcessor实例
  在第3章中，HttpConnector实例一次仅仅拥有一个HttpProcessor实例，所以每次只能处理一个HTTP请求。在默认连接器 中，HttpConnector拥有一个HttpProcessor对象池，每个HttpProcessor实例拥有一个独立线程。因 此，HttpConnector可以同时处理多个HTTP请求。 HttpConnector维护一个HttpProcessor的实例池，从而避免每次创建HttpProcessor实例。这些HttpProcessor实例是存放在一个叫processors的java.io.Stack中： private Stack processors = new Stack(); 在HttpConnector中，创建的HttpProcessor实例数量是有两个变量决定的：minProcessors和 maxProcessors。默认情况下，minProcessors为5而maxProcessors为20，但是你可以通过 setMinProcessors和setMaxProcessors方法来改变他们的值。 protected int minProcessors = 5; private int maxProcessors = 20;
  开始的时候，HttpConnector对象创建minProcessors个HttpProcessor实例。如果一次有比HtppProcessor 实例更多的请求需要处理时，HttpConnector创建更多的HttpProcessor实例，直到实例数量达到maxProcessors个。在到 达这点之后，仍不够HttpProcessor实例的话，请
  来的请求将会给忽略掉。如果你想让HttpConnector继续创建 HttpProcessor实例的话，把maxProcessors设置为一个负数。还有就是变量curProcessors保存了 HttpProcessor实例的当前数量。 下面是类HttpConnector的start方法里边关于创建初始数量的HttpProcessor实例的代码： while (curProcessors < minProcessors) { if ((maxProcessors > 0) && (curProcessors >= maxProcessors)) break; HttpProcessor processor = newProcessor(); recycle(processor); } newProcessor方法构造一个HttpProcessor对象并增加curProcessors。recycle方法把HttpProcessor队会栈。 每个HttpProcessor实例负责解析HTTP请求行和头部，并填充请求对象。因此，每个实例关联着一个请求对象和响应对象。类 HttpProcessor的构造方法包括了类HttpConnector的createRequest和createResponse方法的调用。
  为HTTP请求服务
  就像第3章一样，HttpConnector类在它的run方法中有其主要的逻辑。run方法在一个服务端套接字等待HTTP请求的地方存在一个while循环，一直运行直至HttpConnector被关闭了。 while (!stopped) { Socket socket = null; try { socket = serverSocket.accept(); ... 对每个前来的HTTP请求，会通过调用私有方法createProcessor获得一个HttpProcessor实例。 HttpProcessor processor = createProcessor(); 然而，大部分时候createProcessor方法并不创建一个新的HttpProcessor对象。相反，它从池子中获取一个。如果在栈中已经存在一 个HttpProcessor实例，createProcessor将弹出一个。如果栈是空的并且没有超过HttpProcessor实例的最大数 量，createProcessor将会创建一个。然而，如果已经达到最大数量的话，createProcessor将会返回null。出现这样的情况的 话，套接字将会简单关闭并且前来的HTTP请求不会被处理。 if (processor == null) { try { log(sm.getString("httpConnector.noProcessor")); socket.close(); } ... continue; 如果createProcessor不是返回null，客户端套接字会传递给HttpProcessor类的assign方法： processor.assign(socket);
  现在就是HttpProcessor实例用于读取套接字的输入流和解析HTTP请求的工作了。重要的一点是，assign方法不会等到 HttpProcessor完成解析工作，而是必须马上返回，以便下一个
  前来的HTTP请求可以被处理。每个HttpProcessor实例有自己的线程 用于解析，所以这点不是很难做到。你将会在下节“HttpProcessor类”中看到是怎么做的。
  HttpProcessor类
  默认连接器中的HttpProcessor类是第3章中有着类似名字的类的全功能版本。你已经学习了它是如何工作的，在本章中，我们很有兴趣知道 HttpProcessor类怎样让assign方法异步化，这样HttpProcessor实例就可以同时间为很多HTTP请求服务了。 注意： HttpProcessor类的另一个重要方法是私有方法process，它是用于解析HTTP请求和调用容器的invoke方法的。我们将会在本章稍后部分的“处理请求”一节中看到它。 在第3章中，HttpConnector在它自身的线程中运行。但是，在处理下一个请求之前，它必须等待当前处理的HTTP请求结束。下面是第3章中HttpProcessor类的run方法的部分代码： public void run() { ... while (!stopped) { Socket socket = null; try { socket = serversocket.accept(); } catch (Exception e) { continue; } // Hand this socket off to an Httpprocessor HttpProcessor processor = new Httpprocessor(this); processor.process(socket); } } 第3章中的HttpProcessor类的process方法是同步的。因此，在接受另一个请求之前，它的run方法要等待process方法运行结束。 在默认连接器中，然而，HttpProcessor类实现了java.lang.Runnable并且每个HttpProcessor实例运行在称作处理 器线程(processor thread)的自身线程上。对HttpConnector创建的每个HttpProcessor实例，它的start方法将被调用，有效的启动了 HttpProcessor实例的处理线程。Listing 4.1展示了默认处理器中的HttpProcessor类的run方法： Listing 4.1: The HttpProcessor class's run method.
  public void run() { // Process requests until we receive a shutdown signal while (!stopped) { // Wait for the next socket to be assigned Socket socket = await(); if (socket == null) continue; // Process the request from this socket try { process(socket);
  } catch (Throwable t) { log("process.invoke", t); } // Finish up this request connector.recycle(this); } // Tell threadStop() we have shut ourselves down successfully synchronized (threadSync) { threadSync.notifyAll(); } } run方法中的while循环按照这样的循序进行：获取一个套接字，处理它，调用连接器的recycle方法把当前的HttpProcessor实例推回栈。这里是HttpConenctor类的recycle方法： void recycle(HttpProcessor processor) { processors.push(processor); } 需要注意的是，run中的while循环在await方法中结束。await方法持有处理线程的控制流，直到从HttpConnector中获取到一个新的套接字。用另外一种说法就是，直到HttpConnector调用HttpProcessor实例的assign方法。但是，await方法和assign方 法运行在不同的线程上。assign方法从HttpConnector的run方法中调用。我们就说这个线程是HttpConnector实例的run方法运行的处理线程。assign方法是如何通知已经被调用的await方法的？就是通过一个布尔变量available并且使用java.lang.Object的wait和notifyAll方法。 注意：wait方法让当前线程等待直到另一个线程为这个对象调用notify或者notifyAll方法为止。 这里是HttpProcessor类的assign和await方法：
  synchronized void assign(Socket socket) { // Wait for the processor to get the previous socket while (available) { try { wait(); } catch (InterruptedException e) { } } // Store the newly available Socket and notify our thread this.socket = socket; available = true; notifyAll(); ... } private synchronized Socket await() { // Wait for the Connector to provide a new Socket while (!available) {
  try { wait(); } catch (InterruptedException e) { } } // Notify the Connector that we have received this Socket Socket socket = this.socket; available = false; notifyAll(); if ((debug >= 1) && (socket != null)) log(" The incoming request has been awaited"); return (socket); } 两个方法的程序流向在Table 4.1中总结。 Table 4.1: Summary of the await and assign method The processor thread (the await method) The connector thread (the assign method) while (!available) { while (available) { wait(); wait(); } } Socket socket = this.socket; this.socket = socket; available = false; available = true; notifyAll(); notifyAll(); return socket; // to the run ... // method 刚开始的时候，当处理器线程刚启动的时候，available为false，线程在while循环里边等待(见Table 4.1的第1列)。它将等待另一个线程调用notify或notifyAll。这就是说，调用wait方法让处理器线程暂停，直到连接器线程调用HttpProcessor实例的notifyAll方法。 现在，看看第2列，当一个新的套接字被分配的时候，连接器线程调用HttpProcessor的assign方法。available的值是false，所以while循环给跳过，并且套接字给赋值给HttpProcessor实例的socket变量： this.socket = socket; 连接器线程把available设置为true并调用notifyAll。这就唤醒了处理器线程，因为available为true，所以程序控制跳出while循环：把实例的socket赋值给一个本地变量，并把available设置为false，调用notifyAll，返回最后需要进行处理的socket。 为什么await需要使用一个本地变量(socket)而不是返回实例的socket变量呢？因为这样一来，在当前socket被完全处理之前，实例的socket变量可以赋给下一个前来的socket。 为什么await方法需要调用notifyAll呢? 这是为了防止在available为true的时候另一个socket到来。在这种情况下，连接器线程将会在assign方法的while循环中停止，直到接收到处理器线程的notifyAll调用。
  请求对象
  默认连接器哩变得HTTP请求对象指代org.apache.catalina.Request接口。这个接口被类RequestBase直接实现了，也是HttpRequest的父接口。最终的实现是继承于HttpRequest的
  HttpRequestImpl。像第3章一样，有几个facade类：RequestFacade和HttpRequestFacade。Request接口和它的实现类的UML图在Figure 4.2中给出。注意的是，除了属于javax.servlet和javax.servlet.http包的类，前缀org.apache.catalina已经被省略了。
  Figure 4.2: The Request interface and related types 如果你理解第3章的请求对象，理解这个结构图你应该不会遇到什么困难。
  响应对象
  Response接口和它的实现类的UML图在Figure 4.3中给出。
  Figure 4.3: The Response interface and its implementation classes
  处理请求
  到这个时候，你已经理解了请求和响应对象，并且知道HttpConnector对象是如何创建它们的。现在是这个过程的最后一点东西了。在这节中我们关注HttpProcessor类的process方法，它是一个套接字赋给它之后，在HttpProcessor类的run方法中调用的。process方法会做下面
  这些工作：

* 解析连接

* 解析请求

* 解析头部
  在解释完process方法之后，在本节的各个小节中将讨论每个操作。 process方法使用布尔变量ok来指代在处理过程中是否发现错误，并使用布尔变量finishResponse来指代Response接口中的finishResponse方法是否应该被调用。 boolean ok = true; boolean finishResponse = true; 另外，process方法也使用了布尔变量keepAlive,stopped和http11。keepAlive表示连接是否是持久的，stopped表示HttpProcessor实例是否已经被连接器终止来确认process是否也应该停止，http11表示 从web客户端过来的HTTP请求是否支持HTTP 1.1。 像第3章那样，有一个SocketInputStream实例用来包装套接字的输入流。注意的是，SocketInputStream的构造方法同样传递了从连接器获得的缓冲区大小，而不是从HttpProcessor的本地变量获得。这是因为对于默认连接器的用户而言，HttpProcessor是不可访问的。通过传递Connector接口的缓冲区大小，这就使得使用连接器的任何人都可以设置缓冲大小。 SocketInputStream input = null; OutputStream output = null; // Construct and initialize the objects we will need try { input = new SocketInputStream(socket.getInputstream(), connector.getBufferSize()); } catch (Exception e) { ok = false; } 然后，有个while循环用来保持从输入流中读取，直到HttpProcessor被停止，一个异常被抛出或者连接给关闭为止。 keepAlive = true; while (!stopped && ok && keepAlive) { ... } 在while循环的内部，process方法首先把finishResponse设置为true，并获得输出流，并对请求和响应对象做些初始化处理。
  finishResponse = true; try { request.setStream(input); request.setResponse(response); output = socket.getOutputStream(); response.setStream(output); response.setRequest(request); ((HttpServletResponse) response.getResponse()).setHeader("Server", SERVER_INFO); }
  catch (Exception e) { log("process.create", e); //logging is discussed in Chapter 7 ok = false; } 接着，process方法通过调用parseConnection，parseRequest和parseHeaders方法开始解析前来的HTTP请求，这些方法将在这节的小节中讨论。 try { if (ok) { parseConnection(socket); parseRequest(input, output); if (!request.getRequest().getProtocol().startsWith("HTTP/0")) parseHeaders(input); parseConnection方法获得协议的值，像HTTP0.9, HTTP1.0或HTTP1.1。如果协议是HTTP1.0，keepAlive设置为false，因为HTTP1.0不支持持久连接。如果在HTTP请求里边找到Expect: 100-continue的头部信息，则parseHeaders方法将把sendAck设置为true。 如果协议是HTTP1.1，并且web客户端发送头部Expect: 100-continue的话，通过调用ackRequest方法它将响应这个头部。它将会测试组块是否是允许的。 if (http11) { // Sending a request acknowledge back to the client if requested. ackRequest(output); // If the protocol is HTTP/1.1, chunking is allowed. if (connector.isChunkingAllowed()) response.setAllowChunking(true); } ackRequest方法测试sendAck的值，并在sendAck为true的时候发送下面的字符串： HTTP/1.1 100 Continue\r\n\r\n 在解析HTTP请求的过程中，有可能会抛出异常。任何异常将会把ok或者finishResponse设置为false。在解析过后，process方法把请求和响应对象传递给容器的invoke方法： try { ((HttpServletResponse) response).setHeader("Date", FastHttpDateFormat.getCurrentDate()); if (ok) { connector.getContainer().invoke(request, response); } } 接着，如果finishResponse仍然是true，响应对象的finishResponse方法和请求对象的finishRequest方法将被调用，并且结束输出。 if (finishResponse) { ... response.finishResponse(); ... request.finishRequest(); ... output.flush();
  while循环的最后一部分检查响应的Connection头部是否已经在servlet内部设为close，或者协议是HTTP1.0.如果是这种情况的话，keepAlive设置为false。同样，请求和响应对象接着会被回收利用。 if ( "close".equals(response.getHeader("Connection")) ) { keepAlive = false; } // End of request processing status = Constants.PROCESSOR_IDLE; // Recycling the request and the response objects request.recycle(); response.recycle(); } 在这个场景中，如果哦keepAlive是true的话，while循环将会在开头就启动。因为在前面的解析过程中和容器的invoke方法中没有出现错误，或者HttpProcessor实例没有被停止。否则，shutdownInput方法将会调用，而套接字将被关闭。 try { shutdownInput(input); socket.close(); } ... shutdownInput方法检查是否有未读取的字节。如果有的话，跳过那些字节。
  解析连接
  parseConnection方法从套接字中获取到网络地址并把它赋予HttpRequestImpl对象。它也检查是否使用代理并把套接字赋予请求对象。parseConnection方法在Listing4.2中列出。 Listing 4.2: The parseConnection method private void parseConnection(Socket socket) throws IOException, ServletException { if (debug >= 2) log(" parseConnection: address=" + socket.getInetAddress() + ", port=" + connector.getPort()); ((HttpRequestImpl) request).setInet(socket.getInetAddress()); if (proxyPort != 0) request.setServerPort(proxyPort); else request.setServerPort(serverPort); request.setSocket(socket); }
  解析请求
  parseRequest方法是第3章中类似方法的完整版本。如果你很好的理解第3章的话，你通过阅读这个方法应该可以理解这个方法是怎么运行的。
  解析头部
  默认链接器的parseHeaders方法使用包org.apache.catalina.connector.http里边的HttpHeader和DefaultHeaders类。类HttpHeader指代一个HTTP请求头部。类HttpHeader不是像第3章那样使用字符串，而是使用字符数据用来避免昂贵的字符串操作。类DefaultHeaders是一个final类，在字符数组中包含了标准的HTTP请求头部： standard HTTP request headers in character arrays: static final char[] AUTHORIZATION_NAME = "authorization".toCharArray(); static final char[] ACCEPT_LANGUAGE_NAME = "accept-language".toCharArray(); static final char[] COOKIE_NAME = "cookie".toCharArray(); ... parseHeaders方法包含一个while循环，可以持续读取HTTP请求直到再也没有更多的头部可以读取到。while循环首先调用请求对象的allocateHeader方法来获取一个空的HttpHead实例。这个实例被传递给 SocketInputStream的readHeader方法。 HttpHeader header = request.allocateHeader(); // Read the next header input.readHeader(header); 假如所有的头部都被已经被读取的话，readHeader方法将不会赋值给HttpHeader实例，这个时候parseHeaders方法将会返回。 if (header.nameEnd == 0) { if (header.valueEnd == 0) { return; } else { throw new ServletException(sm.getString("httpProcessor.parseHeaders.colon")); } } 如果存在一个头部的名称的话，这里必须同样会有一个头部的值： String value = new String(header.value, 0, header.valueEnd); 接下去，像第3章那样，parseHeaders方法将会把头部名称和DefaultHeaders里边的名称做对比。注意的是，这样的对比是基于两个字符数组之间，而不是两个字符串之间的。
  if (header.equals(DefaultHeaders.AUTHORIZATION_NAME)) { request.setAuthorization(value); } else if (header.equals(DefaultHeaders.ACCEPT_LANGUAGE_NAME)) { parseAcceptLanguage(value); } else if (header.equals(DefaultHeaders.COOKIE_NAME)) { // parse cookie } else if (header.equals(DefaultHeaders.CONTENT_LENGTH_NAME)) { // get content length } else if (header.equals(DefaultHeaders.CONTENT_TYPE_NAME)) {
  request.setContentType(value); } else if (header.equals(DefaultHeaders.HOST_NAME)) { // get host name } else if (header.equals(DefaultHeaders.CONNECTION_NAME)) { if (header.valueEquals(DefaultHeaders.CONNECTION_CLOSE_VALUE)) { keepAlive = false; response.setHeader("Connection", "close"); } } else if (header.equals(DefaultHeaders.EXPECT_NAME)) { if (header.valueEquals(DefaultHeaders.EXPECT_100_VALUE)) sendAck = true; else throw new ServletException(sm.getstring ("httpProcessor.parseHeaders.unknownExpectation")); } else if (header.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)) { //request.setTransferEncoding(header); } request.nextHeader();
  简单容器的应用程序
  本章的应用程序的主要目的是展示默认连接器是怎样工作的。它包括两个类： ex04.pyrmont.core.SimpleContainer和ex04 pyrmont.startup.Bootstrap。类 SimpleContainer实现了org.apache.catalina.container接口，所以它可以和连接器关联。类Bootstrap是用来启动应用程序的，我们已经移除了第3章带的应用程序中的连接器模块，类ServletProcessor和 StaticResourceProcessor，所以你不能请求一个静态页面。 类SimpleContainer展示在Listing 4.3. Listing 4.3: The SimpleContainer class
  package ex04.pyrmont.core; import java.beans.PropertyChangeListener; import java.net.URL; import java.net.URLClassLoader; import java.net.URLStreamHandler; import java.io.File; import java.io.IOException; import javax.naming.directory.DirContext; import javax.servlet.Servlet; import javax.servlet.ServletException; import javax.servlet.http.HttpServletRequest; import javax.servlet.http.HttpServletResponse;
  import org.apache.catalina.Cluster; import org.apache.catalina.Container; import org.apache.catalina.ContainerListener; import org.apache.catalina.Loader; import org.apache.catalina.Logger; import org.apache.catalina.Manager; import org.apache.catalina.Mapper; import org.apache.catalina.Realm; import org.apache.catalina.Request; import org.apache.catalina.Response; public class SimpleContainer implements Container { public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot"; public SimpleContainer() { } public String getInfo() { return null; } public Loader getLoader() { return null; } public void setLoader(Loader loader) { } public Logger getLogger() { return null; } public void setLogger(Logger logger) { } public Manager getManager() { return null; } public void setManager(Manager manager) { } public Cluster getCluster() { return null; } public void setCluster(Cluster cluster) { } public String getName() { return null; } public void setName(String name) { } public Container getParent() { return null; } public void setParent(Container container) { } public ClassLoader getParentClassLoader() { return null; }
  public void setParentClassLoader(ClassLoader parent) { } public Realm getRealm() { return null; } public void setRealm(Realm realm) { } public DirContext getResources() { return null; } public void setResources(DirContext resources) { } public void addChild(Container child) { } public void addContainerListener(ContainerListener listener) { } public void addMapper(Mapper mapper) { } public void addPropertyChangeListener( PropertyChangeListener listener) { } public Container findchild(String name) { return null; } public Container[] findChildren() { return null; } public ContainerListener[] findContainerListeners() { return null; } public Mapper findMapper(String protocol) { return null; } public Mapper[] findMappers() { return null; } public void invoke(Request request, Response response) throws IoException, ServletException { string servletName = ( (Httpservletrequest) request).getRequestURI(); servletName = servletName.substring(servletName.lastIndexof("/") + 1); URLClassLoader loader = null; try { URL[] urls = new URL[1]; URLStreamHandler streamHandler = null; File classpath = new File(WEB_ROOT); string repository = (new URL("file",null, classpath.getCanonicalpath() + File.separator)).toString(); urls[0] = new URL(null, repository, streamHandler); loader = new URLClassLoader(urls);
  } catch (IOException e) { System.out.println(e.toString() ); } Class myClass = null; try { myClass = loader.loadclass(servletName); } catch (classNotFoundException e) { System.out.println(e.toString()); } servlet servlet = null; try { servlet = (Servlet) myClass.newInstance(); servlet.service((HttpServletRequest) request, (HttpServletResponse) response); } catch (Exception e) { System.out.println(e.toString()); } catch (Throwable e) { System.out.println(e.toString()); } } public Container map(Request request, boolean update) { return null; } public void removeChild(Container child) { } public void removeContainerListener(ContainerListener listener) { } public void removeMapper(Mapper mapper) { } public void removoPropertyChangeListener( PropertyChangeListener listener) { } } 我只是提供了SimpleContainer类的invoke方法的实现，因为默认连接器将会调用这个方法。invoke方法创建了一个类加载器，加载servlet类，并调用它的service方法。这个方法和第3章的ServletProcessor类在哦个的process方法非常类似。 Bootstrap类在Listing 4.4在列出. Listing 4.4: The ex04.pyrmont.startup.Bootstrap class
  package ex04.pyrmont.startup; import ex04.pyrmont.core.simplecontainer; import org.apache.catalina.connector.http.HttpConnector; public final class Bootstrap { public static void main(string[] args) {
  HttpConnector connector = new HttpConnector(); SimpleContainer container = new SimpleContainer(); connector.setContainer(container); try { connector.initialize(); connector.start(); // make the application wait until we press any key. System in.read(); } catch (Exception e) { e.printStackTrace(); } } } Bootstrap 类的main方法构造了一个org.apache.catalina.connector.http.HttpConnector实例和一个 SimpleContainer实例。它接下去调用conncetor的setContainer方法传递container，让connector和container关联起来。下一步，它调用connector的initialize和start方法。这将会使得connector为处理8080端口上的任何请求做好了准备。 你可以通过在控制台中输入一个按键来终止这个应用程序。
  运行应用程序
  要在Windows中运行这个程序的话，在工作目录下输入以下内容： java -classpath ./lib/servlet.jar;./ ex04.pyrmont.startup.Bootstrap 在Linux的话，你可以使用分号来分隔两个库。 java -classpath ./lib/servlet.jar:./ ex04.pyrmont.startup.Bootstrap 你可以和第三章那样调用PrimitiveServlet和ModernServlet。 注意的是你不能请求index.html，因为没有静态资源的处理器。
  总结
  本章展示了如何构建一个能和Catalina工作的Tomcat连接器。剖析了Tomcat4的默认连接器的代码并用这个连接器构建了一个小应用程序。接下来的章节的所有应用程序都会使用默认连接器。