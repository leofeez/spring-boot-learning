package demo.conifgmanager;

import lombok.Data;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class ZookeeperConfigManager {

    private static final AtomicBoolean CONNECTED = new AtomicBoolean(false);

    public static ZooKeeper zk;

    public static final MyConfiguration configuration = new MyConfiguration();

    private static final CountDownLatch CONNECTED_LATCH = new CountDownLatch(1);

    /**
     * 用于监听Zookeeper 连接成功事件
     */
    private static final DefaultWatcher DEFAULT_WATCHER = new DefaultWatcher(CONNECTED_LATCH, CONNECTED);

    /**
     * 连接 Zookeeper
     */
    public static ZookeeperConfigManager connect() {
        ZookeeperConfigManager manager = new ZookeeperConfigManager();
        manager.getZk();
        return manager;
    }

    public ZooKeeper getZk() {
        if (CONNECTED.get()) {
            return zk;
        }
        try {
            zk = new ZooKeeper("192.168.248.131:2181,192.168.248.132:2181/config",
                    3000, DEFAULT_WATCHER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        waitForConnected();

        return zk;
    }

    public MyConfiguration getConfiguration() {
        CountDownLatch latch = new CountDownLatch(2);
        WatcherCallBack watcherCallBack = new WatcherCallBack(zk, configuration, latch);
        if (zk == null) {
            zk = getZk();
        }
        zk.exists("/ip", watcherCallBack, watcherCallBack, "");
        zk.exists("/port", watcherCallBack, watcherCallBack, "");

        // 只有当configuration 都已经配置好了才返回
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return configuration;
    }

    private static void waitForConnected() {
        // 只有当Zookeeper连接成功才返回
        try {
            CONNECTED_LATCH.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
