package org.example.bio;


import java.util.concurrent.CountDownLatch;

/**
 * @author l1uhang
 * @className BIOClient.java
 * @description 创建20个线程，模拟20个客户端同时发送请求
 * @since 2024-08-09 09:44:26
 */
public class BIOClient {


    public static void main(String[] args0) throws Exception {
        int clientNum = 20;
        System.out.println("客户端启动，本次启动"+clientNum+"个客户端");
        CountDownLatch countDownLatch = new CountDownLatch(clientNum);

        //分别开始启动这20个客户端
        for(int i=0;i<clientNum;i++,countDownLatch.countDown()){
            BIOClientHandler client = new BIOClientHandler(countDownLatch,i);
            new Thread(client).start();
        }
        synchronized (BIOClient.class){
                BIOClient.class.wait();

        }
    }
}
