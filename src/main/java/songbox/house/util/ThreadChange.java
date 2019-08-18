package songbox.house.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public class ThreadChange {
    public static interface ThreadChangeListener {
        void didChange(String changeUUID);

        void willChange(String changeUUID);
    }

    private static List<ThreadChangeListener> onThreadChangeListeners = new ArrayList<>();

    public static void addThreadChangeListener(ThreadChangeListener threadChangeListener) {
        onThreadChangeListeners.add(threadChangeListener);
    }

    public static void removeThreadChangeListener(ThreadChangeListener threadChangeListener) {
        onThreadChangeListeners.remove(threadChangeListener);
    }

    private static void notifyThreadChangeListenersDidChangeThread(String changeUUID) {
        onThreadChangeListeners.forEach(it -> it.didChange(changeUUID));
    }

    private static void notifyThreadChangeListenersWillChangeThread(String changeUUID) {
        onThreadChangeListeners.forEach(it -> it.willChange(changeUUID));
    }


    public abstract static class LocalAuthCallable<V> implements Callable<V> {
        private final String changeUUID;

        public LocalAuthCallable() {
            this.changeUUID = UUID.randomUUID().toString();
            notifyThreadChangeListenersWillChangeThread(changeUUID);
        }

        public abstract V callWithContext() throws Exception;

        @Override
        public V call() throws Exception {
            notifyThreadChangeListenersDidChangeThread(changeUUID);
            return callWithContext();
        }
    }

    public static Runnable applyContext(Runnable task) {
        String changeUUID = UUID.randomUUID().toString();
        notifyThreadChangeListenersWillChangeThread(changeUUID);
        return () -> {
            notifyThreadChangeListenersDidChangeThread(changeUUID);
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

