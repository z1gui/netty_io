package org.example.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author l1uhang
 * @className NIOClient.java
 * @description NIO 客户端
 * @since 2024-08-09 16:04:44
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 8888);
        OutputStream out = socket.getOutputStream();
        String s = "hello world";
        out.write(s.getBytes());
        out.close();
    }
}
