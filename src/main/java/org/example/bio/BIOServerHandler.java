package org.example.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author l1uhang
 * @className BIOServerHandler.java
 * @description BIO服务端多线程处理
 * @since 2024-08-09 11:26:12
 */
public class BIOServerHandler implements Runnable{

    private Socket socket;

    public BIOServerHandler(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            //接收客户端信息
            in = socket.getInputStream();
            out = socket.getOutputStream();
            Integer port = socket.getPort();
            int maxLen = 1024;
            byte[] buffer = new byte[maxLen];
            //使用线程，同样无法解决read阻塞问题
            //也就是说，read方法也同样会被阻塞，直到系统有数据准备好
            int realLen = in.read(buffer, 0, maxLen);
            String message = new String(buffer, 0, realLen);
            System.out.println("服务端收到客户端" + port + "号客户端的信息：" + message);
            out.write(("服务端已收到客户端" + port + "号客户端的信息：" + message).getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally{
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
