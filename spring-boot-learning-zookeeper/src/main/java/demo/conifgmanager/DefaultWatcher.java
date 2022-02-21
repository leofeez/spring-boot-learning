package demo.conifgmanager;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认的Watcher，用于观察Zookeeper的连接情况
 */
public class DefaultWatcher implements Watcher {

    private final CountDownLatch connectedLatch;

    private AtomicBoolean CONNECTED;

    public DefaultWatcher(CountDownLatch connectedLatch, AtomicBoolean connected) {
        this.connectedLatch = connectedLatch;
        this.CONNECTED = connected;
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getState()) {
            case Unknown:
                break;
            case Disconnected:
                break;
            case NoSyncConnected:
                break;
            case SyncConnected:
                System.out.println("connected......");
                CONNECTED.compareAndSet(false, true);
                connectedLatch.countDown();
                break;
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                break;
            case Closed:
                break;
        }
    }
}
