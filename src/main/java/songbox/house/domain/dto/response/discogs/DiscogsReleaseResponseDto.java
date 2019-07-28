package songbox.house.domain.dto.response.discogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscogsReleaseResponseDto {
    private String title;
    private List<DiscogsArtistDto> artists;
    private List<String> styles;
    private Short year;
    private List<DiscogsTrackDto> tracklist;
}
