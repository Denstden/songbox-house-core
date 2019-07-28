package songbox.house.util;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static songbox.house.util.Pair.of;

@Slf4j
public class ArtistTitleUtil {

    private static final Pattern ARTIST_TITLE_REGEX = compile("^((\\W*\\s*)|(#\\d*;?.?))?" +
            "(?<artists>[A-Z].*)(\\s*)( - )(\\s*)(?<title>.*)$");

    public static Pair<String, String> extractArtistTitle(String query) {
        Matcher matcher = ARTIST_TITLE_REGEX.matcher(query);
        if (matcher.matches()) {
            String artists = matcher.group("artists");
            String title = matcher.group("title");
            if (isNotBlank(artists) && isNotBlank(title)) {
                return of(artists, title);
            } else {
                log.warn("Can't parse artists and title from '{}' (check regex)", query);
            }
        }

        // TODO remove if regex will work fine
        return extractDummy(query);
    }

    public static Pair<String, String> extractArtistTitleFromVkSearchResultDtoList(final List vkSearchResponseList) {
        return of((String) vkSearchResponseList.get(4), (String) vkSearchResponseList.get(3));
    }

    private static Pair<String, String> extractDummy(String query) {
        String[] artistTitle = query.split("-", 2);

        String artists = null;
        String title = query;
        if (artistTitle.length == 2) {
            artists = artistTitle[0].trim();
            title = artistTitle[1].trim();
        } else {
            String[] splitedBySpace = query.split(" ");
            if (splitedBySpace.length == 2) {
                artists = splitedBySpace[0].trim();
                title = splitedBySpace[1].trim();
            }
        }

        return of(artists, title);
    }
}
