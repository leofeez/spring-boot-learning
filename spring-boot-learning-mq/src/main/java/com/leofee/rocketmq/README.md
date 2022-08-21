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
## Broker

Broker 作为MQ中处理消息的服务，在RocketMQ中，当Broker启动时，会向所有NameServer注册自己的相关信息，如地址等，后续会周期性的向
NameServer发送心跳。

- 启动注册:`BrokerStartUp#start() -> BrokerController#registerBrokerAll() -> BrokerOutAPI#registerBrokerAll()`
- 发送心跳: `BrokerController#scheduleSendHeartbeat()`


## NameServer
NameServer 在RocketMQ中充当的角色就是注册中心，在内部维护了一个`BrokerAddrTable`，记录了所有Broker的信息，
当Broker向Nameserver发送注册请求时，交由`DefaultRequestProcessor#processRequest`进行处理请求，在该方法内部根据`RemotingCommand`中的
请求code`RequestCode`来区分当前的请求具体是哪一种类型，如`RequestCode#REGISTER_BROKER`，当接受到注册Broker的请求时，会执行
`RouteInfoManager#registerBroker`，将申请注册的Broker信息添加到`RouteInfoManager#brokerAddrTable`中。

除了将Broker信息注册后，还会对将Broker中的Topic以及Queue信息进行注册，Topic相关的信息都是存在`TopicConfig`中，将Topic存储到`RoutingInfoManager#topicQueueTable`

## Producer

## Consumer


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

### 指定Queue发送消息，保证消息的顺序性


## 消费消息
消费者消费，在RocketMQ中支持两种模式：
- MessageModel.CLUSTERING（集群）: 该模式为默认的，类似于P2P模式，在该模式下，一个*ConsumerGroup*中只会有一个Consumer 会接收到对应的消息进行消费。
- MessageModel.BROADCASTING（广播）:该模式为广播模式，订阅了对应的Topic，所有的消费者都会接收到对应的消息。

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

### 消息消费的ACK机制
在RocketMQ中，消息的ACK机制是依靠MessageListener的返回值进行决定：
- ConsumeConcurrentlyStatus.CONSUME_SUCCESS
- ConsumeConcurrentlyStatus.RECONSUME_LATER

### 消息过滤

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

### 事务消息
RocketMQ中提供了分布式事务的功能，常见的分布式事务的可以使用 2PC，TCC(try-catch-cancel)，RocketMQ采用的是2PC的方式，即消息发送之后
并不会立马被消费者消费，需要Producer对事务消息进行commit，消费者才可以真正的去消费这条消息，在RocketMQ中该机制称为
Half message

```java
    TransactionMQProducer producer = new TransactionMQProducer("transaction_producer_group");
        producer.setNamesrvAddr("192.168.248.131:9876");

        // Transaction Listener
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                System.out.println("executeLocalTransaction -> args:" + arg);
                System.out.println("executeLocalTransaction -> message:" + msg);
                System.out.println("executeLocalTransaction -> message transaction id :" + msg.getTransactionId());

                // 执行本地事务操作
                    
                /*
                 * COMMIT_MESSAGE: 表示事务可以提交
                 * ROLLBACK_MESSAGE: 表示事务需要回滚
                 * UNKNOW: 表示事务需要等待
                 */
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                // 检查本地事务的状态
                // 该方法是由RocketMQ开启定时去检查本地事务的状态
                System.out.println("checkLocalTransaction -> message transaction id:" + msg.getTransactionId());
        
                // return LocalTransactionState.UNKNOW;
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });
        producer.start();
        Message message = new Message("transaction_message_topic", "hello transaction message".getBytes(StandardCharsets.UTF_8));

        // Transaction message
        producer.sendMessageInTransaction(message, "hello transaction message");
```
RocketMQ的事务消息共有三个事务状态：
- `LocalTransactionState.COMMIT_MESSAGE`
- `LocalTransactionState.ROLLBACK_MESSAGE`
- `LocalTransactionState.UNKNOW`

## 重试机制

### 如何保证消息消费的顺序
在RocketMQ中，以Topic作为broker中最小的逻辑单位，在一个Topic中还包含若干个Queue，真正保存消息的其实还是Queue，只有在同一个Queue中的消息才是有序的，即FIFO。

生产者发送消息保证顺序：
1. 在保证消息的消费顺序前，首先需要保证在发送消息到RocketMQ中的发送顺序，建议使用单线程去发送。
2. 消息发送时，需要将消息都发送到同一个Queue中，利用RocketMQ中send(msg, MessageQueueSelector, args)方法指定对应的Queue。

消费者消费消息保证顺序：
1. 消费者消费注册监听器应该使用 MessageListenerOrderly而不是MessageListenerConcurrently

## 消费者如何监听broker是否存在消息
1. 普通轮询机制，间隔一定周期向server端broker发起请求，查看是否有消息存在，这种方式当消费者数量比较大，会消耗server端性能，因为会发送
大量无用请求。
   
2. 长连接，客户端与server端进行长连接，当服务端产生消息则实时推送给客户端进行消费，缺点是，server端需要与大量客户端建立连接，
   并且需要维护客户端的状态，如果大量消息产生，采用推送的方式，是没有办法知道客户端的消费能力。

3. 长轮询，客户端与server端进行连接，如果server端没有消息，则将连接进行挂起，当收到消息则告诉消费端，将主动权移交给客户端，进行拉取消息进行
消费，这样消费端可以根据自身的消费能力进行消息消费。

## 消费者启动流程分析

1. 默认的消费者 `DefaultMQPushConsumer`，从该类的名称上看，消费消息的机制是按照Push的方式，其实在底层还是有consumer从broker中拉取消息。
2. 消费者启动 `DefaultMQPushConsumer#start();`
