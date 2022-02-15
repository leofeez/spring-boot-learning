# Redis

Redis是一个使用C语言编写的高性能非关系型数据库，于传统关系型数据库不同的是，Redis是基于内存的，所以速度很快，每秒QPS能达到10W，Redis还支持持久化，事务，Lua脚本，多种集群方案。

## Redis为什么快？

Redis的数据是存储在内存当中的，而关系型数据库的数据都是放在磁盘上。

- 磁盘的缺点

1. 寻址慢，磁盘的是ms级别的
2. 带宽

- 内存优点

1. 寻址，内存的寻址是ns级别的
2. 带宽大

所以在内存寻址上，磁盘比内存慢了10w倍。

- IO成本

  磁盘是由一个个扇区组成，一个磁盘的扇区512bytes，操作系统在读取数据的时候并不是按照一个个扇区去读取的，而是基于4K 去读取，所以当读取的数据小于4K，操作系统依旧会一次读取4K的内容。

- 关系型数据库

关系型数据库建表的时候必须给出列的类型和长度，因为关系型数据库倾向于行级存储（即存储数据是一行一行的），指定长度之后，即使那些为 null 的字段，也会用0去占位，这样在增删改的时候不需要去移动数据，只需要去覆写即可。

- 数据在磁盘和内存的体积是不一样的

Redis是单线程单进程的，这样可以避免不必要的多线程的切换的成本，也不用像关系型数据库去考虑各种锁机制。

​	Redis使用了IO 多路复用，并且是NIO的

数据库排名 ： https://www.db-engines.com/en/ranking/

## Redis 和 memchached 的区别？

memcached 中 value 没有类型的概念，在client取出比较复杂的value时，首先server端的网卡IO会影响数据的传输效率，对于value还需要客户端自行解码，增加了复杂度。

而redis中的value都是有类型的，本身也有对应的方法去解析value。

即计算向数据移动。

- redis 单进程，单线程，单实例为什么快？

    * 客户端连接，首先访问内核kernel，

      早期BIO，多个socket线程是通过read命令读取中连接的socket文件描述符fd(file descriptor)，这时候会产生阻塞，且线程切换需要成本。

      发展为单线程轮询（发生在用户空间）去read fd8..9.....同步非阻塞，用户进程需要轮询调用内核，即切用户态切换到内核态。

      的epoll(event poll)

    * NIO

- redis 默认会有16个库，在conf配置文件中可修改，利用select选择对应的库。

## Linux中安装Redis

1. yum install wget
2. yum install man man-pages 安装帮助文档
3. yum install gcc 安装c语言编译环境
4. wget 下载redis-*.tar.gz

2. 解压tar xf redis-*.tar.gz
3. 解压完成后，vi README.md
4. make编译
5. cd src 生成可执行程序
6. cd intall PREFIX=/opt/....
7. 配置环境变量export REDIS_HOME=，export PATH = $PATH:$REDIS_HOME/bin
8. cd utils
9. ./intall_server.sh
    1. 一个物理机可以有多个Redis实例（进城）service redis_6379 status 查看状态，通过port区分每个实例
    2. 可执行程序就一份，但是每个实例会对应各自的配置文件
    3. service redis_port start/stop/status
10. ps -fe | grep redis 查看所有redis进程。

## Redis中value的类型

Redis支持多种类型数据结构，如strings ，hash，list，sets，sorted sets，还支持范围查询，bitmaps，

help 命令查看帮助，如 `help @string`

type key 可以得到value的类型

| type

|encoding

**Redis 是二进制安全的**，底层存放的是字节数组。

### String

Redis由于是C编写的，所以字符串类型的底层实现其实利用了SDS（Simple dynamic String）,结构为 len, buf[],

- set key value [nx|xx] : nx 当key不存在的时候设置（即只能新增），xx是当key存在时才设置（即只能更新）
- mset key value....， msetnx，当其中某个keyset失败，整个mset失败，原子性
- mset key value [key value]: 批量设置key-value
- mget key [key...] 批量获取
- append key value 追加字符
- setget 设置新的value并返回旧的value
- getrange key start end，截取字符串
- setrange k1 offset end
- strlen

数值操作

- set key 99，这时候利用object encoding key 会返回int类型

- incr:

  应用场景：抢购，秒杀，详情页，社交网站的点赞/踩，评论数

- incrby key increment: 增加指定数值

- decr key；减一

- decrby key decrement;减去指定数值

bitmap

-
-

使用场景：

1. 有用户系统，统计用户的登陆天数，且窗口随机：可以利用二进制表示，0表示未登陆，1表示登陆，即每天天代表一个二进制位。

   第一天登陆 setbit leofee 0 1

   第10天登陆 setbit leofee 9 1

   统计登陆天数 bitcount leofee 0 -1

2. 京东618做活动，送礼物，大库备货多少礼物，假设京东有2亿的用户。即统计活跃用户

   每个用户对应一个二进制位

   假设第2个用户在2022-01-01登陆了：setbit 2022-01-01 2 1

   假设第2个用户在2022-02-01登陆了：setbit 2022-02-01 2 1

   假设第3个用户在2022-01-01登陆了：setbit 2022-01-01 3 1

   bitop or result 2022-01-01 2022-02-01

