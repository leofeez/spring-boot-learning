package demo.lock;

import demo.ZookeeperUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class ZkLock {

    private static final ZooKeeper zk = ZookeeperUtil.connect();

    private String lockPath;

    public void tryLock() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<String> lockCallback = (path) -> this.lockPath = path;
        LockWatcherCallBack watcherCallBack = new LockWatcherCallBack(zk, countDownLatch, lockCallback);
        // 创建临时有序节点
        zk.create("/mylock", Thread.currentThread().getName().getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, watcherCallBack, Thread.currentThread().getName());

        await(countDownLatch);
    }

    public void unlock() {
        try {
            System.out.println(Thread.currentThread().getName() + " 释放锁!" + lockPath);
            zk.delete(lockPath, -1);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * 等待创建成功
     */
    private void await(CountDownLatch countDownLatch) {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
