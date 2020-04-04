package songbox.house.util.compare;

import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.util.ArtistsTitle;

import java.util.Comparator;

// TODO Maybe compare ArtistsTitle
public class SearchResultComparator implements Comparator<TrackMetadataDto> {

    private final ArtistsTitle artistsTitle;
    private final TrackMetadataComparator trackMetadataComparator = new TrackMetadataComparator();
    private final LevenshteinDistanceComparator defaultStringComparator = new LevenshteinDistanceComparator();

    public SearchResultComparator(ArtistsTitle artistsTitle) {
        this.artistsTitle = artistsTitle;
    }

    @Override
    public int compare(TrackMetadataDto song1, TrackMetadataDto song2) {
        int compareArtists = compareArtists(song1.getArtists(), song2.getArtists(), artistsTitle.getArtists());
        if (compareArtists != 0) {
            return compareArtists;
        } else {
            int compareTitle = compareTitle(song1.getTitle(), song2.getTitle(), artistsTitle.getTitle());
            if (compareTitle != 0) {
                return compareTitle;
            } else {
                return trackMetadataComparator.compare(song1, song2);
            }
        }
    }

    private int compareTitle(String titleSong1, String titleSong2, String expectedTitle) {
        return compareStrings(titleSong1, titleSong2, expectedTitle);
    }

    private int compareArtists(String artist1, String artist2, String expected) {
        return compareStrings(artist1, artist2, expected);
    }

    private int compareStrings(String str1, String str2, String expected) {
        if (str1 == null && str2 == null) {
            return 0;
        }
        if (str1 == null) {
            return -1;
        }
        if (str2 == null) {
            return 1;
        }
        if (expected == null) {
            return 0;
        }

        int compare1 = defaultStringComparator.compare(str1, expected);
        int compare2 = defaultStringComparator.compare(str2, expected);

        return Integer.compare(compare1, compare2);
    }

}
