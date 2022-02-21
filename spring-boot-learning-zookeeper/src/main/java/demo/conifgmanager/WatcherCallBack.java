package demo.conifgmanager;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatcherCallBack  implements Watcher, AsyncCallback.DataCallback, AsyncCallback.StatCallback {

    private final ZooKeeper zk;

    private final MyConfiguration configuration;

    private final CountDownLatch configLatch;

    public WatcherCallBack(ZooKeeper zooKeeper, MyConfiguration configuration, int configCount) {
        this.zk = zooKeeper;
        this.configuration = configuration;
        this.configLatch = new CountDownLatch(configCount);
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                // 节点创建
                zk.getData(event.getPath(), this, this, "");
                break;
            case NodeDeleted:
                // 节点删除
                zk.getData(event.getPath(), this, this, "");
                break;
            case NodeDataChanged:
                // 节点改变
                zk.getData(event.getPath(), this, this, "");
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
            case PersistentWatchRemoved:
                break;
        }
    }

    /**
     * getData 回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        if ("/ip".equals(path) && data != null) {
            String ip = configuration.getIp();
            configuration.setIp(new String(data));

            // 重新watch
            zk.getData("/ip", this, this, "");


            if (ip == null) {
                configLatch.countDown();
            }

        }

        if ("/port".equals(path) && data != null) {
            String port = configuration.getPort();
            configuration.setPort(new String(data));

            // 重新watch
            zk.getData("/port", this, this, "");

            if (port == null) {
                configLatch.countDown();
            }
        }
    }

    /**
     * exits 回调，如果节点存在了，就去get Data
     *
     * @param stat 如果exists 节点不存在，则返回 null
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (stat != null) {
            if ("/ip".equals(path)) {
                zk.getData("/ip", this, this, "");
            } else if ("/port".equals(path)) {
                zk.getData("/port", this, this, "");
            }
        }
    }

    public void await() {
        try {
            configLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
