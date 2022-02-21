package demo.conifgmanager;

import java.util.concurrent.TimeUnit;

public class TestConfiguration {

    public static void main(String[] args) throws InterruptedException {

        MyConfiguration configuration = ZookeeperConfigManager.getConfiguration();

        while (true) {
            TimeUnit.SECONDS.sleep(2);
            System.out.println(configuration.getIp());
            System.out.println(configuration.getPort());
        }

    }
}
