package org.apache.kafka.mytest.network;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * 只接收一个连接，用于测试io
 *
 * Created by wuzhsh on 2018/9/25.
 */
public class NioServer {

    public static void main(String[] args) throws Exception {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.socket().bind(new InetSocketAddress(8989));

        while (!channel.socket().isBound()) {
            System.out.println("正在启动服务，请稍候...");
        }
        System.out.println("服务器已启动");
        channel.configureBlocking(false);

        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int selectRes = selector.select();
            if (selectRes > 0) {
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();

                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
                        socketChannel.configureBlocking(false);
                        // 注册写事件
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    if (key.isReadable()) {
                        // 取消channel的读事件
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                        SocketChannel readChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(50);
                        int res = readChannel.read(buffer);
                        if (res == -1) {
                            // EOS
                            readChannel.close();
                        } else {
                            buffer.flip();

                            // 输出接收到消息的时间以及客户端发送的消息（客户端发送的消息中包含了客户端发送的时间）
                            System.out.println("[" + new Date() + "]" + new String(buffer.array()));

                            // sleep5秒，模拟对消息的处理，5秒之后再注册读事件
                            TimeUnit.SECONDS.sleep(5);
                            readChannel.register(selector, SelectionKey.OP_READ);
                        }
                    }

                    keys.remove();
                }
            }
        }
    }
}
