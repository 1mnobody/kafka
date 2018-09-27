package org.apache.kafka.mytest.producer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuzhsh on 2018/6/8.
 */
public class ProducerDemo {
    public static void main(String args[]) {
        Properties p = new Properties();
        p.put("bootstrap.servers", "localhost:9092");
        p.put("client.id", "demoProducer");
        p.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        p.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer producer = new KafkaProducer(p);
        String topic = "kafka_action";
        int messageNo = 1;
        boolean isAsync = true;
        while (true) {
            String message = "message__" + messageNo;
            long startTime = System.currentTimeMillis();
            if (isAsync) {
                producer.send(new ProducerRecord(topic, messageNo, message), new DemoCallBack(startTime, messageNo, message));
            } else {
                try {
                    producer.send(new ProducerRecord(topic, messageNo, message)).get();
                    System.out.println("Sent message (" + messageNo + ", " + message + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messageNo++;
        }
    }
}

class DemoCallBack implements Callback {
    long timestamp;
    int messageNo;
    String message;

    public DemoCallBack(long timestamp, int messageNo, String message) {
        this.timestamp = timestamp;
        this.messageNo = messageNo;
        this.message = message;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        long costTimes = System.currentTimeMillis() - timestamp;
        if (metadata != null) {
            System.out.println("message(" + messageNo + ", " + message + ") sent to partition("
                    + metadata.partition() + "), offset(" + metadata.offset() + ") in " + costTimes + " ms");
        } else {
            exception.printStackTrace();
        }
    }
}