package learn.platform.commons.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadLocalMemory {

    private static final ExecutorService executor = new ThreadPoolExecutor(5, 5,
            0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100),
            r -> {
                Thread t = new Thread();
                t.setDaemon(false);
                return t;
            });

    private static ThreadLocal<Entry> threadLocal = ThreadLocal.withInitial(() -> new Entry("init"));

    public void mock() {
        executor.execute(() -> {
            threadLocal.set(new Entry(Thread.currentThread().getName()));
            System.out.println("user local variable.");
//            threadLocal.remove();
        });
    }

    public static void main(String[] args) {
        ThreadLocalMemory localMemory = new ThreadLocalMemory();
        for (int i = 0;i < 50; i++) {
            try {
                localMemory.mock();
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Main end!");
    }

    static class Entry {
        private static Long[] aLong = new Long[1024 * 1024];
        private String thrPrefix;

        public Entry(String thrPrefix) {
            this.thrPrefix = thrPrefix;
        }
    }
}
