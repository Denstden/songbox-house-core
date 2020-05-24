package songbox.house.domain.entity.user;

import songbox.house.util.Mask;

public enum UserPropertyMask implements Mask {

    SEARCH_REPROCESS_ENABLED(1),
    AUTO_SEARCH_REPROCESS_AFTER_FAIL_ENABLED(1 << 1),
    AUTO_DOWNLOAD_SEARCH_REPROCESS_ENABLED(1 << 2),
    AUTO_USE_FULL_SEARCH_IF_FAST_NOT_SUCCESS(1 << 3),
    USE_ALWAYS_FULL_SEARCH(1 << 4);

    private final long mask;

    UserPropertyMask(long mask) {
        this.mask = mask;
    }

    @Override
    public long getMask() {
        return mask;
    }
}
