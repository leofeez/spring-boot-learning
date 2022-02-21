package demo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class ZookeeperDemo {

    public static void main(String[] args) throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(3);
        ZooKeeper zk = new ZooKeeper("192.168.248.131:2181", 3000, e -> {
            System.out.println("new zk" + e.toString());

            switch (e.getState()) {
                case Unknown:
                    break;
                case Disconnected:
                    break;
                case NoSyncConnected:
                    break;
                case SyncConnected:
                    System.out.println("connected...");
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

        });


        Stat stat = new Stat();
        zk.create("/leofee", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, stat);


        zk.setData("/leofee", "hello".getBytes(), 0, new AsyncCallback.StatCallback() {

            @Override
            public void processResult(int code, String path, Object ctx, Stat stat) {
                // KeeperException.Code.OK.
                System.out.println(code);
                System.out.println(path);
                System.out.println(ctx);
                System.out.println(stat);
                countDownLatch.countDown();
            }
        }, "");


        Stat finalStat = stat;
        byte[] data = zk.getData("/leofee", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getData event = " + event.toString());

                // 再次注册watcher
                try {
                    zk.getData("/leofee", this, finalStat);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }

                countDownLatch.countDown();
            }
        }, stat);
        System.out.println("data = " + new String(data));

        stat = zk.setData("/leofee", "hello world".getBytes(), stat.getVersion());

        zk.setData("/leofee", "hello leofee".getBytes(), stat.getVersion());

        countDownLatch.await();

        FutureTask<String> futureTask = new FutureTask<>(() -> {
            TimeUnit.SECONDS.sleep(5);
            return "hello";
        });


        new Thread(futureTask).start();
        String s = futureTask.get();
        System.out.println(s);
    }
}
