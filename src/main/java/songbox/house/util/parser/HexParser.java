package songbox.house.util.parser;

import songbox.house.util.Pair;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexParser {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?<first>[0-9a-f]+)//(?<second>[0-9a-f]+)///(?<third>[0-9a-f]+)");

    public static Optional<Pair<String, String>> parseHex(String hex) {
        Matcher matcher = HEX_PATTERN.matcher(hex);

        if (matcher.find()) {
            return Optional.of(new Pair<>(matcher.group("second"), matcher.group("third")));
        }

        return Optional.empty();
    }
}
