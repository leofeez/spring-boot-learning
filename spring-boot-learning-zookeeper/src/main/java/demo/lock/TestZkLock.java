package demo.lock;

import java.util.concurrent.TimeUnit;

public class TestZkLock {

    public static void main(String[] args) {
        ZkLock lock = new ZkLock();

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                lock.tryLock();

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lock.unlock();
            }).start();
        }

        while (true) {

        }
    }
}
