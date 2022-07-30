# Rocket MQ

## Linux 安装
由于RocketMQ是基于Java编写的，所以需要先安装好Java的运行环境。并配置好环境变量。
1. 准备Java环境: `vi /etc/profile`
   ```bash
      # 这里jdk路径根据实际情况
      export JAVA_HOME=/opt/leofee/java/jdk1.8.0_221
      export PATH=$PATH:$JAVA_HOME/bin
    ```
   
2. 下载 RocketMQ 压缩包到Linux上，地址[官网 4.9.3 版本](https://www.apache.org/dyn/closer.cgi?path=rocketmq/4.9.3/rocketmq-all-4.9.3-bin-release.zip)
   然后利用 unzip 进行解压（如果没有unzip，先执行 `yum install -y unzip`）
   ```bash
      unzip rocketmq-all-4.9.3-bin-release.zip
   ```
3. 切换到RocketMQ的bin目录下，启动 name server `./mqnamesrv`
   ```bash
      cd /usr/local/rocketmq-4.9.3/bin
      # 启动 name server
      ./mqnamesrv
   ```
   
4. 启动name server 之后，我们再启动 broker
   ```shell
      # -n localhost:9876 代表broker需要将自身的信息注册到name server 中
      ./mqbroker -n localhost:9876
   
      # 输出 The broker[localhost.localdomain, 192.168.248.131:10911] boot success. serializeType=JSON and name server is localhost:9876
      # 表示启动成功，并且 name server 是 localhost:9876
   ```

## 发送消息

### 同步阻塞发送消息

```java
    // 1. 设置生产者组 producer
    DefaultMQProducer producer=new DefaultMQProducer("hello_world_producer_group");
    // 2. 指定 name server
    producer.setNamesrvAddr("192.168.248.131:9876");
    Message message=new Message("hello_world","hello rocketmq".getBytes());
    producer.start();
    // 同步消息发送
    SendResult sendResult=producer.send(message);
    System.out.println(sendResult);
```

### 批量发送消息
```java
    // 批量发送
    List<Message> messageList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
        messageList.add(new Message("test_tag", "TAG-B", ("hello rocketmq" + i).getBytes()));
    }
    SendResult sendBatch = producer.send(messageList);
```

### 异步发送
```java
    // 异步发送
    producer.send(new Message("hello_world", "hello rocketmq async".getBytes(StandardCharsets.UTF_8)), new SendCallback() { 
        @Override
        public void onSuccess(SendResult sendResult) {
            System.out.println("async send result: " + sendResult);
        }   
        @Override
        public void onException(Throwable e) {
            e.printStackTrace();
        }
    });
```

## 消费消息

```java
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hello_world_consumer_group");
    // Name Server
    consumer.setNamesrvAddr("192.168.248.131:9876");
    // 消费者组
    consumer.setConsumerGroup("hello_world_consumer_group");
    // * 代表接收该Topic 下的所有消息
    consumer.subscribe("hello_world", "*");
    // 注册消息监听
    consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
        for (MessageExt msg : msgs) {   System.out.println(new String(msg.getBody()));
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }); 
    // 开始接收消息
    consumer.start();
```

## 消息过滤

1. 通过 `Tag` 过滤消息

生产者生产消息时指定消息的具体`Tag`
```java
    DefaultMQProducer producer = new DefaultMQProducer("hello_world_producer_group");
    producer.setNamesrvAddr("192.168.248.131:9876");
    producer.start();
    // Message 的 构造方法支持指定 Tag
    Message message = new Message("test_tag", "TAG-A", "hello world rocket tag filter".getBytes());
    SendResult sendResult = producer.send(message);
```

消费者消费消息时，指定对应的过滤策略
```java
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hello_world_consumer_group");
    consumer.setNamesrvAddr("192.168.248.131:9876");
    consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> { 
        for (MessageExt msg : msgs) {
            System.out.println(new String(msg.getBody()));
        }   
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    });
    // 指定只处理指定Tag的消息
    consumer.subscribe("test_tag", "TAG-A");
    consumer.start();
```
2. 通过 SQL 方式过滤消息
要使用SQL模式进行消息过滤，需要在RocketMQ的conf/broker.xml 中设置`enableProteryFilter=true`

生产者生产消息时设置对应的 Property
```java
    DefaultMQProducer producer = new DefaultMQProducer("hello_world_producer_group");
    producer.setNamesrvAddr("192.168.248.131:9876");
    producer.start();   
    // 批量发送 List<Message> messageList = new ArrayList<>();  for (int i = 0; i < 5; i++) {
            Message message = new Message("test_tag", "TAG-B", ("hello rocketmq" + i).getBytes());
            // 设置对应的属性
            message.putUserProperty("order", i + "");
            messageList.add(message);
    }
    SendResult sendBatch = producer.send(messageList);
```

消费者消费消息：
```java
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hello_world_consumer_group");
    consumer.setNamesrvAddr("192.168.248.131:9876");
    consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> { 
        for (MessageExt msg : msgs) {
            System.out.println(new String(msg.getBody()));
        }   
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    });
    // 创建对应的过滤器 MessageSelector selector = MessageSelector.bySql("order > 5 and order <= 10 "); consumer.subscribe("test_tag", selector);   consumer.start();
```

## 事务消息
