package demo.conifgmanager;

import demo.ZookeeperUtil;
import lombok.Data;
import org.apache.zookeeper.ZooKeeper;

@Data
public class ZookeeperConfigManager {

    public static final MyConfiguration configuration = new MyConfiguration();

    public static MyConfiguration getConfiguration() {
        // 连接zk
        ZooKeeper zk = ZookeeperUtil.connect();

        // 注册回调
        WatcherCallBack watcherCallBack = new WatcherCallBack(zk, configuration, 2);

        // 判断配置是否存在
        zk.exists("/ip", watcherCallBack, watcherCallBack, "");
        zk.exists("/port", watcherCallBack, watcherCallBack, "");

        // 只有当configuration 都已经存在了才返回
        watcherCallBack.await();

        return configuration;
    }



}