- setbit
- bitcount
- bitop

### List

key中有head和tail的指针

L 和 R 开头的命令区别是添加元素是从 链表的左边或者右边，例如LPUSH 和 RPUSH

- Redis中List是一个双向的链表

- 也可以看成java的栈结构（同向命令的时候）
- 也可以看成队列（反向命令的时候如，RPOP）

- 也可以看成数组
- 阻塞，单播队列（FIFO），B开头命令，如BLPOP

LREM key count value : 其中count有正负和0区分，正数代表从左边移除，负数代表从右边移除。



### hash

类似于Map<k, Map<k,v>>，可以存放面向对象类型的数据

场景：点赞，收藏，商品详情页

### set

- 无序去重

- 集合操作：差集SDIFF，并集SUNION，

- 随机事件，SRANDMEMBER key [count], 正数，取出一个去重的结果集

  ​	场景一：抽奖

  ​	场景二：交集：共同好友的功能

#### SortedSet

List中元素的顺序是按照元素添加的顺序

SortedSet 中有以下几个属性：

- 分值 score，用于排序
- 元素
- 索引

命令以Z开头：

- zadd key score member [score member ...]

  如一个班级学生的考试分数：zadd students 80 zhangsan 59 lisi 90 wangwu

- zrange key start stop

- zrevrange key start stop，物理内存左小右大且不随命令而发生变化

- zrank 获取排名

- zincre key increment member 增加指定元素的分值，场景：歌曲排行榜，获取歌曲排行榜前10名

集合操作：

- zunionscore destKey keynum key... weights

排序是如何实现的？

底层数据结构使用的skip list 跳表，增删改的速度综合平均值相对最优。



## 管道

类似于批量请求

## 发布订阅，PUB/SUB

发布：publish [channel] [message]

订阅：subscribe [channel]，订阅如果在发布之后才开始，不会接收到历史消息

可以模拟实时聊天，

## 事务

help @transactions

multi 开启事务

exec 执行事务

watch监控某个key，可以实现CAS

discard 撤销事务

## 布隆过滤器

解决缓存穿透，一定概率去（低于1%）防止用户请求一个数据库没有的数据，导致请求直接落到数据库上。

实现原理大致如下：

由一个很长的二进制向量和一系列的随机映射函数组成，去检索一个元素是否在一个集合中。

1. 你有哪些数据
2. 有的数据则向bitmap中设置标记
3. 请求有可能会被误判



## Key 过期和回收策略LRU

- Redis的key是可以设置过期时间的，set key ex 50 ,或者expire key 50，或者expireat 时间戳

  Redis keys过期有两种方式：被动和主动方式。

  当一些客户端尝试访问它时，key会被发现并主动的过期。

  还有一些已经过期但是一直没有被访问，就会被动的被Redis发现：

  具体就是Redis每秒10次做的事情：

    1. 测试随机的20个keys进行相关过期检测。
    2. 删除所有已经过期的keys。
    3. 如果有多于25%的keys过期，重复步奏1.

  以下操作不会延长有效期：

    1. 对key的访问不会延长有效期
    2. 倒计时不会延长过期时间
    3. 定时不会延长过期时间、

  以下操作会清除过期时间

    1. 对key发生写操作会剔除过期时间
    2. 利用PERSIST命令清除
    3. 对key进行RENAME，相关的超时时间会转移到新`key`上面。

- 由于Redis的数据都是放在内存中的，而内存的大小是有限制的，所以随着业务的运转，需要淘汰掉那些冷数据，所以对于Redis的key是需要一定的回收策略。

    - **noeviction**:返回错误当内存限制达到并且客户端尝试执行会让更多内存被使用的命令（大部分的写入指令，但DEL和几个例外）
    - **allkeys-lru**: 尝试回收最少使用的键（LRU），使得新添加的数据有空间存放。
    - **volatile-lru**: 尝试回收最少使用的键（LRU），但仅限于在过期集合的键,使得新添加的数据有空间存放。
    - **allkeys-random**: 回收随机的键使得新添加的数据有空间存放。
    - **volatile-random**: 回收随机的键使得新添加的数据有空间存放，但仅限于在过期集合的键。
    - **volatile-ttl**: 回收在过期集合的键，并且优先回收存活时间（TTL）较短的键,使得新添加的数据有空间存放。

  为了键设置过期时间也是需要消耗内存的，所以使用**allkeys-lru**这种策略更加高效，因为没有必要为键取设置过期时间当内存有压力时。

## Redis的持久化

Redis持久化的原理，就是会进行系统调用fork()出一个子进程去处理，主线程依旧保持处理客户端请求。

1. 进程间的数据是具有隔离性的，但是主线程可以设置数据对子线程可见，类似于export 设置环境变量。
2. 父子线程对于互相可见数据的写操作不会互相影响，
3. 由于内存中对于数据的写只是移动了指针的指向的物理地址，父子线程间的数据快照是基于指针的快照，而不是基于数据本身的快照，否则假如有10G的数据，如果复制一份的话，内存空间不一定支撑数据的全量复制。

### RDB 快照方式



### AOF 日志形式

