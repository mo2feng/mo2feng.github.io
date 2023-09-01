# 第一章:一个简单的Web服务器
<show-structure for="chapter,procedure" depth="2"/>

本章说明java web服务器是如何工作的。Web服务器也成为超文本传输协议(HTTP)服务器，因为它使用HTTP来跟客户端进行通信的，这通常是个web浏览器。一个基于java的web服务器使用两个重要的类：java.net.Socket和java.net.ServerSocket，并通过HTTP消息进行通信。因此这章就自然是从HTTP和这两个类的讨论开始的。接下去，解释这章附带的一个简单的web服务器。

## 超文本传输协议(HTTP) {id="htw-http-protocol"}

HTTP是一种协议，允许web服务器和浏览器通过互联网进行来发送和接受数据。它是一种请求和响应协议。客户端请求一个文件而服务器响应请求。HTTP使用可靠的TCP连接--TCP默认使用80端口。第一个HTTP版是HTTP/0.9，然后被HTTP/1.0所替代。正在取代HTTP/1.0的是当前版本HTTP/1.1，它定义于征求意见文档(RFC) 2616，可以从http://www.w3.org/Protocols/HTTP/1.1/rfc2616.pdf下载。 


> **注意**：本节涵盖的HTTP 1.1只是简略的帮助你理解web服务器应用发送的消息。假如你对更多详细信息感兴趣，请阅读RFC 2616。 在HTTP中，始终都是客户端通过建立连接和发送一个HTTP请求从而开启一个事务。web服务器不需要联系客户端或者对客户端做一个回调连接。无论是客户端或者服务器都可以提前终止连接。举例来说，当你正在使用一个web浏览器的时候，可以通过点击浏览器上的停止按钮来停止一个文件的下载进程，从而有效的关闭与web服务器的HTTP连接。

### HTTP请求 {id="htw-http-request"}

一个HTTP请求包括三个组成部分：

* 方法—统一资源标识符(URI)—协议/版本

* 请求的头部

* 主体内容

下面是一个HTTP请求的例子：

```http
POST /examples/default.jsp HTTP/1.1 
Accept: text/plain; text/html 
Accept-Language: en-gb 
Connection: Keep-Alive 
Host: localhost 
User-Agent: Mozilla/4.0 (compatible; MSIE 4.01; Windows 98) 
Content-Length: 33 
Content-Type: application/x-www-form-urlencoded 
Accept-Encoding: gzip, deflate
 
lastName=Franks&firstName=Michael
```

 方法—统一资源标识符(URI)—协议/版本出现在请求的第一行。 

```http
POST /examples/default.jsp HTTP/1.1
```

这里POST是请求方法，`/examples/default.jsp`是URI，而HTTP/1.1是协议/版本部分。 

每个HTTP请求可以使用HTTP标准里边提到的多种方法之一。HTTP 1.1支持7种类型的请求：`GET`, `POST`, `HEAD`, `OPTIONS`, `PUT`, `DELETE`和`TRACE`。`GET`和`POST`在互联网应用里边最普遍使用的。 

