package org.example.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author l1uhang
 * @className NIOCopy.java
 * @description NIO 快速复制文件
 * @since 2024-08-09 15:55:55
 */
public class NIOFileCopy {
    public static void main(String[] args) throws IOException {
        String src = "/Users/leon/Downloads/123.txt";
        String dest = "/Users/leon/Downloads/456.txt";
        copy(src, dest);
    }

    public static void copy(String src, String dest) throws IOException {
        FileInputStream fin = new FileInputStream(src);
        FileChannel finChannel = fin.getChannel();
        FileOutputStream fout = new FileOutputStream(dest);
        FileChannel foutChannel = fout.getChannel();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        while (true) {
            int r = finChannel.read(buffer);
            if (r == -1) {
                break;
            }
            buffer.flip();
            foutChannel.write(buffer);
            buffer.clear();
        }
    }
}
