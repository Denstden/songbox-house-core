package songbox.house.util.compare;

import songbox.house.domain.dto.response.TrackMetadataDto;

import java.util.Comparator;

public class TrackMetadataComparator implements Comparator<TrackMetadataDto> {
    @Override
    public int compare(TrackMetadataDto song1, TrackMetadataDto song2) {
        final int compareBitRates = (song1.getBitRate() == null || song2.getBitRate() == null) ? 0 :
                song1.getBitRate().compareTo(song2.getBitRate());
        if (compareBitRates != 0) {
            return compareBitRates;
        } else {
            return (song1.getDurationSec() == null || song2.getDurationSec() == null) ? 0 :
                    song1.getDurationSec().compareTo(song2.getDurationSec());
        }
    }
}
