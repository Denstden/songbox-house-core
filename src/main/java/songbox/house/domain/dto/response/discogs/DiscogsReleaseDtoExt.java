package songbox.house.domain.dto.response.discogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import songbox.house.domain.dto.request.ArtistTitleDto;
import songbox.house.domain.dto.response.SongDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscogsReleaseDtoExt {
    private Long id;

    private String thumbnail;
    private ArtistTitleDto artistTitle;
    private String audioLabel; /* RORA */
    private String audioLabelReleaseName; /* RORA 18*/
    private String country; /* Switzerland */
    private Set<String> genres; /* Minimal */
    private String discogsLink; /* https://www.discogs.com/IulyB-Systematic-EP/release/13370327 */

    private Map<ArtistTitleDto, List<SongDto>> songs;
}
