package com.renxingbao.spring_fivth.util;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;

import java.util.Properties;

public class ProducerTest {

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        prop.put("zookeeper.connect", "47.106.151.28:2181");
        prop.put("metadata.broker.list", "47.106.151.28:9092");
        prop.put("serializer.class", StringEncoder.class.getName());
        //prop.put("request.required.acks", "1");
        Producer<String, String> producer = new Producer<String, String>(new ProducerConfig(prop));
        int i = 0;
        while (true) {
            producer.send(new KeyedMessage<String, String>("test", "msg:" + i++));
            System.out.println("发送消息成功！");
            Thread.sleep(1000);
        }
    }
}
