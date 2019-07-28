package songbox.house.util.compare;

import org.junit.Test;
import songbox.house.domain.dto.response.SongDto;
import songbox.house.util.compare.SearchResultComparator;

import static org.junit.Assert.*;

public class SearchResultComparatorTest {

    private final SearchResultComparator comparator = new SearchResultComparator("Foreign fruit", "Sync 24 & Morphology");

    @Test
    public void shouldCompareCorrectly() {
        // Given
        SongDto songDto1 = create("Electrix Podcast 003 mixed by Sync 24", null, 3151, (short) 128, "Youtube");
        SongDto songDto2 = create("Foreign Fruit", "Sync 24 & Morphology", 396, (short) 320, "VK");

        // When
        int compare = comparator.compare(songDto1, songDto2);

        // Then
        assertEquals(-1, compare);
    }

    @Test
    public void shouldCompareByBitRates() {
        // Given
        SongDto songDto1 = create("Foreign Fruit", "Sync 24 & Morphology", 394, (short) 128, "Youtube");
        SongDto songDto2 = create("Foreign Fruit", "Sync 24 & Morphology", 396, (short) 320, "VK");

        // When
        int compare = comparator.compare(songDto1, songDto2);

        // Then
        assertEquals(-192, compare);
    }


    private SongDto create(String title, String authors, Integer duration, Short bitRate, String resource) {
        SongDto songDto = new SongDto();
        songDto.setArtist(authors);
        songDto.setTitle(title);
        songDto.setDuration(duration);
        songDto.setBitRate(bitRate);
        songDto.setResource(resource);
        return songDto;
    }



}