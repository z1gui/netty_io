package org.example.bio;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author l1uhang
 * @className BIOServer2.java
 * @description BIO多线程服务端
 * @since 2024-08-09 11:23:48
 */
public class BIOServer2 {
    public static void main(String[] args) throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(8080);
        try{
            while (true){
                Socket socket = serverSocket.accept();
                new Thread(new BIOServerHandler(socket)).start();
            }
        }catch (Exception e){
            System.out.println("服务端异常"+e);
        }finally{
            if(serverSocket!=null){
                serverSocket.close();
            }
        }
    }
}
