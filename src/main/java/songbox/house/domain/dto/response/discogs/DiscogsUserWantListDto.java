package songbox.house.domain.dto.response.discogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DiscogsUserWantListDto {
    private List<DiscogsUserWantListItemDto> wants;
    private DiscogsPaginationDto pagination;
}
