package demo.lock;

public class TestZkLock {

    public static void main(String[] args) {
        ZkLock lock = new ZkLock();

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                lock.tryLock();

                lock.unlock();
            }).start();
        }

    }
}
