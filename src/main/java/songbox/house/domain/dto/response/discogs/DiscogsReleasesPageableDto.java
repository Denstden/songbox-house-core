package songbox.house.domain.dto.response.discogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscogsReleasesPageableDto implements ReleasePageable {
    private DiscogsPaginationDto pagination;
    private List<DiscogsReleaseDto> releases;

}
