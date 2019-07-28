package songbox.house.service.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQuery {
    final String query;
    boolean fetchArtwork = true;
    boolean filterByArtistTitle = true;
}
