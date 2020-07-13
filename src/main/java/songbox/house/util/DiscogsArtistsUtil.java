package songbox.house.util;

import songbox.house.domain.dto.response.discogs.DiscogsArtistDto;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;
import static org.springframework.util.CollectionUtils.isEmpty;
import static songbox.house.util.StringUtils.removeEndingNumberInBrackets;

public class DiscogsArtistsUtil {

    private static final String DEFAULT_DISCOGS_ARTISTS_DELIMITER = " & ";

    public static String concatArtists(final List<DiscogsArtistDto> artists) {
        if (!isEmpty(artists)) {
            return join(artists.stream()
                            .map(discogsArtistDto -> removeEndingNumberInBrackets(discogsArtistDto.getName()))
                            .iterator(),
                    DEFAULT_DISCOGS_ARTISTS_DELIMITER);
        } else {
            return "";
        }
    }
}