URI完全指明了一个互联网资源。URI通常是相对服务器的根目录解释的。因此，始终一斜线/开头。统一资源定位器(URL)其实是一种URI(查看[http://www.ietf.org/rfc/rfc2396.txt](http://www.ietf.org/rfc/rfc2396.txt))。该协议版本代表了正在使用的HTTP协议的版本。 

请求的头部包含了关于客户端环境和请求的主体内容的有用信息。例如它可能包括浏览器设置的语言，主体内容的长度等等。每个头部通过一个回车换行符(`CRLF``)来分隔的。 

对于HTTP请求格式来说，头部和主体内容之间有一个回车换行符(`CRLF`)是相当重要的。`CRLF`告诉HTTP服务器主体内容是在什么地方开始的。在一些互联网编程书籍中，CRLF还被认为是HTTP请求的第四部分。 

在前面一个HTTP请求中，主体内容只不过是下面一行： `lastName=Franks&firstName=Michael `实体内容在一个典型的HTTP请求中可以很容易的变得更长。

### HTTP响应 {id="htw-http-response"}

类似于HTTP请求，一个HTTP响应也包括三个组成部分：

* 方法—统一资源标识符(URI)—协议/版本
* 响应的头部
* 主体内容

下面是一个HTTP响应的例子： 

```http
HTTP/1.1 200 OK 
Server: Microsoft-IIS/4.0 
Date: Mon, 5 Jan 2004 13:13:33 GMT 
Content-Type: text/html 
Last-Modified: Mon, 5 Jan 2004 13:13:12 GMT 
Content-Length: 112 

<html> <head> <title>HTTP Response Example</title> </head> <body> Welcome to Brainy Software </body> </html>
```

响应头部的第一行类似于请求头部的第一行。第一行告诉你该协议使用HTTP 1.1，请求成功(200=成功)，表示一切都运行良好。

响应头部和请求头部类似，也包括很多有用的信息。响应的主体内容是响应本身的HTML内容。头部和主体内容通过CRLF分隔开来。

## `Socket`类 {id="htw-java-socket"}

套接字是网络连接的一个端点。套接字使得一个应用可以从网络中读取和写入数据。放在两个不同计算机上的两个应用可以通过连接发送和接受字节流。为了从你的应用发送一条信息到另一个应用，你需要知道另一个应用的IP地址和套接字端口。在Java里边，套接字指的是`java.net.Socket`类。 

要创建一个套接字，你可以使用`Socket`类众多构造方法中的一个。其中一个接收主机名称和端口号： 

```java
public Socket (java.lang.String host, int port) 
```

在这里主机是指远程机器名称或者IP地址，端口是指远程应用的端口号。例如，要连接yahoo.com的80端口，你需要构造以下的Socket对象： 

```java
new Socket ("yahoo.com", 80);
```

 一旦你成功创建了一个Socket类的实例，你可以使用它来发送和接受字节流。要发送字节流，你首先必须调用`Socket`类的`getOutputStream`方法来获取一个`java.io.OutputStream`对象。要发送文本到一个远程应用，你经常要从返回的OutputStream对象中构造一个java.io.PrintWriter对象。要从连接的另一端接受字节流，你可以调用Socket类的`getInputStream`方法用来返回一个`java.io.InputStream`对象。 

以下的代码片段创建了一个套接字，可以和本地HTTP服务器(127.0.0.1是指本地主机)进行通讯，发送一个HTTP请求，并从服务器接受响应。它创建了一个`StringBuffer`对象来保存响应并在控制台上打印出来。

```java
class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("127.0.0.1", 8080);
        OutputStream os = socket.getOutputStream();
        boolean autoflush = true;
        PrintWriter out = new PrintWriter(socket.getOutputStream(), autoflush);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // send an HTTP request to the web server
        out.println("GET /index.jsp HTTP/1.1");
        out.println("Host: localhost:8080");
        out.println("Connection: Close");
        out.println(); // read the response
        boolean loop = true;
        StringBuffer sb = new StringBuffer(8096);
        while (loop) {
            if (in.ready()) {
                int i = 0;
                while (i != -1) {
                    i = in.read();
                    sb.append((char) i);
                }
                loop = false;
            }
            Thread.currentThread().sleep(50);
        }
        // display the response to the out console
        System.out.println(sb.toString());
        socket.close();
    }
}
```

请注意，为了从web服务器获取适当的响应，你需要发送一个遵守HTTP协议的HTTP请求。假如你已经阅读了前面一节超文本传输协议(HTTP)，你应该能够理解上面代码提到的HTTP请求。 

> **注意**：你可以本书附带的`com.brainysoftware.pyrmont.util.HttpSniffe`类来发送一个HTTP请求并显示响应。要使用这个Java程序，你必须连接到互联网上。虽然它有可能并不会起作用，假如你有设置防火墙的话。

## `ServerSocket`类 {id="htw-java-serversocket"}

Socket类代表一个客户端套接字，即任何时候你想连接到一个远程服务器应用的时候你构造的套接字，现在，假如你想实施一个服务器应用，例如一个HTTP服务器或者FTP服务器，你需要一种不同的做法。这是因为你的服务器必须随时待命，因为它不知道一个客户端应用什么时候会尝试去连接它。为了让你的应用能随时待命，你需要使用`java.net.ServerSocket`类。这是服务器套接字的实现。 

`ServerSocket`和`Socket`不同，服务器套接字的角色是等待来自客户端的连接请求。一旦服务器套接字获得一个连接请求，它创建一个Socket实例来与客户端进行通信。 

要创建一个服务器套接字，你需要使用`ServerSocket`类提供的四个构造方法中的一个。你需要指定IP地址和服务器套接字将要进行监听的端口号。通常，IP地址将会是127.0.0.1，也就是说，服务器套接字将会监听本地机器。服务器套接字正在监听的IP地址被称为是绑定地址。服务器套接字的另一个重要的属性是`backlog`，这是服务器套接字开始拒绝传入的请求之前，传入的连接请求的最大队列长度。 

其中一个`ServerSocket`类的构造方法如下所示: 

```java
public ServerSocket(int port, int backLog, InetAddress bindingAddress); 
```

对于这个构造方法，绑定地址必须是`java.net.InetAddress`的一个实例。一种构造InetAddress对象的简单的方法是调用它的静态方法`getByName`，传入一个包含主机名称的字符串，就像下面的代码一样。

```java
InetAddress.getByName("127.0.0.1");
```

下面一行代码构造了一个监听的本地机器8080端口的`ServerSocket`，它的backlog为1。 

```java
new ServerSocket(8080, 1, InetAddress.getByName("127.0.0.1")); 
```

一旦你有一个`ServerSocket`实例，你可以让它在绑定地址和服务器套接字正在监听的端口上等待传入的连接请求。你可以通过调用`ServerSocket`类的`accept`方法做到这点。这个方法只会在有连接请求时才会返回，并且返回值是一个Socket类的实例。`Socket`对象接下去可以发送字节流并从客户端应用中接受字节流，就像前一节"Socket类"解释的那样。实际上，这章附带的程序中，accept方法是唯一用到的方法。

## 应用程序 {id="htw-application"}

我们的web服务器应用程序放在`ex01.pyrmont`包里边，由三个类组成：

* `HttpServer`
* `Request`
* `Response`

这个应用程序的入口点(静态main方法)可以在HttpServer类里边找到。main方法创建了一个HttpServer的实例并调用了它的await方法。await方法，顾名思义就是在一个指定的端口上等待HTTP请求,处理它们并发送响应返回客户端。它一直等待直至接收到shutdown命令。 应用程序不能做什么，除了发送静态资源，例如放在一个特定目录的HTML文件和图像文件。它也在控制台上显示传入的HTTP请求的字节流。不过，它不给浏览器发送任何的头部例如日期或者cookies。 现在我们将在以下各小节中看看这三个类。

### HttpServer类 {id="htw-http-server-class"}

HttpServer类代表一个web服务器并展示在Listing 1.1中。请注意，await方法放在Listing 1.2中，为了节省空间没有重复放在Listing 1.1中。

> Listing 1.1: `HttpServer`类

```java
package org.example;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    /**
     * WEB_ROOT is the directory where our HTML and other files reside.
     * For this package, WEB_ROOT is the "webroot" directory under the * working directory.
     * The working directory is the location in the file system
     * from where the java command was invoked.
     */
    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";
    // shutdown command 
    private static final String SHUTDOWN_COMMAND = "/SHUTDOWN"; // the shutdown command received 
    private boolean shutdown = false;

    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.await();
    }

    public void await() {
        //...
    }
}
```

> Listing 1.2: `HttpServer`类的await方法

```java
public void await() {
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } // Loop waiting for a request
        while (!shutdown) {
            Socket socket = null;
            InputStream input = null;
            OutputStream output = null;
            try {
                socket = serverSocket.accept();
                input = socket.getInputStream();
                output = socket.getOutputStream();
                // create Request object and parse
                Request request = new Request(input);
                request.parse();
                // create Response object 
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource();
                // Close the socket
                socket.close();
                //check if the previous URI is a shutdown command 
                shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }
```



web服务器能提供公共静态final变量WEB_ROOT所在的目录和它下面所有的子目录下的静态资源。如下所示，WEB_ROOT被初始化：

```java
public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot"; 
```

代码列表包括一个叫webroot的目录，包含了一些你可以用来测试这个应用程序的静态资源。你同样可以在相同的目录下找到几个servlet用于测试下一章的应用程序。为了请求一个静态资源，在你的浏览器的地址栏或者网址框里边敲入以下的URL： 

```
http://machineName:port/staticResource
```

如果你要从一个不同的机器上发送请求到你的应用程序正在运行的机器上，`machineName`应该是正在运行应用程序的机器的名称或者IP地址。假如你的浏览器在同一台机器上，你可以使用localhost作为machineName。端口是8080，staticResource是你需要请求的文件的名称，且必须位于WEB_ROOT里边。

举例来说，假如你正在使用同一台计算机上测试应用程序，并且你想要调用`HttpServer`对象去发送一个index.html文件，你可以使用一下的URL： http://localhost:8080/index.html

要停止服务器，你可以在web浏览器的地址栏或者网址框里边敲入预定义字符串，就在URL的`host`:`port`的后面，发送一个shutdown命令。

shutdown命令是在HttpServer类的静态final变量SHUTDOWN里边定义的： 
```java
private static final String SHUTDOWN_COMMAND = "/SHUTDOWN"; 
```
因此，要停止服务器，使用下面的URL： http://localhost:8080/SHUTDOWN 

现在我们来看看Listing 1.2印出来的await方法。 

使用方法名`await`而不是wait是因为wait方法是与线程相关的`java.lang.Object`类的一个重要方法。

`await`方法首先创建一个`ServerSocket`实例然后进入一个while循环。 
```java
serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1")); ... 
// Loop waiting for a request 
while (!shutdown) { ... } 
```
while循环里边的代码运行到`ServletSocket`的`accept`方法停了下来，只会在8080端口接收到一个HTTP请求的时候才返回： 

```java
socket = serverSocket.accept();
```
接收到请求之后，await方法从accept方法返回的Socket实例中取得`java.io.InputStream`和`java.io.OutputStream`对象。

```java
input = socket.getInputStream();
output = socket.getOutputStream();
```
 await方法接下去创建一个`org.example.Request`对象并且调用它的`parse`方法去解析HTTP请求的原始数据。 
 ```java
 // create Request object and parse 
 Request request = new Request(input); 
 request.parse();
 ```
在这之后，await方法创建一个`Response`对象，把`Request`对象设置给它，并调用它的`sendStaticResource`方法。 
```java
// create Response object 
Response response = new Response(output); 
response.setRequest(request);
response.sendStaticResource();
```
 最后，await关闭套接字并调用`Request`的`getUri`来检测HTTP请求的URI是不是一个shutdown命令。假如是的话，shutdown变量将被设置为true且程序会退出while循环。 
 ```java
  // Close the socket 
  socket.close (); 
  //check if the previous URI is a shutdown command 
  shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
 ```

### Request类 {id="htw-http-request-class"}
`ex01.pyrmont.Request`类代表一个HTTP请求。从负责与客户端通信的Socket中传递过来InputStream对象来构造这个类的一个实例。你调用InputStream对象其中一个read方法来获取HTTP请求的原始数据。 
`Request`类显示在Listing 1.3。`Request`对象有`parse`和`getUri`两个公共方法，分别在Listings 1.4和1.5列出来。 
> Listing 1.3: Request类
```java
package ex01.pyrmont;

import java.io.IOException;
import java.io.InputStream;
public class Request {
    private InputStream input;
    private String uri;

    public Request(InputStream input) {
        this.input = input;
    }

    public void parse() {
    }

    private String parseUri(String requestString) {
        //...
    }

    public String getUri() {
        return uri;
    }
} 

```
> Listing 1.4: Request类的parse方法
```java
    public void parse() {
        // Read a set of characters from the socket 
        StringBuffer request = new StringBuffer(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        System.out.print(request.toString());
        uri = parseUri(request.toString());
    }
```
> Listing 1.5: Request类的parseUri方法 
```java
    private String parseUri(String requestString) {
        int index1, index2;
        index1 = requestString.indexOf(' ');
        if (index1 != -1) {
            index2 = requestString.indexOf(' ', index1 + 1);
            if (index2 > index1) return requestString.substring(index1 + 1, index2);
        }
        return null;
    }
```
`parse`方法解析HTTP请求里边的原始数据。这个方法没有做很多事情。它唯一可用的信息是通过调用HTTP请求的私有方法`parseUri`获得的URI。`parseUri`方法在uri变量里边存储URI。公共方法getUri被调用并返回HTTP请求的URI。

> **注意**：在第3章和下面各章的附带程序里边，HTTP请求将会对原始数据进行更多的处理。 

为了理解parse和parseUri方法是怎样工作的，你需要知道上一节“超文本传输协议(HTTP)”讨论的HTTP请求的结构。在这一章中，我们仅仅关注HTTP请求的第一部分，请求行。请求行从一个方法标记开始，接下去是请求的URI和协议版本，最后是用回车换行符(CRLF)结束。请求行里边的元素是通过一个空格来分隔的。例如，使用GET方法来请求index.html文件的请求行如下所示。

```http
GET /index.html HTTP/1.1
```
`parse`方法从传递给`Requst`对象的套接字的`InputStream`中读取整个字节流并在一个缓冲区中存储字节数组。然后它使用缓冲区字节数据的字节来填入一个`StringBuffer`对象，并且把代表`StringBuffer`的字符串传递给`parseUri`方法。 `parse`方法列在Listing 1.4。 然后`parseUri`方法从请求行里边获得URI。Listing 1.5给出了`parseUri`方法。`parseUri`方法搜索请求里边的第一个和第二个空格并从中获取URI。

### Response类 {id="htw-http-response-class"}

`ex01.pyrmont.Response`类代表一个HTTP响应，在Listing 1.6里边给出。 

> Listing 1.6: Response类
```java
package ex01.pyrmont;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Response {
    private static final int BUFFER_SIZE = 1024;
    Request request;
    OutputStream output;

    public Response(OutputStream output) {
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void sendStaticResource() throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        try {
            File file = new File(HttpServer.WEB_ROOT, request.getUri());
            if (file.exists()) {
                fis = new FileInputStream(file);
                int ch = fis.read(bytes, 0, BUFFER_SIZE);
                while (ch != -1) {
                    output.write(bytes, 0, ch);
                    ch = fis.read(bytes, 0, BUFFER_SIZE);
                }
            } else {
                // file not found
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n" + "Content-Type: text/html\r\n" + "Content-Length: 23\r\n" + "\r\n" + "<h1>File Not Found</h1>";
                output.write(errorMessage.getBytes());
            }
        } catch (Exception e) {
            // thrown if you cannot instantiate a File object
            System.out.println(e.toString());
        } finally {
            if (fis != null) fis.close();
        }
    }
}
```
首先注意到它的构造方法接收一个`java.io.OutputStream`对象，就像如下所示。 
```java
public Response(OutputStream output) {
     this.output = output; 
} 
```
响应对象是通过传递由套接字获得的`OutputStream`对象给`HttpServer`类的`await`方法来构造的。`Response`类有两个公共方法：`setRequest`和`sendStaticResource`。`setRequest`方法用来传递一个`Request`对象给`Response`对象。 

`sendStaticResource`方法是用来发送一个静态资源，例如一个HTML文件。它首先通过传递上一级目录的路径和子路径给File累的构造方法来实例化`java.io.File`类。 

```java
File file = new File(HttpServer.WEB_ROOT, request.getUri());
```
然后它检查该文件是否存在。假如存在的话，通过传递`File`对象让`sendStaticResource`构造一个`java.io.FileInputStream`对象。然后，它调用`FileInputStream`的`read`方法并把字节数组写入`OutputStream`对象。请注意，这种情况下，静态资源是作为原始数据发送给浏览器的。

```java
if (file.exists()) { 
    fis = new FileInputstream(file);
    int ch = fis.read(bytes, 0, BUFFER_SIZE); 
    while (ch!=-1) { 
        output.write(bytes, 0, ch); 
        ch = fis.read(bytes, 0, BUFFER_SIZE); 
    } 
}
```

假如文件并不存在，`sendStaticResource`方法发送一个错误信息到浏览器。 

```java
String errorMessage = "Content-Type: text/html\r\n" 
+ "Content-Length: 23\r\n" 
+ "\r\n" 
+ "<h1>File Not Found</h1>"; 
output.write(errorMessage.getBytes());
```
 
### 运行应用程序 {id="htw-run-app"}
为了运行应用程序，可以在工作目录下敲入下面的命令： 
```java
java ex01.pyrmont.HttpServer 
```
为了测试应用程序，可以打开你的浏览器并在地址栏或网址框中敲入下面的命令： 
```
http://localhost:8080/index.html 
```
正如Figure 1.1所示，你将会在你的浏览器里边看到index.html页面。

> Figure 1.1: web服务器的输出 在控制台中，你可以看到类似于下面的HTTP请求：
```http
GET /index.html HTTP/1.1
Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg,application/vnd.ms-excel, application/msword, application/vnd.ms- powerpoint, application/x-shockwave-flash, application/pdf, */* 
Accept-Language: en-us 
Accept-Encoding: gzip, deflate 
User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322) 
Host: localhost:8080 
Connection: Keep-Alive 

GET /images/logo.gif HTTP/1.1 Accept: */* 
Referer: http://localhost:8080/index.html Accept-Language: en-us Accept-Encoding: gzip, deflate 
User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322) 
Host: localhost:8080 
Connection: Keep-Alive
```

## 总结 {id="htw-tldr"}
在这章中你已经看到一个简单的web服务器是如何工作的。这章附带的程序仅仅由三个类组成，并不是全功能的。不过，它提供了一个良好的学习工具。下一章将要讨论动态内容的处理过程。