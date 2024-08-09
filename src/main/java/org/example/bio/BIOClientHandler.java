package org.example.bio;


import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * @author l1uhang
 * @className BIOClientHandler.java
 * @description 客户端执行线程
 * @since 2024-08-09 09:50:17
 */

public class BIOClientHandler implements Runnable{

    private CountDownLatch countDownLatch;

    private Integer clientIndex;

    /**
     * @description 构造函数
     * @param countDownLatch java提供的同步计数器，当计数器数值减为0时，所有等待的线程会被唤醒。这样保证模拟并发请求的真实性。
     * @param clientIndex
     */
    public BIOClientHandler(CountDownLatch countDownLatch, Integer clientIndex) {
        this.countDownLatch = countDownLatch;
        this.clientIndex = clientIndex;
    }

    @Override
    public void run() {
        System.out.println("客户端"+ clientIndex+"开始请求服务端");
        Socket socket = null;
        OutputStream clientRequest = null;
        InputStream clientResponse = null;
        try{
            socket = new Socket("127.0.0.1", 8080);
            clientRequest = socket.getOutputStream();
            clientResponse = socket.getInputStream();

            //等待线程全部就绪，模拟并发请求
            this.countDownLatch.await();
            //发送请求
            clientRequest.write(("这是客户端"+clientIndex+"的请求消息，请注意").getBytes());
            clientRequest.flush();
            //在这里等待，直到服务器端返回响应
            System.out.println("客户端"+clientIndex+"请求服务端成功，等待服务器端返回信息");
            int maxLength = 1024;
            byte[] buffer = new byte[maxLength];
            int realLen;
            String message = "";
            while ((realLen = clientResponse.read(buffer)) != -1){
                message += new String(buffer, 0, realLen);
            }
            System.out.println("客户端"+clientIndex+"收到服务端响应："+ message);
        }catch (Exception e){
            System.out.println("客户端"+clientIndex+"等待线程异常");
        }finally{
            try{
                if(clientRequest != null){
                    clientRequest.close();
                }
                if(clientResponse != null){
                    clientResponse.close();
                }
                if(socket != null){
                    socket.close();
                }
            }catch (Exception e){
                System.out.println("客户端"+clientIndex+"关闭连接异常" + e);
            }
        }


    }
}
