package songbox.house.util;

import lombok.Data;

@Data
public final class Pair<L, R> {
    private final L left;
    private final R right;

    public static <L, R> Pair<L, R> of(L l, R r) {
        return new Pair<>(l, r);
    }
}
