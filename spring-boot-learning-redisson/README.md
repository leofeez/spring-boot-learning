# Redisson 分布式锁

在单机情况下，利用JDK提供的锁即可满足，但是在分布式负载均衡的情况下，由于服务器实例之间的锁没办法共享，所以需要Redis作为中间件实现分布式的锁同步机制。



## 分布式锁的方案

- Redisson
- Zookeeper



## Redisson 实现

Redisson 实现锁机制的功能主要体现在以下几个方面：

- 互斥性：利用一个key作为锁标记，存入Redis，value是当前的UUID + 线程ID，当key不存在时才进行加锁
- 可重入性：利用key的计数实现可重入，类似于ReentrantLock
- 过期释放，防死锁
- 当释放锁的时候，只有当前持有锁的线程才能删除key

## 源码分析

首先看一下加锁的代码：
```java
        String orderName = Thread.currentThread().getName();
        String stockId = UUID.randomUUID().toString();

        RLock lock = redissonClient.getLock("stockLock" + stockId);

        boolean locked = false;
        try {

            log.info("订单：[{}]， 开始锁库存......", orderName);
            locked = lock.tryLock(5, 3, TimeUnit.SECONDS);

            if (locked) {
                orderService.createOrder();
            } else {
                log.info("[{}]订单生成超时......", orderName);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 只有持有锁的线程才能释放锁
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
```

1. 获取锁对象

`RLock lock = redissonClient.getLock("stockLock" + stockId);`
   
这里就是指定Redis中的key

2. tryLock 尝试获取锁，底层利用Lua脚本原子性的操作，去获取锁更新锁
    ```java
     <T> RFuture<T> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
        internalLockLeaseTime = unit.toMillis(leaseTime);
        
        // 利用Lua脚本
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, command,
                  "if (redis.call('exists', KEYS[1]) == 0) then " +
                      "redis.call('hset', KEYS[1], ARGV[2], 1); " +
                      "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                      "return nil; " +
                  "end; " +
                  "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                      "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                      "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                      "return nil; " +
                  "end; " +
                  "return redis.call('pttl', KEYS[1]);",
                    Collections.<Object>singletonList(getName()), internalLockLeaseTime, getLockName(threadId));
    }
    ```
   
第一步通过 exists 去判断锁的key是否存在，
第二步如果返回0，则取hset key value 设置key的value，即ARGV[2].
ARGV[2] 代表的是加锁的客户端的 ID，类似于下面这样：285475da-9152-4c83-822a-67ee2f116a79:52。至于最后面的一个 1 是为了后面可重入做的计数统计
第三部设置key的存活时间internalLockLeaseTime，这里 ARGV[1] 代表的是锁 key 的默认生存时间，默认 30 秒。

3. 当持有锁的时间超出了锁定有效期，则利用WatchDog 看门狗去定期（默认10秒）去给锁续期。

4. unlock 释放锁


## Redisson 单机模式下的缺点
事实上这类琐最大的缺点就是它加锁时只作用在一个Redis节点上，即使Redis通过sentinel保证高可用，
如果这个master节点由于某些原因发生了主从切换，那么就会出现锁丢失的情况：

   1. 在Redis的master节点上拿到了锁；
   2. 但是这个加锁的key还没有同步到slave节点；
   3. master故障，发生故障转移，slave节点升级为master节点；
   4. 导致锁丢失