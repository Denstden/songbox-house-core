package songbox.house.domain.dto.response.discogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscogsUserWantListItemDto {
    private final Long id;
}
