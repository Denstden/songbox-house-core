package songbox.house.util.parser;

import songbox.house.domain.dto.request.TrackListParsingResultDto;
import songbox.house.util.ArtistsTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public final class TrackListParser {
    public static final String ARTISTS_LABEL = "artists";
    public static final String TITLE_LABEL = "title";

    public static TrackListParsingResultDto parseTrackList(final String trackList, final String regex,
            final String separator) {
        final TrackListParsingResultDto result = new TrackListParsingResultDto();

        final List<ArtistsTitle> artistTitles = new ArrayList<>();
        final List<String> notFound = new ArrayList<>();

        final String[] split = trackList.split(separator);
        final Pattern pattern = Pattern.compile(regex);

        for (final String trackInfo : split) {
            final Matcher matcher = pattern.matcher(trackInfo);
            if (matcher.find()) {
                final ArtistsTitle artistTitleDto = ArtistsTitle.of(matcher.group(ARTISTS_LABEL), matcher.group(TITLE_LABEL));
                artistTitles.add(artistTitleDto);
            } else {
                notFound.add(trackInfo);
            }
        }

        result.setArtistTitles(artistTitles);
        result.setNotParsed(notFound);

        return result;
    }

    public static boolean canParse(final String regex, final String example) {
        try {
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(example);

            return matcher.find() && isNotEmpty(matcher.group(ARTISTS_LABEL)) && isNotEmpty(matcher.group(TITLE_LABEL));
        } catch (final Exception e) {
            return false;
        }
    }
}
