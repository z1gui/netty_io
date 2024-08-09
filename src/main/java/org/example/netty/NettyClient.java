package org.example.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

/**
 * @author l1uhang
 * @className MyClient.java
 * @description Netty客户端启动类
 * @since 2024-08-08 18:26:42
 */
public class NettyClient {
    public static void main(String[] args) {
        NioEventLoopGroup eventExecutor = new NioEventLoopGroup();
        try{
            //创建bootstrap对象，配置参数
            Bootstrap bootstrap = new Bootstrap();
            //设置线程组
            bootstrap.group(eventExecutor)
                    //设置客户端通道的实现类（反射）
                    .channel(NioSocketChannel.class)
                    //创建通道初始化对象
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            System.out.println("客户端准备就绪，随时可以起飞~");
            //连接服务端
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6666).sync();
            //等待通道关闭的监听器（异步的）
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //关闭线程组
            eventExecutor.shutdownGracefully();
        }
    }
    private static class NettyClientHandler extends ChannelInboundHandlerAdapter {
       @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.copiedBuffer("你好啊，我是客户端", CharsetUtil.UTF_8));
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println("客户端收到消息：" + byteBuf.toString(CharsetUtil.UTF_8));
        }
    }
}
