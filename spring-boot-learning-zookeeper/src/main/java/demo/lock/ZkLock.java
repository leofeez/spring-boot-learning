package demo.lock;

import demo.ZookeeperUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZkLock {

    private static final ZooKeeper zk = ZookeeperUtil.connect();

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public void tryLock() {
        LockWatcherCallBack watcherCallBack = new LockWatcherCallBack(zk, countDownLatch);
        // 创建临时有序节点
        zk.create("/mylock", Thread.currentThread().getName().getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, watcherCallBack, Thread.currentThread().getName());

        await();
    }

    public void unlock() {

    }

    /**
     * 等待创建成功
     */
    private void await() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
