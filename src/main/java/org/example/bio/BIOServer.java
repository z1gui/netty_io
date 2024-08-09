package org.example.bio;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author l1uhang
 * @className BIOServer.java
 * @description BIO服务端
 * @since 2024-08-09 09:42:16
 */
public class BIOServer {

    public static void main(String[] args) throws Exception {
        System.out.println("BIO服务端启动");
        ServerSocket serverSocket = new ServerSocket(8080);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                // 处理客户端请求
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                int maxLen = 2048;
                byte[] buffer = new byte[maxLen];
                //
                int readLen = in.read(buffer, 0, maxLen);
                if (readLen > 0) {
                    String msg = new String(buffer, 0, readLen);
                    System.out.println("收到客户端消息：" + msg);
                    out.write("服务端已收到消息".getBytes());
                }
                in.close();
                out.close();
                socket.close();

            }
        } catch (Exception e) {
            System.out.println("BIO服务端启动失败" + e);
        } finally {
            System.out.println("BIO服务端关闭");
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}
