package org.apache.kafka.mytest.network;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

/**
 * Created by wuzhsh on 2018/9/25.
 */
public class NioClient {

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket();
        InetSocketAddress endpoint = new InetSocketAddress("127.0.0.1", 8989);
        socket.connect(endpoint);
        while (!socket.isConnected()) {
            System.out.println("连接尚未完成，请稍候....");
        }
        OutputStream out = socket.getOutputStream();
        DataOutputStream d = new DataOutputStream(out);
        String msg = "hello, currentTime is " + new Date();
        d.write(msg.getBytes());
        System.out.println("消息1已经发送");

//        TimeUnit.MILLISECONDS.sleep(500);
        String msg2 = "message2  --- " + new Date();
        d.write(msg2.getBytes());
        System.out.println("消息2已经发送");

        d.close();
        out.close();
        socket.close();
    }
}
