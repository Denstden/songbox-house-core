package songbox.house.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BitRateCalculator {
    public static Short calculateBitRate(final Integer duration, final Long size) {
        log.trace("Duration {}, size {}", duration, size);
        if (duration == null || duration == 0) {
            return 0;
        }

        final Long bitRate = size / duration / 128;
        return round(bitRate);
    }

    private static Short round(final Long bitRate) {
        if (bitRate > 300) {
            return 320;
        }
        if (bitRate > 196) {
            return 196;
        }
        return 128;
    }
}
