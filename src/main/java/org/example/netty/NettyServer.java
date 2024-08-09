package org.example.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author l1uhang
 * @className MyServer.java
 * @description Netty服务端启动类
 * @since 2024-08-08 18:01:16
 */
public class NettyServer {
    public static void main(String[] args) {
        //创建两个线程组，bossGroup只处理连接请求，workerGroup处理业务逻辑
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //创建服务端启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //配置参数
            serverBootstrap.group(bossGroup, workerGroup)
                    //使用NioServerSocketChannel作为服务器的通道实现
                    .channel(NioServerSocketChannel.class)
                    //设置线程队列得到连接个数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //使用匿名内部类方式创建通道初始化对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //给pipeline管道添加自定义处理器
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            System.out.println("服务器 is ready...");
            //绑定端口并启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(6666).sync();
            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 自定义Handler 内部类
     *   自定义的Handler需要继承Netty规定好的HandlerAdapter
     *   才能被Netty框架所关联，有点类似SpringMVC的适配器模式
     */
    public static class NettyServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("服务器读取线程：" + Thread.currentThread().getName());
            System.out.println("server ctx = " + ctx);
            System.out.println("看看channel和pipeline的关系");
            SocketChannel channel = (SocketChannel) ctx.channel();
            System.out.println("channel.remoteAddress() = " + channel.remoteAddress());
            System.out.println("channel.localAddress() = " + channel.localAddress());
            System.out.println("channel.parent() = " + channel.parent());
            //获取客户端发送过来的消息
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println("客户端发送的消息是：" + byteBuf.toString(io.netty.util.CharsetUtil.UTF_8));
        }
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            //将数据写入到缓存，并刷新
            //一般讲，对发送的数据进行编码
            ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端", io.netty.util.CharsetUtil.UTF_8));
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //发生异常，关闭通道
            ctx.close();
        }
    }
}
