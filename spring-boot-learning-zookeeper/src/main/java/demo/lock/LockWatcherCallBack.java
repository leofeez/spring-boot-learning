package demo.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class LockWatcherCallBack implements AsyncCallback.StringCallback, AsyncCallback.ChildrenCallback, Watcher, AsyncCallback.StatCallback {

    private ZooKeeper zk;

    private CountDownLatch countDownLatch;

    private String sequenceNo;

    private Consumer<String> consumer;

    private String threadName;

    public LockWatcherCallBack(ZooKeeper zk,CountDownLatch countDownLatch, Consumer<String> consumer) {
        this.zk = zk;
        this.countDownLatch = countDownLatch;
        this.consumer = consumer;
        threadName = Thread.currentThread().getName();
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


        // 当前是不是第一个节点
        int i = children.indexOf(sequenceNo.substring(1));
        if (i == 0) {
            System.out.println(threadName + " 获得了锁!" + sequenceNo);
            consumer.accept(sequenceNo);
            countDownLatch.countDown();
        }
        // 当前不是第一个节点，则判断前一个节点是否存在
        else {
            // watcher 用于监听前一个节点
            zk.exists("/" + children.get(i -1), this, this, ctx);
        }


    }



    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zk.getChildren("/", false, this, "");
                break;
            case NodeDataChanged:
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
     * exists 回调
     * @param rc
     * @param path
     * @param ctx
     * @param stat 如果exists 节点不存在，则返回 null
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (stat == null) {
            zk.getChildren("/", false, this, ctx);
        }
    }
}
