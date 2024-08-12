# netty_io

BIO，NIO，Netty 实例

> 不管是 BIO，NIO，Netty，请先启动 Server ，在启动 Client 访问

## 各个文件的的声明

### BIO
- BIOClient.java 创建 20 个线程，模拟 20 个客户端同时发送请求
- BIOClientHandler.java 客户端执行线程
- BIOServer.java 服务端
- BIOServer2.java BIO 多线程服务端
- BIOServerHandler.java BIO 服务端多线程处理

### NIO
- NIOClient.java NIO 客户端
- NIOServer.java NIO 服务端
- NIOServer2.java NIO 框架实现简单的 IO 多路复用
- NIOFileCopy.java NIO 快速复制文件

### Netty
- NettyClient.java  Netty 客户端
- NettyServer.java  Netty 服务端

