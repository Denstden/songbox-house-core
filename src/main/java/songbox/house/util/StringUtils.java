package songbox.house.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.toUpperCase;

public final class StringUtils {
    private static final Pattern PATTERN = Pattern.compile("^(.*)(\\(\\d+\\))");

    private static final String WRAPPED_REGEX = "\\[([^\\]]*)\\]";
    private static final String AUTHORS_SEPARATOR = "(&amp;)|(&)|( x )|(,)|( vs. )|( vs )|( ft. )|( ft )|( feat. )|( feat )|( with )|( and )";

    public static String removeEndingNumberInBrackets(final String input) {
        final Matcher matcher = PATTERN.matcher(input);

        if (matcher.matches()) {
            return matcher.group(1).trim();
        }

        return input.trim();
    }

    public static Set<String> parseAuthors(final String authorsString) {
        final Set<String> authors = new HashSet<>();
        final String normalizedAuthors = normalizeAuthors(authorsString);

        final String[] split = normalizedAuthors.split(AUTHORS_SEPARATOR);
        if (split.length == 0) {
            authors.add(authorsString.trim());
        } else {
            for (final String author : split) {
                authors.add(author.trim());
            }
        }

        return authors;
    }

    public static String firstCapitalLetter(String name) {
        return toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
    }

    private static String normalizeAuthors(final String authorsString) {
        final Pattern pattern = Pattern.compile(WRAPPED_REGEX);
        final Matcher matcher = pattern.matcher(authorsString);
        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return authorsString.trim();
        }
    }
}
