package songbox.house.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class ThreadChange {
    public static interface ThreadChangeListener {
        void didChange(String changeUUID);

        void willChange(String changeUUID);

        void finish(String changeUUID);
    }

    public static class ThreadChangeTransaction {
        private String changeUUID = UUID.randomUUID().toString();

        void didChange() {
            onThreadChangeListeners.forEach(it -> it.didChange(changeUUID));
        }

        void willChange() {
            onThreadChangeListeners.forEach(it -> it.willChange(changeUUID));
        }

        void finish() {
            onThreadChangeListeners.forEach(it -> it.finish(changeUUID));
        }
    }

    private static List<ThreadChangeListener> onThreadChangeListeners = new ArrayList<>();

    public static void addThreadChangeListener(ThreadChangeListener threadChangeListener) {
        onThreadChangeListeners.add(threadChangeListener);
    }

    public static void removeThreadChangeListener(ThreadChangeListener threadChangeListener) {
        onThreadChangeListeners.remove(threadChangeListener);
    }

    private static ThreadChangeTransaction createThreadChangeTransaction() {
        return new ThreadChangeTransaction();
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadChangeExecutorService(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    public static Runnable applyContext(Runnable task) {
        ThreadChangeTransaction changeTransaction = createThreadChangeTransaction();
        changeTransaction.willChange();
        return () -> {
            changeTransaction.didChange();
            task.run();
            changeTransaction.finish();
        };
    }

    public static <V> Callable<V> applyContext(Callable<V> task) {
        return new ThreadChangeCallable<V>() {
            @Override
            public V callWithContext() throws Exception {
                return task.call();
            }
        };
    }

    public abstract static class ThreadChangeCallable<V> implements Callable<V> {
        private final ThreadChangeTransaction changeTransaction = createThreadChangeTransaction();

        public ThreadChangeCallable() {
            changeTransaction.willChange();
        }

        public abstract V callWithContext() throws Exception;

        @Override
        public V call() throws Exception {
            changeTransaction.didChange();
            V result = callWithContext();
            changeTransaction.finish();
            return result;
        }
    }

    public static class ThreadChangeThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
        @Override
        public Future<?> submit(Runnable task) {
            return super.submit(applyContext(task));
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return super.submit(applyContext(task));
        }

        @Override
        public ListenableFuture<?> submitListenable(Runnable task) {
            return super.submitListenable(applyContext(task));
        }

        @Override
        public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
            return super.submitListenable(applyContext(task));
        }
    }

    public static class ThreadChangeExecutorService extends ThreadPoolExecutor {
        ThreadChangeExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        public void execute(Runnable command) {
            super.execute(applyContext(command));
        }

        @Override
        public Future<?> submit(Runnable task) {
            return super.submit(applyContext(task));
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return super.submit(applyContext(task), result);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return super.submit(applyContext(task));
        }
    }
}

