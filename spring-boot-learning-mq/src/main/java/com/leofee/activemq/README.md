# 消息中间件MQ

JMS(Java Message Service): Java 消息服务，是Java为消息系统定义的一套API标准，目前常见的实现有ActiveMQ，阿里的RocketMq, RabbitMQ
等。

在JMS系统中，消息是传送数据的单位，消息可以非常简单，如一个字符串，也可以很复杂， 如对象结构，消息的传递需要一个队列作为载体，即消息队列，
消息队列提供路由并保证消息的传递，如果发送消息时，接收者处于不可用状态，此时的消息会保留在队列中，直到成功的被接收者消费。

### 为什么需要MQ？

- 解耦：因为消息其实是语言和平台无关的数据，而且在语义上，也不是基于函数的调用，因此消息队列也可以实现多个应用之间松耦合的交互。
- 异步：消息队列的主要特点是异步的，主要的目的就是为了减少请求阻塞等待的消耗，所以主要的使用场景就是将那些比较耗时并且不是必须同步返回结果的操作（如磁盘IO，发邮件，短信）放入到队列中。
- 削峰：在某些场景中，由于在短时间会产生大量的请求，如果都是采用同步阻塞的方式处理请求，那么会产生大量的请求积压，最终会降低计算机的性能，而通过消息队列
将大量的请求处理放置在队列中，让请求的处理变得更平缓一些，让消费者有足够的时间去消费消息。

### JMS中的一些角色

### Broker
消息服务器，作为server提供消息核心服务。

### Queue 队列

### Provider
生产者，消息生产者是由会话创建的一个对象，用于把消息发送到一个目的地（Queue/Topic）。

```java
    // 获取一个连接
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
    Connection connection = connectionFactory.createConnection();

    // 以非事务方式(transacted = false)创建 session
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

    // 创建队列
    Queue queue = session.createQueue("leofee_queue");

    // 创建消息的生产者
    MessageProducer producer = session.createProducer(queue);
    producer.setDeliveryMode(DeliveryMode.PERSISTENT);

    // 发送消息到队列
    TextMessage textMessage = session.createTextMessage("hello");
    producer.send(queue, textMessage);
```

### Consumer
消费者，消息消费者是由会话创建的一个对象，它用于接收发送到目的地的消息。
```java
    // 获取一个连接
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
    Connection connection = connectionFactory.createConnection();
    connection.start();

    // 以非事务方式(transacted = false)创建 session
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

    // 创建队列
    Queue queue = session.createQueue("leofee_queue");

    // 创建消息的消费者
    MessageConsumer consumer = session.createConsumer(queue);

    // receive 不指定时间，则该方法会阻塞，直到接收到消息
    Message message = consumer.receive();
```

消息的消费可以采用以下两种方法之一：
- 同步消费。通过调用消费者的receive方法从目的地中显式提取消息。receive方法可以一直阻塞到消息到达。
  ```java
    // receive 不指定时间，则该方法会阻塞，直到接收到消息
    Message message = consumer.receive();
  
    // receive 支持指定超时时间，当超过指定时间后，receive 会返回 null
    Message messageWithTimeOut = consumer.receive(10000);
  ```
- 异步消费。客户可以为消费者注册一个消息监听器，以定义在消息到达时所采取的动作。
  ```java
    consumer.setMessageListener(message -> {
        // 处理消息
    });
  ```
### 消息 Message

- P2P：Queue：
    - 点对点的，消息被消费之后就会消失，所以不会出现重复消费
    - 支持多个Consumer，但是对于一个Message而言，只会被一个Consumer消费。如果消费者没有消费，消息会一直在队列中等待消费。
- PUB/SUB发布/订阅
    - Topic，支持多个订阅者订阅，当消息发布到Topic中后，所有的订阅者都会受到消息
    - 如果消息发布到Topic中，但是没有消费者，此时会丢失Topic
    - 消费者要先进行订阅，才能接收到消息

#### 消息的类型

- TextMessage
- ActiveMQObjectMessage
- ByteMessage

#### 消息优先级
消息的优先级可以保证消息消费的顺序性,优先级从0~9,由低到高
```java
// 在 producer 维度设置优先级
producer.setPriority(9);
// 在 message 维度设置优先级
message.setJMSPriority(9);
// 在send时指定
producer.send(message, DeliveryMode.PERSISTENT, 9, 0);
```

#### 消息的有效期

```java
producer.setTimeToLive(1000);
```
消息支持设置有效期，如果超出有效期，则会进入死信队列，默认的死信队列的名称为`ActiveMQ.DLQ`（支持自定义名称），可以通过从该死信队列进行重新消费。


## 消息可靠性机制

#### 1. 消息持久化

```java
// 设置开启消息持久化(默认就是PERSISTENT)
producer.setDeliveryMode(DeliveryMode.PERSISTENT);
```

消息持久化支持以下几种类型：
- kahadb：默认的持久化策略，日志存储，在ActiveMq目录的 `/data` 文件夹中
  * db.data
  * db.redo
  * db-1.log
  * lock
  
