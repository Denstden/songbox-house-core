package songbox.house.domain.dto.response;

import lombok.Data;

@Data
public class TracksDto {
    private Iterable<TrackInfoDto> tracks;
    private PageDto pageDto;
}
