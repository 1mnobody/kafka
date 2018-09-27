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
                        TimeUnit.SECONDS.sleep(10);
                        // 这里休眠了10s 再注册 READ 事件，可以发现，消息仍然可以从客户端收到，说明数据实际上已经发送，
                        // 只不过在传输层缓存了起来。当程序要读取时，立即就返回了数据
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    if (key.isReadable()) {
                        SocketChannel readChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(50);
                        readChannel.read(buffer);
                        buffer.flip();
                        // 客户端将会发送一个时间过来，可以看到两个时间相差正好10s
                        System.out.println("[" + new Date() + "]" + new String(buffer.array()));
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                    }


                    keys.remove();
                }
            }
        }
    }
}
