package demo.conifgmanager;

import java.util.concurrent.TimeUnit;

public class TestConfiguration {

    public static void main(String[] args) throws InterruptedException {
        ZookeeperConfigManager configManager = ZookeeperConfigManager.connect();
        MyConfiguration configuration = configManager.getConfiguration();

        while (true) {
            TimeUnit.SECONDS.sleep(2);
            System.out.println(configuration.getIp());
            System.out.println(configuration.getPort());
        }

    }
}
