import kafka.tools.ConsoleProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author leofee
 */
public class TestKafka {

    private KafkaProducer<String, String> producer;

    @Before
    public void beforeProducer() {
        Properties properties = new Properties();

        // kafka server 地址
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.101:9092,192.168.1.102:9092,192.168.1.103:9092");

        // kafka 存储的是字节数组，所以生产者和消费者需要约定序列化方式
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "1");

        producer = new KafkaProducer<>(properties);
    }

    @Before
    public void beforeConsumer() {
        Properties properties = new Properties();

        // kafka server 地址
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.101:9092,192.168.1.102:9092,192.168.1.103:9092");

        // kafka 存储的是字节数组，所以生产者和消费者需要约定序列化方式
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(properties);
    }

    @Test
    public void producer() throws ExecutionException, InterruptedException {

        // 指定生产消息的topic
        ProducerRecord<String, String> record = new ProducerRecord<>("testTopic", "key1", "value1");
        Future<RecordMetadata> sent = producer.send(record);
        RecordMetadata recordMetadata = sent.get();
        System.out.println("partition:" + recordMetadata.partition());
        System.out.println("topic:" + recordMetadata.topic());
        System.out.println("offset:" + recordMetadata.offset());
    }

    /**
     * 同一类消息的key会发送到同一个分区
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void producer2() throws ExecutionException, InterruptedException {

        String topic = "testTopic";
        String prefix = "test";

        for (int i = 0; i < 5; i++) {
            String key = prefix + "-key-" + i;
            String value = prefix + "-val-" + i;
            // 指定生产消息的topic
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            Future<RecordMetadata> sent = producer.send(record);
            RecordMetadata recordMetadata = sent.get();
            System.out.println("key : " + key + ", partition:" + recordMetadata.partition());
            System.out.println("key : " + key + ", offset:" + recordMetadata.offset());
        }
    }

    @Test
    public void consumer() {

        Properties properties = new Properties();

        // kafka server 地址
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.101:9092,192.168.1.102:9092,192.168.1.103:9092");

        // kafka 存储的是字节数组，所以生产者和消费者需要约定序列化方式
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());


        // 设置消费者所属的组
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "testGroup");
        /*
         * 当kafka持久化数据中没有offset时，所执行的动作
         * <ul>
         *  <li>earliest: 自动找到最早的，即分区的头部位置作为起始的offset，automatically reset the offset to the earliest offset
         *  <li>latest: 分区的尾部，即只会消费者连接后，生产者新生产的消息，automatically reset the offset to the latest offset</li>
         *  <li>none: throw exception to the consumer if no previous offset is found for the consumer's group</li>
         *  <li>anything else: throw exception to the consumer.</li></ul>";
         */
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // 自动提交offset，异步的，默认为true，如果需要手工控制offset提交，则设置为false
        // 自动提交是有延迟的，并不是消息拉取后立刻就提交offset，ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG
        // 如果自动提交开启，在消息处理的过程中发生异常，则消息offset被提交后会导致消息丢失
        // 如果消息很快就处理完成，已经提交了事务，但是offset延迟时间还没有到达，会导致消息的重复消费
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        // 一次拉取消息的条数
        //properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Arrays.asList("testTopic"));

        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(0));

            Iterator<ConsumerRecord<String, String>> iterator = records.iterator();
            while (iterator.hasNext()) {
                ConsumerRecord<String, String> record = iterator.next();
                System.out.println(record.partition());
                System.out.println("consumer partition:" + record.partition());
                System.out.println("consumer topic:" + record.topic());
                System.out.println("consumer offset:" + record.offset());
                System.out.println("consumer key:" + record.key());
                System.out.println("consumer value:" + record.value());
            }
        }


    }
}
