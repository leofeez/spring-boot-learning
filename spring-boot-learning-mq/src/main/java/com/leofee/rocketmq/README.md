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

```java
    // producer
    DefaultMQProducer producer=new DefaultMQProducer("hello_world_group");
    // 指定 name server
    producer.setNamesrvAddr("192.168.248.131:9876");
    Message message=new Message("hello_world","hello rocketmq".getBytes());
    producer.start();
    // 同步消息发送
    SendResult sendResult=producer.send(message);
    System.out.println(sendResult);
```
批量发送，`producer.send` 支持批量发送