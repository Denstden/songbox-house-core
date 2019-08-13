package songbox.house.util;

import java.util.concurrent.Callable;

public class ThreadLocalAuth {
    public static class Context {
        private String userName;

        Context(String userName) {
            this.userName = userName;
        }
    }

    protected static class ThreadLocalCopier {
        ThreadLocalCopier(Context oldThreadContext) {
            this.oldThreadContext = oldThreadContext;
        }

        private Context oldThreadContext;

        public void applyToThisThread() {
            userContext.set(oldThreadContext);
        }
    }

    public abstract static class LocalAuthCallable<V> implements Callable<V> {
        private final ThreadLocalCopier threadLocalCopier;

        public LocalAuthCallable() {
            this.threadLocalCopier = ThreadLocalAuth.createThreadLocalCopier();
        }

        public abstract V callWithContext() throws Exception;

        @Override
        public V call() throws Exception {
            threadLocalCopier.applyToThisThread();
            return callWithContext();
        }
    }

    private static ThreadLocal<Context> userContext = new ThreadLocal<>();

    public static void setUser(String userName) {
        userContext.set(new Context(userName));
    }

    public static String getUser() {
        Context context = userContext.get();
        if (context == null) {
            return "";
        }

        return context.userName;
    }

    // Retrieve ThreadLocalCopier instance in old thread
    // And call ThreadLocalCopier::applyThisThread in a new thread
    public static ThreadLocalCopier createThreadLocalCopier() {
        return new ThreadLocalCopier(userContext.get());
    }

    public static Runnable applyContext(Runnable task) {
        ThreadLocalCopier threadLocalCopier = createThreadLocalCopier();
        return () -> {
            threadLocalCopier.applyToThisThread();
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

