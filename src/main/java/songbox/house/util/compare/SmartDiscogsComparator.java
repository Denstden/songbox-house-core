package songbox.house.util.compare;

import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.util.Pair;

import java.util.Comparator;

import static songbox.house.util.compare.LevenshteinDistanceComparator.MAX_COMPARE_RESULT;

public class SmartDiscogsComparator implements Comparator<TrackMetadataDto> {
    final TrackMetadataDto expectedTrackMetadataDto;
    final Pair<String, String> expectedArtistTitle;
    final ArtistTitleComparator artistTitleComparator = new ArtistTitleComparator();

    private static final int DURATION_DIFF_MULTIPLIER = 3;
    private static final int BITRATE_DIFF_PENALTY = 60;

    public SmartDiscogsComparator(TrackMetadataDto expectedTrackMetadataDto, Pair<String, String> expectedArtistTitle) {
        this.expectedTrackMetadataDto = expectedTrackMetadataDto;
        this.expectedArtistTitle = expectedArtistTitle;
    }

    @Override
    public int compare(TrackMetadataDto trackMetadataDto1, TrackMetadataDto trackMetadataDto2) {
        int r1 = MAX_COMPARE_RESULT - artistTitleComparator.compareArtistTitle(expectedArtistTitle, Pair.of(trackMetadataDto1.getArtist(), trackMetadataDto1.getTitle()));
        int r2 = MAX_COMPARE_RESULT - artistTitleComparator.compareArtistTitle(expectedArtistTitle, Pair.of(trackMetadataDto2.getArtist(), trackMetadataDto2.getTitle()));

        Integer expectedDuration = expectedTrackMetadataDto.getDuration();
        if (expectedDuration > 60 /*min amount of time for applying penalty on duration difference*/) {
            r1 += Math.abs(trackMetadataDto1.getDuration() - expectedDuration) * DURATION_DIFF_MULTIPLIER; // duration is important so multiply it
            r2 += Math.abs(trackMetadataDto2.getDuration() - expectedDuration) * DURATION_DIFF_MULTIPLIER;
        }

        if (trackMetadataDto1.getBitRate() != null && trackMetadataDto2.getBitRate() != null) {
            if (trackMetadataDto1.getBitRate() > trackMetadataDto2.getBitRate()) {
                r2 += BITRATE_DIFF_PENALTY;
            } else if (trackMetadataDto1.getBitRate() < trackMetadataDto2.getBitRate()) {
                r1 += BITRATE_DIFF_PENALTY;
            }
        }

        return r1 - r2;
    }
}