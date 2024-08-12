package org.example.nio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author l1uhang
 * @className NIOServer2.java
 * @description NIO 框架实现简单的 IO 多路复用
 * @since 2024-08-09 17:23:19
 */
public class NIOServer2 {


    private static final ConcurrentMap<Integer, StringBuffer> MESSAGEHASHCONTEXT = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new java.net.InetSocketAddress(8888));

        Selector selector = Selector.open();
        // 注意，服务器通道只能注册 SelectionKey.OP_ACCEPT 事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        try {
            while (true) {
                // 如果条件成立，说明本次询问 selector，并没有获取到任何准备好的，感兴趣的时间
                // java 程序对多路复用 IO 的支持也包含了阻塞模式，和非阻塞两种
                if (selector.select(100) == 0) {
                    /**
                     * 这里可以执行一些与业务逻辑没有关系的操作。
                     */
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    // 这个已经处理的 key 一定要 remove 掉，不然，会一直存在集合中，
                    // 等下次 selector.select() > 0 的时候，这个 key 又会被处理一次
                    iterator.remove();
                    SelectableChannel selectableChannel = selectionKey.channel();
                    if (selectionKey.isValid() && selectionKey.isAcceptable()) {
                        /**
                         * 当 server socket 感兴趣的 OP_ACCEPT 事件被触发时，就可以从 server socket channel 中获取 socketchannel 了
                         * 拿到 socket channel 后，要做的事情就是马上到 selector 注册这个 socket channel 感兴趣的事情
                         * 否则无法金婷这个 socket channel 到达的数据
                         */
                        ServerSocketChannel server = (ServerSocketChannel) selectableChannel;
                        SocketChannel socketChannel = server.accept();
//                        socketChannel.configureBlocking(false);
                        registerSocketChannel(socketChannel, selector);
                        System.out.println("客户端的IP地址：" + socketChannel.getRemoteAddress() + "服务通道已经开启注册");
                    } else if (selectionKey.isValid() && selectionKey.isReadable()) {
                        System.out.println("socket channel 数据准备好了，可以进行读取了");
                        readSocketChannel2(selectionKey);
                    } else if (selectionKey.isValid() && selectionKey.isConnectable()) {
                        System.out.println("socket channel 连接成功");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 在 server socket channel 接收到一个新的 TCP 连接后，就会向程序返回一个新的 socketChannel，但是这个新的 socketchannel 并没有在 selector 中注册
     * 所以程序无法通过 selector 监听到这个新的 socketChannel，所以需要程序主动将新的 socketChannel 注册到 selector 中
     *
     * @param socketChannel 新的 socketChannel
     * @param selector      选择器、迭代器
     * @throws Exception
     */
    private static void registerSocketChannel(SocketChannel socketChannel, Selector selector) throws Exception {
        socketChannel.configureBlocking(false);
        // socket 通道只能注册三种事件：SelectionKey.OP_READ、SelectionKey.OP_WRITE、SelectionKey.OP_CONNECT
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private static void readSocketChannel(SelectionKey selectionKey) throws Exception {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        // 获取客户端的端口
        InetSocketAddress address = (InetSocketAddress) socketChannel.getRemoteAddress();
        System.out.println("客户端的IP地址：" + address.getAddress().getHostAddress());
        Integer port = address.getPort();
        System.out.println("客户端的端口号：" + port);

        // 拿到这个 ocket channel 使用的缓存区，准备读取数据
        ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
        // 将通道的数据写入到缓存区，住也是写入到缓存区
        // 由于之前设置了 ByteBuffer 的大小为 2048 byte，所有可能存在写入不完的情况
        int readLen = -1;
        try {
            readLen = socketChannel.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            socketChannel.close();
            return;
        }
        if (readLen == -1) {
            System.out.println("缓存区没有数据？");
            return;
        }
        // 将缓存区从写状态切换为读状态 (实际上这个方法是读写模式互切换)。
        // 这是 java nio 框架中的这个 socket channel 的写请求将全部等待。
        buffer.flip();
        // 注意中文乱码问题
        byte[] messageBytes = buffer.array();
        String message = new String(messageBytes, 0, readLen, StandardCharsets.UTF_8);

        // 如果收到"over"关键字，才会清空 buffer，并回发数据；
        if (message.contains("over")) {
            buffer.clear();
            System.out.println("收到客户端发送来的数据：" + message);
            /**
             * 接收完成之后，就可以正式处理业务了
             */

            // 回发数据，并关闭 channel
            ByteBuffer sendBuffer = ByteBuffer.wrap(URLEncoder.encode("hello,客户端", "UTF-8").getBytes());
            socketChannel.write(sendBuffer);
            socketChannel.close();
        } else {
            System.out.println("收到客户端发送来的数据：" + message);
            buffer.position(readLen);
            buffer.limit(buffer.capacity());

        }
    }

    /**
     * 对上述的 readSocketChannel 方法进行改造，优化缓存区，节约内存资源
     * <p>
     * 改进的 java nio server 的代码中，由于 buffer 的大小设置的比较小
     * 我们不再把一个 client 通过 socket channel 多次传给服务器的信息保存在 beff 中了 (因为根本存不下)
     * 我们使用 socketchanel 的 hashcode 作为 key(当然您也可以自己确定一个 id)，信息的 stringbuffer 作为 value，存储到服务器端的一个内存区域 MESSAGEHASHCONTEXT。
     */
    private static void readSocketChannel2(SelectionKey selectionKey) throws Exception {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
        int realLen = 0;
        StringBuffer message = new StringBuffer();
        while ((realLen = socketChannel.read(buffer)) != 0) {
            buffer.flip();
            int position = buffer.position();
            int capacity = buffer.capacity();
            byte[] messageBytes = new byte[capacity];
            buffer.get(messageBytes, position, realLen);

            String messageEncode = new String(messageBytes, 0, realLen, StandardCharsets.UTF_8);
            message.append(messageEncode);

        }
        if (URLDecoder.decode(message.toString(), "UTF-8").indexOf("over") != -1) {
            Integer channelUUID = socketChannel.hashCode();
            StringBuffer completeMessage;
            StringBuffer historyMessage = (StringBuffer) MESSAGEHASHCONTEXT.remove(channelUUID);
            if (historyMessage == null) {
                completeMessage = message;
            } else {
                completeMessage = historyMessage.append(message);
            }
            System.out.println("收到客户端发送来的数据：" + completeMessage);
            /**
             * 接收完成之后，就可以正式处理业务了
             */

            // 回发数据，并关闭 channel
            ByteBuffer sendBuffer = ByteBuffer.wrap(URLEncoder.encode("hello,客户端", "UTF-8").getBytes());
            socketChannel.write(sendBuffer);
            socketChannel.close();
        } else {

            System.out.println("收到客户端发送来的数据还没接收完，继续接受：" + message);
            Integer channelUUID = socketChannel.hashCode();
            StringBuffer historyMessage = (StringBuffer) MESSAGEHASHCONTEXT.get(channelUUID);
            if (historyMessage == null) {
                MESSAGEHASHCONTEXT.put(channelUUID, message);
            } else {
                historyMessage.append(message);
            }
        }

    }
}
