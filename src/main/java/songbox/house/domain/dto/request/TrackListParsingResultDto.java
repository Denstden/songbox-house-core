package songbox.house.domain.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class TrackListParsingResultDto {
    private List<ArtistTitleDto> artistTitles;
    private List<String> notParsed;
}
