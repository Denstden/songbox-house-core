package songbox.house.domain.dto.request;

import lombok.Data;
import songbox.house.util.ArtistsTitle;

import java.util.List;

@Data
public class TrackListParsingResultDto {
    private List<ArtistsTitle> artistTitles;
    private List<String> notParsed;
}
