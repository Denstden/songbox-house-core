package songbox.house.domain.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class TrackInfoDto {
    private Long trackId;
    private String title;
    private List<ArtistDto> artists;
    private List<GenreDto> genres;
    private Double sizeMb;
    private Short durationSec;
    private Short bitRate;
    private String format;
}
