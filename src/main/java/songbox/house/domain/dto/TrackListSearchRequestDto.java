package songbox.house.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackListSearchRequestDto {
    private final String trackList;
    private final Long patternId;
    private final String separator;
    private final Set<String> genres;
    private final Long collectionId;
}
