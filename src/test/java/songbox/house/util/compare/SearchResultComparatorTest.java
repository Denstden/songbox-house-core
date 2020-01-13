package songbox.house.util.compare;

import org.junit.Test;
import songbox.house.domain.dto.response.TrackMetadataDto;

import static org.junit.Assert.*;

public class SearchResultComparatorTest {

    private final SearchResultComparator comparator = new SearchResultComparator("Foreign fruit", "Sync 24 & Morphology");

    @Test
    public void shouldCompareCorrectly() {
        // Given
        TrackMetadataDto trackMetadataDto1 = create("Electrix Podcast 003 mixed by Sync 24", null, 3151, (short) 128, "Youtube");
        TrackMetadataDto trackMetadataDto2 = create("Foreign Fruit", "Sync 24 & Morphology", 396, (short) 320, "VK");

        // When
        int compare = comparator.compare(trackMetadataDto1, trackMetadataDto2);

        // Then
        assertEquals(-1, compare);
    }

    @Test
    public void shouldCompareByBitRates() {
        // Given
        TrackMetadataDto trackMetadataDto1 = create("Foreign Fruit", "Sync 24 & Morphology", 394, (short) 128, "Youtube");
        TrackMetadataDto trackMetadataDto2 = create("Foreign Fruit", "Sync 24 & Morphology", 396, (short) 320, "VK");

        // When
        int compare = comparator.compare(trackMetadataDto1, trackMetadataDto2);

        // Then
        assertEquals(-192, compare);
    }


    private TrackMetadataDto create(String title, String authors, Integer duration, Short bitRate, String resource) {
        TrackMetadataDto trackMetadataDto = new TrackMetadataDto();
        trackMetadataDto.setArtist(authors);
        trackMetadataDto.setTitle(title);
        trackMetadataDto.setDuration(duration);
        trackMetadataDto.setBitRate(bitRate);
        trackMetadataDto.setResource(resource);
        return trackMetadataDto;
    }



}