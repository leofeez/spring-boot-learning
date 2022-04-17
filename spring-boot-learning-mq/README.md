# 消息中间件MQ

JMS(Java Message Service): Java 消息服务。

### 为什么需要MQ？

- 解耦：为了解决多系统之间的一个耦合性，当某个系统的一个业务数据需要多个系统去处理，但是他们的逻辑各不相同，如果采用传统强耦合的方式，需要适配每个
- 异步：就是将比较耗时而且不需要即时（同步）返回结果的操作作为消息放入消息队列，比如发送邮件和短信。
- 削峰：

### MQ 有什么缺点
- 

### JMS中的一些角色

### Broker

消息服务器，作为server提供消息核心服务

### provider

生产者，消息生产者是由会话创建的一个对象，用于把消息发送到一个目的地（Queue/Topic）。

### Consumer

消费者，消息消费者是由会话创建的一个对象，它用于接收发送到目的地的消息。消息的消费可以采用以下两种方法之一：

- 同步消费。通过调用消费者的receive方法从目的地中显式提取消息。receive方法可以一直阻塞到消息到达。
- 异步消费。客户可以为消费者注册一个消息监听器，以定义在消息到达时所采取的动作。



### 消息模型

- P2P：Queue：
    - 点对点的，消息被消费之后就会消失，所以不会出现重复消费
    - 支持多个Consumer，但是对于一个Message而言，只会被一个Consumer消费。如果消费者没有消费，消息会一直在队列中等待消费。
- PUB/SUB发布/订阅
    - Topic，支持多个订阅者订阅，当消息发布到Topic中后，所有的订阅者都会受到消息
    - 如果消息发布到Topic中，但是没有消费者，此时会丢失Topic
    - 消费者要先进行订阅，才能接收到消息


## ActiveMQ

### 消息可靠性机制

#### 1. 消息持久化

```java
// 设置开启消息持久化(默认就是PERSISTENT)
producer.setDeliveryMode(DeliveryMode.PERSISTENT);
```

消息持久化支持以下几种类型：

- kahadb：日志存储
- jdbc：数据库存储
- jdbc journal：这种方式克服了JDBC Store的不足，JDBC存储每次消息过来，都需要去写库和读库。 ActiveMQ Journal，使用延迟存储数据到数据库，当消息来到时先缓存到文件中，延迟后才写到数据库中。
当消费者的消费速度能够及时跟上生产者消息的生产速度时，journal文件能够大大减少需要写入到DB中的消息。

当消息消费成功后，持久化中的消息就会被移除。

#### 2. 消息发送是支持事务的

````java
// 第一个参数为true表示开启事务机制
Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
// 提交事务，只有当提交事务后，消费者才能消费消息
session.commit();
// 回滚事务，如果事务发生回滚，消息则不会出现在队列中
session.rollback();

````

#### 3. 确认JMS消息

消息的成功消费可以分为三个阶段，消费者接受消息，消费者处理消息，消费者确认（ACK）。

消费者在开启事务的模式下，当发生commit时，消息也就随之ACK。

消费者在非事务的模式下，消息的确认取决于设置的应答模式(ackknowlegement mode)，主要有以下几种：

- `Session.AUTO_ACKNOWLEDGE`：当consumer.receive()方法返回时，或者从MessageListener.onMessage方法成功返回时，会自动确认消费者已经收到消息。
- `Session.CLIENT_ACKNOWLEDGE`：客户端通过`Message#acknowledge`方法手动确认，但是需要注意的是，如果客户端一次性接受到10个消息，但是在处理第5个的时候触发了acknowledge，这时候，会将所有的10个消息都进行确认，所以ackknowledge是基于一个session层面的。
- `Session.DUPS_OK_ACKNOWLEDGE`：Session不必确保对传送消息的签收，这个模式可能会引起消息的重复，但是降低了Session的开销，所以只有客户端能容忍重复的消息，才可使用。



### 消息的类型

- TextMessage
- ActiveMQObjectMessage
- ByteMessage

### 消息优先级
消息的优先级可以保证消息消费的顺序性。
```java
// 在 producer 维度设置优先级
producer.setPriority(2);
// 在 message 维度设置优先级
message.setJMSPriority(9);
```

### 消息的有效期

```java
producer.setTimeToLive(1000);
```

消息支持设置有效期，如果超出有效期，则会进入死信队列，默认的死信队列的名称为`ActiveMQ.DLQ`（支持自定义名称），可以通过从该死信队列进行重新消费。

#### 死信队列
某些消息如果比较重要，可以利用死信队列，防止消息丢失，然后再重新从死信队列中重新消费掉。
