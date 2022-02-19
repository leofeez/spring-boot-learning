package demo;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ZookeeperDemo {

    public static void main(String[] args) throws Exception {

        ZooKeeper zk = new ZooKeeper("192.168.248.131:2181", 3000, e -> {
            System.out.println(e.toString());
            System.out.println(e.getPath());
        });


    }
}
