# Redisson 分布式锁

## 为什么需要分布式锁
在早期互联网的架构中，一个应用都是部署在一台机器上，这种单机情况下，利用JVM提供的锁机制即可解决共享数据在多线程场景下的线程安全问题，
随着技术的发展，分布式系统架构逐渐普及，在分布式架构中，由于一个应用会进行多机部署，服务器实例之间的JVM是互相独立的， 用JVM提供的锁在这种场景下是没办法共享，
所以需要一个中间件实现在分布式的场景下对锁的共享，典型的如 Redis 分布式锁。

_为什么是Redis_
- 由于Redis中的数据是存放在内存中，读写速度很快，所以加锁和释放锁的速度会很快，性能很高。
- Redis是单线程单进程的，对于客户端的请求都是串行化去执行的，所以Redis的单个命令是支持原子性的，而且对于多个redis指令，
  只需要将多个Redis的指令合并在一个Lua脚本发送到Redis就可以实现多条指令的原子性操作。

对于分布式锁的加锁和解锁就要求必须是原子性的，而Redis就可以很好的支持这一特性。

## 如何利用Redis实现锁机制
用一句话概括的说，其实Redis实现锁机制其实就是在Redis中设置一个key-value，当key存在时，即上锁，删除key即解锁。
当然要想实现一个很全面的锁机制，这其中还有很多细节不容忽视，所以下面，我们一步一步的看看如何使用Redis实现一个分布式的锁：

1. 加锁
   可以通过Redis的`setnx`命令实现，`setnx`即 set if not exists，即当key不存在时才能设置成功，由于Redis是单线程的，
   所以当大量请求过来时，一定只有一个请求才能设置成功，成功则返回1，否则返回0。
   ```java
      SETNX key value
        summary: Set the value of a key, only if the key does not exist
        since: 1.0.0
        group: string
   ```
      
2. 防止死锁
   当客户端加锁之后，在释放锁之前如果发生了宕机，那么redis中的锁就无法自动释放，最终产生死锁。
   所以为了避免死锁，我们还需要给这个key设置一个合理的过期时间，当锁占用的时间超过指定的过期时间，则自动删除该锁对应的key。
   
3. 锁过期提前释放
   上一步由于为了避免死锁，所以在加锁时，指定了锁的有效期，但是这个有效期也是估算出来的，如果业务处理时间超过了锁的有效期，锁会被提前释放，
   就会导致其他请求重新获得了锁，从而导致锁机制的失效。
   所以为了解决该问题，还需要一个机制去对锁进行续期，防止锁在加锁的业务还未处理完，被提前释放，我们可以利用一个子线程去定时的给锁进行续期。
   
4. 释放锁
   释放锁，只需要将对应的锁的key从redis中删除即可，但是这里需要注意的是，在释放锁之前，必须判断只有是当前线程占用的锁才可以进行释放。
   
综合以上的对Redis实现锁的思路分析，已经有了对应的开源框架的实现，就是 `Redisson`，`Redisson` 不仅实现了基于Redis实现加锁，解锁，还提供了
防死锁，锁续期，以及可重入的锁的功能，可以说能够满足大多数的场景了。

## Redisson原理

Redisson 提供RLock的接口，是继承于Lock，并扩展提供了基于过期释放的特性。

首先看一段利用Redisson加锁的代码：
```java
        // 获取锁对象
        RLock lock = redissonClient.getLock("stockLock" + stockId);
        try {
            // 加锁， 5 代表锁过期自动释放的时间，单位为 秒
            boolean locked = lock.tryLock(5, TimeUnit.SECONDS);

            if (locked) {
                // 处理业务逻辑
            } else {
                // 未获取锁的逻辑
            }
        } finally {
            // 只有持有锁的线程才能释放锁
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
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
   
对于上述的Lua脚本解读如下：

第一步通过 `exists key `去判断锁的key是否存在，
第二步，如果第一步中的判断返回0，表示 key 不存在，这时候可以加锁，利用`hset key value` 设置`key`的`value`，即`ARGV[2]`.
> ARGV[2] 代表的是加锁的客户端的 ID，类似于下面这样：285475da-9152-4c83-822a-67ee2f116a79:52，在Redisson中即UUID + threadId。
> 至于最后面的一个 1 是为了后面可重入做的计数统计，类似于AQS中的state。

第三步设置key的存活时间`internalLockLeaseTime`，这里 ARGV[1] 代表的是锁 key 的默认生存时间，默认 30 秒。

如果第一步中的`exists key`判断key已经存在，则利用`hexists key field` 判断当前的客户端ID（即ARG[2]）是否在锁的key对应的hash数据结构中是否存在，
存在表明是当前客户端持有的锁，这时候就相当于锁重入，就利用`hincrby key field increment`去对锁重入进行 + 1，并通过`pexpire key millseconds`设置过期时间。

如果上述两个if条件都未满足，则`pttl key `返回当前锁的key的剩余存活时间。

3. 从上述描述可以看出，锁的key其实可以设置过期时间的，key一旦过期，redis就会清除这个key，如果当业务处理的时间超出了锁的有效期，这时候锁就会被其他
   客户端获取成功，会造成数据的不准确，所以在Redisson中还存在一个WatchDog的机制去对去定期（默认10秒）去给锁续期，即Redisson会开启一个子线程并利用定时器
   去定时对锁的有效期进行。

4. unlock 释放锁，释放锁的时候需要判断当前的客户端（UUID + threadId）是否持有锁，只有持有锁的客户端才能释放锁。


## Redisson 单机模式下的缺点
事实上这类琐最大的缺点就是它加锁时只作用在一个Redis节点上，如果Redis挂了，那么就会产生单点故障的问题，
即使Redis通过sentinel保证高可用，虽然对于master节点发生故障后，可以故障转移，slaver升级为master，
但由于主从之间的数据同步是异步的， 如果在发生主从切换的时候，key 还没来得及同步到slaver上，那么就会出现锁丢失的情况：

   1. 在Redis的master节点上拿到了锁；
   2. 但是这个加锁的key还没有同步到slave节点；
   3. master故障，发生故障转移，slave节点升级为master节点；
   4. 导致锁丢失


所以Redis提供RedLock，即对主节点的Redis进行集群，多个master实例间互相独立，需要对N个实例进行上锁，这里假设有5个Redis集群，
当获取锁的时候，当且仅当大多数的节点（即 N/2 + 1）都设置锁成功，整个获取锁的过程才算成功，如果没有满足该条件，就需要在向所有的Redis实例发送释放锁命令即可，
不用关心之前有没有从Redis实例成功获取到锁.

## 推荐好文
> https://segmentfault.com/a/1190000041172633
