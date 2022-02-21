package demo.lock;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LockWatcherCallBack implements AsyncCallback.StringCallback, AsyncCallback.ChildrenCallback, Watcher {

    private ZooKeeper zk;

    private CountDownLatch countDownLatch;

    private String sequenceNo;

    public LockWatcherCallBack(ZooKeeper zk,CountDownLatch countDownLatch) {
        this.zk = zk;
        this.countDownLatch = countDownLatch;
    }

    /**
     * create 节点回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {

        // 设置当前节点的sequence
        this.sequenceNo = name;
        zk.getChildren("/", false, this, ctx);
    }

    /**
     * getChildren 回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {

        // 排序
        Collections.sort(children);

        System.out.println(ctx);
        for (String child : children) {
            System.out.println(child);
        }

        // 当前是不是第一个节点
        int i = children.indexOf(sequenceNo.substring(1));
        if (i == 0) {
            System.out.println(ctx + " 获得了锁!");
            countDownLatch.countDown();
        } else {

        }


    }



    @Override
    public void process(WatchedEvent event) {

    }
}
