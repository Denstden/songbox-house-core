package songbox.house.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ThreadChange {
    public static interface ThreadChangeListener {
        void didChange();

        void willChange();
    }

    private static List<ThreadChangeListener> onThreadChangeListeners = new ArrayList<>();

    public static void addThreadChangeListener(ThreadChangeListener threadChangeListener) {
        onThreadChangeListeners.add(threadChangeListener);
    }

    public static void removeThreadChangeListener(ThreadChangeListener threadChangeListener) {
        onThreadChangeListeners.remove(threadChangeListener);
    }

    private static void notifyThreadChangeListenersDidChangeThread() {
        onThreadChangeListeners.forEach(ThreadChangeListener::didChange);
    }

    private static void notifyThreadChangeListenersWillChangeThread() {
        onThreadChangeListeners.forEach(ThreadChangeListener::willChange);
    }


    public abstract static class LocalAuthCallable<V> implements Callable<V> {
        public LocalAuthCallable() {
            notifyThreadChangeListenersWillChangeThread();
        }

        public abstract V callWithContext() throws Exception;

        @Override
        public V call() throws Exception {
            notifyThreadChangeListenersDidChangeThread();
            return callWithContext();
        }
    }

    public static Runnable applyContext(Runnable task) {
        notifyThreadChangeListenersWillChangeThread();
        return () -> {
            notifyThreadChangeListenersDidChangeThread();
            task.run();
        };
    }

    public static <V> Callable<V> applyContext(Callable<V> task) {
        return new LocalAuthCallable<V>() {
            @Override
            public V callWithContext() throws Exception {
                return task.call();
            }
        };
    }
}