- jdbc：数据库存储，在activemq.xml中配置数据库连接信息。
  
  1. 在`/conf/activemq.xml`中添加一个数据库连接池的bean，并添加对应的数据库驱动和连接池的jar包到`/lib`目录下。
  ```xml
    <!-- 此处使用的是Druid数据库连接池 -->
    <bean id="mysql-ds" class="com.alibaba.druid.pool.DruidDataSource" destroy-method="close"> 
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/> 
        <property name="url" value="jdbc:mysql://localhost:3306/learning?useUnicode=true&amp;characterEncoding=utf-8&amp;autoReconnect=true&amp;useSSL=false&amp;serverTimezone=GMT"/> 
        <property name="username" value="root"/>
        <property name="password" value="admin123"/>
        <property name="maxActive" value="200"/>
        <property name="poolPreparedStatements" value="true"/>
    </bean>
  ```
  2. 修改`persistenceAdapter`
  ```xml
    <persistenceAdapter>
        <!-- dataSource 引用上述配置的bean -->
        <jdbcPersistenceAdapter dataSource="#mysql-ds" createTablesOnStartup="true" /> 
    </persistenceAdapter>
  ```
  3. 当生产者生产消息的时候，MQ会通过异步的方式将数据写入到数据库中
- jdbc journal：这种方式克服了JDBC Store的不足，JDBC存储每次消息过来，都需要去写库和读库。 ActiveMQ Journal，使用延迟存储数据到数据库，当消息来到时先缓存到文件中，延迟后才写到数据库中。
当消费者的消费速度能够及时跟上生产者消息的生产速度时，journal文件能够大大减少需要写入到DB中的消息。

当消息消费成功后，持久化中的消息就会被移除。

#### 2. 消息是支持事务的

消息生产者支持事务：
````java
// 第一个参数为true表示开启事务机制
Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
// 提交事务，只有当提交事务后，消费者才能消费消息
session.commit();
// 回滚事务，如果事务发生回滚，消息则不会出现在队列中
session.rollback();
````

消息消费者支持事务：
```java
// 消费者开启事务消费消息，则必须commit，消息才会从队列中移除
Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
Queue queue = session.createQueue("leofee_trx");

MessageConsumer consumer = session.createConsumer(queue);

TextMessage receive = (TextMessage) consumer.receive();
String text = receive.getText();

// 消费者提交事务后消息才算消费成功，才会从队列中移除
session.commit();
```

#### 3. 确认JMS消息 ACK

消息的成功消费可以分为三个阶段，消费者接受消息，消费者处理消息，消费者确认（ACK）。

- 消费者在开启事务的模式下，当发生commit时，消息也就随之ACK，如果只调用了`message.acknowledge()`但是没有commit，消息也就不会从队列移除。

  ```java
  // 消费者开启事务消费消息，则ACK机制默认是 SESSION_TRANSACTED 即使设置了CLIENT_ACKNOWLEDGE也是没有效果的
  Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
  
  Queue queue = session.createQueue("leofee_trx");
  
  MessageConsumer consumer = session.createConsumer(queue);
  
  TextMessage message = (TextMessage) consumer.receive();
  
  // commit后会自动ACK,消息才会从队列中移除
  session.commit();
  ```

- 消费者在非事务的模式下，消息的确认取决于设置的应答模式(ackknowlegement mode)，主要有以下几种：
    * `Session.AUTO_ACKNOWLEDGE`：当consumer.receive()方法返回时，或者从MessageListener.onMessage方法成功返回时，会自动确认消费者已经收到消息。
    * `Session.CLIENT_ACKNOWLEDGE`：客户端通过`Message#acknowledge`方法手动确认，如果消费者接收到消息没有显示调用acknowledge，
      消息就一直会存在队列中，还有一点需要注意的是，如果客户端一次性接受到10个消息， 但是在处理第5个的时候触发了acknowledge，
      这时候，会将所有的10个消息都进行确认，所以ackknowledge是基于一个session层面的。
    * `Session.DUPS_OK_ACKNOWLEDGE`：Session不必确保对传送消息的签收，这个模式可能会引起消息的重复，但是降低了Session的开销，所以只有客户端能容忍重复的消息，才可使用。
  ```java
  // 消费者未开启事务，设置了CLIENT_ACKNOWLEDGE 手工进行ack
  Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
  
  Queue queue = session.createQueue("leofee_trx");
  
  MessageConsumer consumer = session.createConsumer(queue);
  
  TextMessage message = (TextMessage) consumer.receive();
  
  // 对接收到的消息进行ACK
  message.acknowledge();
  ```
当存在多个消费者的情况下, 如果A消费者接收到某个消息没有被ack, 则其他消费者也不会收到对应的消息, 如果A消费者在ack的过程中, 连接断开,则该消息会被推送到其他消费者

#### 4. 死信队列
某些消息如果比较重要，可以利用死信队列，防止消息丢失，然后再重新从死信队列中重新消费掉。
