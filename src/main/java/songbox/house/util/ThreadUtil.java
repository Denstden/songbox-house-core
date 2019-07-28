package songbox.house.util;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public final class ThreadUtil {
    private ThreadUtil(){
    }

    public static void doSleep(int ms) {
        try {
            sleep(ms);
        } catch (InterruptedException e) {
            currentThread().interrupt();
            e.printStackTrace();
        }
    }
}
