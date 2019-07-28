package songbox.house.util.compare;

import songbox.house.domain.dto.response.SongDto;
import songbox.house.util.Pair;

import java.util.Comparator;

import static songbox.house.util.compare.LevenshteinDistanceComparator.MAX_COMPARE_RESULT;

public class SmartDiscogsComparator implements Comparator<SongDto> {
    final SongDto expectedSongDto;
    final Pair<String, String> expectedArtistTitle;
    final ArtistTitleComparator artistTitleComparator = new ArtistTitleComparator();

    private static final int DURATION_DIFF_MULTIPLIER = 3;
    private static final int BITRATE_DIFF_PENALTY = 60;

    public SmartDiscogsComparator(SongDto expectedSongDto, Pair<String, String> expectedArtistTitle) {
        this.expectedSongDto = expectedSongDto;
        this.expectedArtistTitle = expectedArtistTitle;
    }

    @Override
    public int compare(SongDto songDto1, SongDto songDto2) {
        int r1 = MAX_COMPARE_RESULT - artistTitleComparator.compareArtistTitle(expectedArtistTitle, Pair.of(songDto1.getArtist(), songDto1.getTitle()));
        int r2 = MAX_COMPARE_RESULT - artistTitleComparator.compareArtistTitle(expectedArtistTitle, Pair.of(songDto2.getArtist(), songDto2.getTitle()));

        Integer expectedDuration = expectedSongDto.getDuration();
        if (expectedDuration > 60 /*min amount of time for applying penalty on duration difference*/) {
            r1 += Math.abs(songDto1.getDuration() - expectedDuration) * DURATION_DIFF_MULTIPLIER; // duration is important so multiply it
            r2 += Math.abs(songDto2.getDuration() - expectedDuration) * DURATION_DIFF_MULTIPLIER;
        }

        if (songDto1.getBitRate() != null && songDto2.getBitRate() != null) {
            if (songDto1.getBitRate() > songDto2.getBitRate()) {
                r2 += BITRATE_DIFF_PENALTY;
            } else if (songDto1.getBitRate() < songDto2.getBitRate()) {
                r1 += BITRATE_DIFF_PENALTY;
            }
        }

        return r1 - r2;
    }
}