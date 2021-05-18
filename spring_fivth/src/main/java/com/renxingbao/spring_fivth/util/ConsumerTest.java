package com.renxingbao.spring_fivth.util;







import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringEncoder;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;



public class ConsumerTest {
    static final String topic = "test";

    public static void main(String[] args) {
        Properties prop = new Properties();
        prop.put("zookeeper.connect", "47.106.151.28:2181");
        prop.put("metadata.broker.list", "47.106.151.28:9092");
        prop.put("serializer.class", StringEncoder.class.getName());
        prop.put("group.id", "group1");
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(prop));
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = consumer.createMessageStreams(topicCountMap);
        final KafkaStream<byte[], byte[]> kafkaStream = messageStreams.get(topic).get(0);
        ConsumerIterator<byte[], byte[]> iterator = kafkaStream.iterator();
        while (iterator.hasNext()) {
            String msg = new String(iterator.next().message());
            System.out.println("收到消息："+msg);
        }
    }

}
