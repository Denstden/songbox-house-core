package songbox.house.threadchange;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import songbox.house.util.ExecutorUtil;
import songbox.house.util.ThreadChange;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ThreadChangeTest {
    ExecutorService executorService = null;
    static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    private class ThreadLocalThreadChangeListener implements ThreadChange.ThreadChangeListener {
        Map<String, String> changeUUIDToThreadLocalValue = new ConcurrentHashMap<>();
        @Override
        public void didChange(String changeUUID) {
            String localValue = changeUUIDToThreadLocalValue.get(changeUUID);
            assertNotNull(localValue);

            assertNull("Context is not cleared", threadLocal.get());
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
    public void init() {
        threadLocal.set(null);
        executorService = ExecutorUtil.createExecutorService(4);
    }

    @Test
    public void shouldPassThreadIsChangedAsRunnable() {
        ThreadLocalThreadChangeListener threadLocalThreadChangeListener = new ThreadLocalThreadChangeListener();
        ThreadChange.addThreadChangeListener(threadLocalThreadChangeListener);

        String userName = UUID.randomUUID().toString();
        threadLocal.set(userName);
        System.out.println("Hello my name is: " + userName);

        final boolean[] taskIsFinished = {false};
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                assertEquals(userName, threadLocal.get());
                System.out.println("Hello from another thread, my name is :" + threadLocal.get());
                taskIsFinished[0] = true;
            }
        });


        while (!taskIsFinished[0]) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ThreadChange.removeThreadChangeListener(threadLocalThreadChangeListener);
    }

    @Test
    public void shouldPassThreadIsChangedAsTask() {
        ThreadLocalThreadChangeListener threadLocalThreadChangeListener = new ThreadLocalThreadChangeListener();
        ThreadChange.addThreadChangeListener(threadLocalThreadChangeListener);

        String userName = UUID.randomUUID().toString();
        threadLocal.set(userName);

        final boolean[] taskIsFinished = {false};

        System.out.println("Hello my name is: " + userName);

        executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(userName, threadLocal.get());
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

        ThreadChange.removeThreadChangeListener(threadLocalThreadChangeListener);
    }

    @Test
    public void shouldPassMultipleTasks() {
        ThreadLocalThreadChangeListener threadLocalThreadChangeListener = new ThreadLocalThreadChangeListener();
        ThreadChange.addThreadChangeListener(threadLocalThreadChangeListener);

        int taskCount = 64;
        AtomicInteger tasksDone = new AtomicInteger();

        String userName = UUID.randomUUID().toString();
        threadLocal.set(userName);

        for (int i = 0; i < taskCount; i++) {
            executorService.submit((Callable<Void>) () -> {
                assertEquals(userName, threadLocal.get());
                tasksDone.getAndIncrement();
                return null;
            });
        }


        while (tasksDone.get() != taskCount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ThreadChange.removeThreadChangeListener(threadLocalThreadChangeListener);
    }
}
