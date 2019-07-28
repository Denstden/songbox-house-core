package songbox.house.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;
import static java.util.Optional.empty;
import static songbox.house.util.Constants.PERFORMANCE_MARKER;

@Slf4j
public final class RetryUtil {

    public static final int DEFAULT_RETRIES = 10;

    private RetryUtil() {
    }

    public static <T, R> Optional<R> getOptionalWithRetries(final Function<T, Optional<R>> function, final T input,
            final int maxRetries, final String operation) {
        final long startMs = currentTimeMillis();
        int retries = 0;
        Optional<R> result;
        do {
            if (retries != 0) {
                log.debug("Retry {}, {}", retries, operation);
            }
            try {
                result = function.apply(input);
            } catch (Exception e) {
                log.debug("Retryable exception", e);
                result = empty();
            }
        } while (!result.isPresent() && ++retries < maxRetries);

        if (result.isPresent()) {
            log.debug(PERFORMANCE_MARKER, "Executed {} in {} retries, time {}ms",
                    operation, retries, currentTimeMillis() - startMs);
        } else {
            log.warn("Can't execute {} in {} tries", operation, retries);
        }


        return result;
    }

    public static <T, E, R> Optional<R> getOptionalWithRetries(final BiFunction<T, E, Optional<R>> function,
            final T input1, final E input2, final int maxRetries, final String operation) {
        int retries = 0;
        Optional<R> result;
        do {
            if (retries != 0) {
                log.debug("Retry {}, {}", retries, operation);
            }
            try {
                result = function.apply(input1, input2);
            } catch (Exception e) {
                log.debug("Retryable exception", e);
                result = empty();
            }
        } while (!result.isPresent() && ++retries < maxRetries);

        if (!result.isPresent()) {
            log.warn("Can't execute {} in {} tries", operation, retries);
        }

        return result;
    }

    public static <T, R> Optional<R> getOptionalWithDefaultRetries(final Function<T, Optional<R>> function,
            final T input, final String operation) {
        return getOptionalWithRetries(function, input, DEFAULT_RETRIES, operation);
    }
}
