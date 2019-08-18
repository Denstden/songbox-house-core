package songbox.house.threadchange;

import org.junit.Before;
import org.junit.Test;
import songbox.house.util.ExecutorUtil;
import songbox.house.util.ThreadChange;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import static org.junit.Assert.*;

public class ThreadChangeTest {
    static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    private class ThreadLocalThreadChangeListener implements ThreadChange.ThreadChangeListener {
        Map<String, String> changeUUIDToThreadLocalValue = new ConcurrentHashMap<>();
        @Override
        public void didChange(String changeUUID) {
            String localValue = changeUUIDToThreadLocalValue.get(changeUUID);
            assertNotNull(localValue);

            threadLocal.set(localValue);
        }

        @Override
        public void willChange(String changeUUID) {
            assertNotNull(threadLocal.get());
            changeUUIDToThreadLocalValue.put(changeUUID, threadLocal.get());
        }

        @Override
        public void finish(String changeUUID) {
            assertNotNull(threadLocal.get());
            threadLocal.set(null);
            assertTrue(changeUUIDToThreadLocalValue.containsKey(changeUUID));
            changeUUIDToThreadLocalValue.remove(changeUUID);
        }
    }

    @Before
    public void clearThreadLocal() {
        threadLocal.set(null);
    }

    @Test
    public void testThreadIsChangedAsRunnable() {
        ThreadChange.addThreadChangeListener(new ThreadLocalThreadChangeListener());
        String userName = UUID.randomUUID().toString();
        threadLocal.set(userName);
        System.out.println("Hello my name is: " + userName);

        final boolean[] taskIsFinished = {false};
        ExecutorService executorService = ExecutorUtil.createExecutorService(4);
        executorService.submit(() -> {
            System.out.println("Hello from another thread, my name is :" + threadLocal.get());
            taskIsFinished[0] = true;
        });


        while (!taskIsFinished[0]) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testThreadIsChangedAsTask() {

        ThreadChange.addThreadChangeListener(new ThreadLocalThreadChangeListener());
        String userName = UUID.randomUUID().toString();
        threadLocal.set(userName);
        System.out.println("Hello my name is: " + userName);

        final boolean[] taskIsFinished = {false};
        ExecutorService executorService = ExecutorUtil.createExecutorService(4);

        executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                System.out.println("Hello from another as task thread, my name is :" + threadLocal.get());
                taskIsFinished[0] = true;
                return null;
            }
        });


        while (!taskIsFinished[0]) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
