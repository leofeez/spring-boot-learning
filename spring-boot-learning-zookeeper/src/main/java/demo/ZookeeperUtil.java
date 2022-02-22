package demo;

import demo.conifgmanager.DefaultWatcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZookeeperUtil {

    public static ZooKeeper zk;

    private static final AtomicBoolean CONNECTED = new AtomicBoolean(false);

    private static final CountDownLatch CONNECTED_LATCH = new CountDownLatch(1);

    /**
     * 用于监听Zookeeper 连接成功事件
     */
    private static final DefaultWatcher DEFAULT_WATCHER = new DefaultWatcher(CONNECTED_LATCH, CONNECTED);

    /**
     * 连接Zookeeper
     *
     * @return Zookeeper
     */
    public static ZooKeeper connect() {
        if (CONNECTED.get()) {
            return zk;
        }
        try {
            zk = new ZooKeeper("192.168.248.131:2181,192.168.248.132:2181/config",
                    10000, DEFAULT_WATCHER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 只有当Zookeeper连接成功才返回
        waitForConnected();

        return zk;
    }

    /**
     * 等待zookeeper连接成功
     */
    private static void waitForConnected() {
        try {
            CONNECTED_LATCH.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
